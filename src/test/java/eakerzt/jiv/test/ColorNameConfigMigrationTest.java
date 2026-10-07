package eakerzt.jiv.test;

import eakerzt.jiv.library.config.ColorNameConfig;
import eakerzt.jiv.common.config.legacy.LegacyConfigPaths;
import eakerzt.jiv.config.file.ConfigFileWatcherSettings;
import eakerzt.jiv.config.file.ConfigFileUtil;
import eakerzt.jiv.config.file.ConfigManager;
import eakerzt.jiv.config.schema.ConfigSchemaBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ColorNameConfigMigrationTest {
	@ParameterizedTest
	@ValueSource(strings = {
		"Exact:123456, Invalid:not-a-color, Other:654321",
		"[Exact:123456, Invalid:not-a-color, Other:654321]"
	})
	public void migratesValidLegacyColorsWhenOneIsInvalid(String legacyColors, @TempDir Path tempDir) throws IOException {
		Path configDirectory = tempDir.resolve("jiv");
		UUID profileId = UUID.randomUUID();
		Path rootLegacyFile = configDirectory.resolve("jiv-colors.ini");
		Path legacyFile = configDirectory.resolve("players").resolve(profileId.toString()).resolve("jiv-colors.ini");
		Path mezzConfigFile = configDirectory.resolve("client").resolve("jiv-colors.ini");
		Path reloadedConfigFile = configDirectory.resolve("reloaded-jiv-colors.ini");
		Files.createDirectories(legacyFile.getParent());
		Files.writeString(rootLegacyFile, """
			[colors]
			searchColors = Root:abcdef
			""");
		Files.writeString(legacyFile, "[colors]\nsearchColors = " + legacyColors + "\n");

		ConfigFileWatcherSettings disabledWatcher = ConfigFileWatcherSettings.clientDefaults().withEnabled(false);
		ConfigManager configManager = new ConfigManager("JIV Color Config Migration Test", disabledWatcher, disabledWatcher);
		ConfigSchemaBuilder schemaBuilder = new ConfigSchemaBuilder("jiv", mezzConfigFile, "jiv.config.colors", configManager);
		ColorNameConfig colorNameConfig = new ColorNameConfig(
			schemaBuilder,
			LegacyConfigPaths.get(configDirectory, profileId, "jiv-colors.ini")
		);
		schemaBuilder.build();

		assertEquals("Exact", colorNameConfig.getClosestColorName(0x123456));
		assertTrue(Files.exists(legacyFile));
		assertTrue(Files.exists(mezzConfigFile));
		assertEquals(Files.readString(legacyFile), Files.readString(ConfigFileUtil.getBackupPath(legacyFile, 1)));
		assertTrue(Files.notExists(ConfigFileUtil.getBackupPath(rootLegacyFile, 1)));
		Files.copy(mezzConfigFile, reloadedConfigFile, StandardCopyOption.REPLACE_EXISTING);

		ConfigManager reloadedConfigManager = new ConfigManager("Reloaded JIV Color Config", disabledWatcher, disabledWatcher);
		ConfigSchemaBuilder reloadedSchemaBuilder = new ConfigSchemaBuilder("jiv", reloadedConfigFile, "jiv.config.colors", reloadedConfigManager);
		ColorNameConfig reloaded = new ColorNameConfig(reloadedSchemaBuilder);
		reloadedSchemaBuilder.build();
		assertEquals("Exact", reloaded.getClosestColorName(0x123456));
	}

	@ParameterizedTest
	@ValueSource(strings = {"Invalid:not-a-color", "[Exact:123456"})
	public void invalidLegacyColorsLeaveTheDestinationAbsent(String invalidColors, @TempDir Path tempDir) throws IOException {
		Path configDirectory = tempDir.resolve("jiv");
		Path legacyFile = configDirectory.resolve("jiv-colors.ini");
		Path mezzConfigFile = configDirectory.resolve("client").resolve("jiv-colors.ini");
		Files.createDirectories(configDirectory);
		Files.writeString(legacyFile, "[colors]\nsearchColors = " + invalidColors + "\n");

		ConfigFileWatcherSettings disabledWatcher = ConfigFileWatcherSettings.clientDefaults().withEnabled(false);
		ConfigManager configManager = new ConfigManager("Invalid JIV Color Config Migration Test", disabledWatcher, disabledWatcher);
		ConfigSchemaBuilder schemaBuilder = new ConfigSchemaBuilder("jiv", mezzConfigFile, "jiv.config.colors", configManager);
		ColorNameConfig colorNameConfig = new ColorNameConfig(
			schemaBuilder,
			LegacyConfigPaths.get(configDirectory, UUID.randomUUID(), "jiv-colors.ini")
		);

		schemaBuilder.build();

		assertEquals("White", colorNameConfig.getClosestColorName(0xEEEEEE));
		assertTrue(Files.notExists(mezzConfigFile));
		assertEquals(Files.readString(legacyFile), Files.readString(ConfigFileUtil.getBackupPath(legacyFile, 1)));

		// Fixing the source must allow migration on the next startup.
		Files.writeString(legacyFile, "[colors]\nsearchColors = Fixed:123456\n");
		ConfigManager retryManager = new ConfigManager("Retried JIV Color Config", disabledWatcher, disabledWatcher);
		ConfigSchemaBuilder retryBuilder = new ConfigSchemaBuilder("jiv", mezzConfigFile, "jiv.config.colors", retryManager);
		ColorNameConfig retried = new ColorNameConfig(retryBuilder, List.of(legacyFile));
		retryBuilder.build();
		assertEquals("Fixed", retried.getClosestColorName(0x123456));
		assertTrue(Files.exists(mezzConfigFile));
	}

}
