package eakerzt.jiv.config.file;

import eakerzt.jiv.config.api.schema.ConfigSchemaType;
import eakerzt.jiv.config.api.value.editor.ConfigValueRestartRequirement;
import eakerzt.jiv.config.schema.ConfigCategoryBuilder;
import eakerzt.jiv.config.schema.ConfigSchema;
import eakerzt.jiv.config.schema.LayeredConfigSchemaPathResolver;
import eakerzt.jiv.config.schema.StaticConfigSchemaPathResolver;
import eakerzt.jiv.config.util.ErrorUtil;
import eakerzt.jiv.config.value.ConfigValue;
import eakerzt.jiv.config.internal.scheduler.DelayedExecutor;
import eakerzt.jiv.config.internal.scheduler.DelayedTaskScheduler;
import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

@ApiStatus.Internal
public final class MezzConfigSettings {
	private static final String MOD_ID = "jiv";
	private static final String CONFIG_FILE_NAME = "settings.ini";
	private static final String LOCALIZATION_PATH = "jiv_config.config";

	private MezzConfigSettings() {

	}

	public static ConfigManager createManager(
		String fileWatcherThreadName,
		Path configRootDir,
		boolean developmentEnvironment
	) {
		configRootDir = ErrorUtil.checkNotNull(configRootDir, "configRootDir")
			.toAbsolutePath()
			.normalize();
		DelayedExecutor saveExecutor = ConfigManager.createSaveExecutor();
		SchemaData startupSettings = createSchema(configRootDir, developmentEnvironment, saveExecutor);
		startupSettings.schema().loadIfNeeded();
		ConfigManager configManager = new ConfigManager(
			fileWatcherThreadName,
			startupSettings.fileWatcherSettings(),
			ConfigFileWatcherSettings.serverDefaults(),
			startupSettings.logUntranslatedKeys(),
			saveExecutor
		);
		ConfigSchema schema = createSchema(configRootDir, developmentEnvironment, saveExecutor).schema();
		configManager.registerSchema(schema);
		return configManager;
	}

	private static SchemaData createSchema(
		Path configRootDir,
		boolean developmentEnvironment,
		DelayedTaskScheduler saveScheduler
	) {
		saveScheduler = ErrorUtil.checkNotNull(saveScheduler, "saveScheduler");
		ConfigFileWatcherSettings defaults = ConfigFileWatcherSettings.clientDefaults();
		ConfigCategoryBuilder fileWatcher = new ConfigCategoryBuilder(LOCALIZATION_PATH, "fileWatcher");
		ConfigValue<Boolean> enabled = fileWatcher.addBoolean("enabled", defaults.enabled())
			.setRestartRequirement(ConfigValueRestartRequirement.GAME_RESTART)
			.build();
		ConfigValue<Long> settlingDelay = fileWatcher.addLong(
				"changeSettlingDelayMilliseconds",
				defaults.changeSettlingDelay().toMillis(),
				1L,
				Long.MAX_VALUE
			)
			.setRestartRequirement(ConfigValueRestartRequirement.GAME_RESTART)
			.build();
		ConfigValue<Long> retryInterval = fileWatcher.addLong(
				"missingDirectoryRetryIntervalMilliseconds",
				defaults.missingDirectoryRetryInterval().toMillis(),
				1L,
				Long.MAX_VALUE
			)
			.setRestartRequirement(ConfigValueRestartRequirement.GAME_RESTART)
			.build();
		ConfigCategoryBuilder logging = new ConfigCategoryBuilder(LOCALIZATION_PATH, "logging");
		ConfigValue<Boolean> logUntranslatedKeys = logging.addBoolean("logUntranslatedKeys", developmentEnvironment)
			.setRestartRequirement(ConfigValueRestartRequirement.GAME_RESTART)
			.build();

		Path clientConfigDirectory = configRootDir.resolve(MOD_ID).resolve("client");
		ConfigSchema schema = new ConfigSchema(
			CONFIG_FILE_NAME,
			MOD_ID,
			new LayeredConfigSchemaPathResolver(
				clientConfigDirectory.resolve("default").resolve(CONFIG_FILE_NAME),
				new StaticConfigSchemaPathResolver(clientConfigDirectory.resolve(CONFIG_FILE_NAME))
			),
			List.of(fileWatcher, logging),
			List.of(fileWatcher, logging),
			saveScheduler,
			ConfigSchemaType.CLIENT,
			null
		);
		return new SchemaData(
			schema,
			enabled,
			settlingDelay,
			retryInterval,
			logUntranslatedKeys
		);
	}

	private record SchemaData(
		ConfigSchema schema,
		ConfigValue<Boolean> enabled,
		ConfigValue<Long> settlingDelay,
		ConfigValue<Long> retryInterval,
		ConfigValue<Boolean> logUntranslatedKeysValue
	) {
		private ConfigFileWatcherSettings fileWatcherSettings() {
			return new ConfigFileWatcherSettings(
				enabled.get(),
				Duration.ofMillis(settlingDelay.get()),
				Duration.ofMillis(retryInterval.get())
			);
		}

		private boolean logUntranslatedKeys() {
			return logUntranslatedKeysValue.get();
		}
	}
}
