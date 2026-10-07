package eakerzt.jiv.test;

import eakerzt.jiv.common.config.legacy.LegacyConfigPaths;
import eakerzt.jiv.library.config.ModIdFormatConfig;
import eakerzt.jiv.config.api.schema.IConfigSchema;
import eakerzt.jiv.config.file.ConfigFileWatcherSettings;
import eakerzt.jiv.config.file.ConfigFileUtil;
import eakerzt.jiv.config.file.ConfigManager;
import eakerzt.jiv.config.schema.ConfigSchemaBuilder;
import eakerzt.jiv.config.schema.LayeredConfigSchemaPathResolver;
import eakerzt.jiv.config.schema.StaticConfigSchemaPathResolver;
import net.minecraft.ChatFormatting;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModIdFormatConfigMigrationTest {
	private static Stream<Arguments> legacyFormats() {
		return Stream.of(
			Arguments.of("red bold", List.of(ChatFormatting.RED, ChatFormatting.BOLD)),
			Arguments.of("\"red bold\"", List.of(ChatFormatting.RED, ChatFormatting.BOLD)),
			Arguments.of("RED, BOLD", List.of(ChatFormatting.RED, ChatFormatting.BOLD)),
			Arguments.of("", List.of()),
			Arguments.of("\"\"", List.of())
		);
	}

	@ParameterizedTest
	@MethodSource("legacyFormats")
	public void migratesLegacyFormattingTransactionallyThroughMezzConfig(String legacyFormat, List<ChatFormatting> expected, @TempDir Path tempDir) throws IOException {
		Path configDirectory = tempDir.resolve("jiv");
		Path legacyFile = configDirectory.resolve("jiv-mod-id-format.ini");
		Path mezzConfigFile = configDirectory.resolve("client").resolve("jiv-mod-id-format.ini");
		Files.createDirectories(configDirectory);
		Files.writeString(legacyFile, "[modName]\nmodNameFormat = " + legacyFormat + "\n");
		UUID profileId = UUID.randomUUID();

		ConfigFileWatcherSettings disabledWatcher = ConfigFileWatcherSettings.clientDefaults().withEnabled(false);
		ConfigManager configManager = new ConfigManager("JIV Mod Name Config Migration Test", disabledWatcher, disabledWatcher);
		var pathResolver = new LayeredConfigSchemaPathResolver(
			configDirectory.resolve("client/default/jiv-mod-id-format.ini"),
			new StaticConfigSchemaPathResolver(mezzConfigFile)
		);
		ConfigSchemaBuilder schemaBuilder = new ConfigSchemaBuilder("jiv", pathResolver, "jiv.config.modIdFormat", configManager);
		new ModIdFormatConfig(
			schemaBuilder,
			LegacyConfigPaths.get(configDirectory, profileId, "jiv-mod-id-format.ini")
		);
		IConfigSchema schema = schemaBuilder.build();

		assertEquals(expected, getConfiguredValue(schema));
		assertTrue(Files.exists(legacyFile));
		assertTrue(Files.exists(mezzConfigFile));
		assertEquals(Files.readString(legacyFile), Files.readString(ConfigFileUtil.getBackupPath(legacyFile, 1)));

		ConfigManager reloadedConfigManager = new ConfigManager("Reloaded JIV Mod Name Config", disabledWatcher, disabledWatcher);
		ConfigSchemaBuilder reloadedSchemaBuilder = new ConfigSchemaBuilder("jiv", pathResolver, "jiv.config.modIdFormat", reloadedConfigManager);
		new ModIdFormatConfig(reloadedSchemaBuilder);
		IConfigSchema reloadedSchema = reloadedSchemaBuilder.build();
		assertEquals(expected, getConfiguredValue(reloadedSchema));
	}

	@Test
	public void keepsExistingSettingsAndAllowsAnExplicitMigrationRetry(@TempDir Path tempDir) throws IOException {
		Path legacyFile = tempDir.resolve("jiv-mod-id-format.ini");
		Path currentFile = tempDir.resolve("client/jiv-mod-id-format.ini");
		Files.createDirectories(currentFile.getParent());
		String disabledFormatting = "[modName]\nmodNameFormat =\n";
		Files.writeString(legacyFile, disabledFormatting);
		Files.writeString(currentFile, "[modName]\nmodNameFormat = [\"GREEN\"]\n");

		assertEquals(List.of(ChatFormatting.GREEN), loadWithMigration(currentFile, legacyFile));
		assertTrue(Files.notExists(ConfigFileUtil.getBackupPath(legacyFile, 1)));
		assertEquals(disabledFormatting, Files.readString(legacyFile));

		// A user can preserve the current config and explicitly retry the legacy import.
		Path previousConfig = currentFile.resolveSibling("jiv-mod-id-format.ini.before-reimport");
		Files.move(currentFile, previousConfig);
		assertEquals(List.of(), loadWithMigration(currentFile, legacyFile));
		assertTrue(Files.exists(previousConfig));
		assertEquals(disabledFormatting, Files.readString(ConfigFileUtil.getBackupPath(legacyFile, 1)));
		assertEquals(List.of(), loadWithMigration(currentFile, legacyFile));
	}

	private static Object loadWithMigration(Path currentFile, Path legacyFile) {
		ConfigFileWatcherSettings disabledWatcher = ConfigFileWatcherSettings.clientDefaults().withEnabled(false);
		ConfigManager manager = new ConfigManager("JIV Mod Name Migration Retry Test", disabledWatcher, disabledWatcher);
		ConfigSchemaBuilder builder = new ConfigSchemaBuilder("jiv", currentFile, "jiv.config.modIdFormat", manager);
		new ModIdFormatConfig(builder, List.of(legacyFile));
		return getConfiguredValue(builder.build());
	}

	private static Object getConfiguredValue(IConfigSchema schema) {
		return schema.getCategories().getFirst().getConfigValues().getFirst().get();
	}
}
