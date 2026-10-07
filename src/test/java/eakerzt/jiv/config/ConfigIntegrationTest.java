package eakerzt.jiv.config;

import eakerzt.jiv.config.api.Configs;
import eakerzt.jiv.config.gui.ConfigGui;
import eakerzt.jiv.config.gui.MezzConfigScreenConfigs;
import eakerzt.jiv.config.gui.config.ConfigGuiOptions;
import eakerzt.jiv.config.gui.config.ConfigGuiOptionsPlugin;
import eakerzt.jiv.config.gui.remote.RemoteConfigRequestChunkPayload;
import eakerzt.jiv.config.gui.remote.RemoteConfigResponseChunkPayload;
import eakerzt.jiv.config.minecraft.network.IdentityPacket;
import eakerzt.jiv.config.minecraft.network.SyncPacket;
import eakerzt.jiv.config.registration.ConfigProvider;
import eakerzt.jiv.gui.config.JivConfigGuiPlugin;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ConfigIntegrationTest {
    @Test
    void embeddedProviderAndGuiOptionsShareOneJivScreen() {
        var builder = Configs.forMod("jiv").createClientSchemaBuilder(
            "integration-" + UUID.randomUUID() + ".ini", "jiv.config.integration"
        );
        var enabled = builder.addCategory("integration").addBoolean("enabled", true).build();
        var schema = builder.build();
        assertTrue(enabled.get());
        assertEquals("jiv", schema.getModId());
        boolean client = ConfigProvider.getEnvironment().isPhysicalClient();
        assertEquals(client, schema.isActive());
        if (client) {
            assertTrue(enabled.set(false));
            assertFalse(enabled.get());
            assertEquals("jiv", schema.getPath().orElseThrow().getParent().getParent().getFileName().toString());
        } else {
            assertTrue(schema.getPath().isEmpty());
            assertThrows(IllegalStateException.class, () -> enabled.set(false));
        }

        ConfigGuiOptions.register();
        var schemas = List.of(schema, ConfigGuiOptions.getSchema());
        var screens = MezzConfigScreenConfigs.getConfigScreens(schemas);
        assertEquals(1, screens.size());
        assertEquals("jiv", screens.getFirst().getModId());
        var categories = screens.getFirst().getSchema().getCategories().stream()
            .map(category -> category.getName()).collect(Collectors.toSet());
        if (client) {
            assertTrue(categories.containsAll(Set.of("integration", "appearance", "modList")));
        } else {
            assertTrue(categories.isEmpty());
        }

        var registry = ConfigGui.createScreenFactoryRegistry(
            schemas, List.of(new ConfigGuiOptionsPlugin(), new JivConfigGuiPlugin())
        );
        assertEquals(Set.of("jiv"), registry.getFactories().keySet());
        assertEquals(1, registry.getEntries().size());
    }

    @Test
    void configurationSyncAndEditorChannelsBelongToJiv() {
        var ids = List.of(IdentityPacket.TYPE.id(), SyncPacket.TYPE.id(),
            RemoteConfigRequestChunkPayload.TYPE.id(), RemoteConfigResponseChunkPayload.TYPE.id());
        assertEquals(4, Set.copyOf(ids).size());
        for (var id : ids) {
            assertEquals("jiv", id.getNamespace());
            assertTrue(id.getPath().startsWith("config/"));
        }
    }
}
