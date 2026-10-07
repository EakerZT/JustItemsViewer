package eakerzt.jiv.library.config;

import eakerzt.jiv.api.constants.ModIds;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.config.ClientConfigs;
import eakerzt.jiv.common.config.DebugConfig;
import eakerzt.jiv.common.config.legacy.LegacyConfigPaths;
import eakerzt.jiv.common.platform.Services;
import eakerzt.jiv.config.api.Configs;
import eakerzt.jiv.config.api.IConfigRegistration;
import eakerzt.jiv.config.api.schema.builder.IConfigSchemaBuilder;
import eakerzt.jiv.config.api.sorting.ISortingConfig;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.UUID;

public final class JivConfigRegistration {
	private JivConfigRegistration() {

	}

	public static JivConfigData register() {
		Path jivConfigDirectory = Services.PLATFORM.getConfigHelper().createJivConfigDir();
		Minecraft minecraft = Minecraft.getInstance();
		@Nullable
		UUID profileId = null;
		if (minecraft != null) {
			profileId = minecraft.getUser().getProfileId();
		}

		IConfigRegistration registration = Configs.forMod(ModIds.JIV_ID);
		IConfigSchemaBuilder debugFileBuilder = registration.createClientSchemaBuilder("jiv-debug.ini", "jiv.config.debug");
		DebugConfig.create(debugFileBuilder);
		debugFileBuilder.setLegacySources(LegacyConfigPaths.get(jivConfigDirectory, profileId, "jiv-debug.ini"));
		debugFileBuilder.build();

		IConfigSchemaBuilder modFileBuilder = registration.createClientSchemaBuilder("jiv-mod-id-format.ini", "jiv.config.modIdFormat");
		var legacyModNameFormatPaths = LegacyConfigPaths.get(jivConfigDirectory, profileId, "jiv-mod-id-format.ini");
		ModIdFormatConfig modIdFormatConfig = new ModIdFormatConfig(modFileBuilder, legacyModNameFormatPaths);
		modFileBuilder.build();

		IConfigSchemaBuilder colorFileBuilder = registration.createClientSchemaBuilder("jiv-colors.ini", "jiv.config.colors");
		var legacyColorPaths = LegacyConfigPaths.get(jivConfigDirectory, profileId, "jiv-colors.ini");
		ColorNameConfig colorNameConfig = new ColorNameConfig(colorFileBuilder, legacyColorPaths);
		colorFileBuilder.build();

		boolean isDev = Services.PLATFORM.getModHelper().isInDev();
		ISortingConfig<String> recipeCategorySortingConfig = RecipeCategorySortingConfig.create(registration, jivConfigDirectory, profileId);
		String localizationPath = "jiv.config.client";
		IConfigSchemaBuilder clientFileBuilder = registration.createClientSchemaBuilder("jiv-client.ini", localizationPath);
		clientFileBuilder.setLegacySources(LegacyConfigPaths.get(jivConfigDirectory, profileId, "jiv-client.ini"));
		ClientConfigs clientConfigs = new ClientConfigs(
			clientFileBuilder,
			isDev,
			recipeCategorySortingConfig
		);

		Internal.setClientConfigs(clientConfigs);
		return new JivConfigData(modIdFormatConfig, colorNameConfig, clientConfigs);
	}
}
