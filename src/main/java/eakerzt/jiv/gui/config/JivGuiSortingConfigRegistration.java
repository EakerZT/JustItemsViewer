package eakerzt.jiv.gui.config;

import eakerzt.jiv.api.constants.ModIds;
import eakerzt.jiv.common.platform.Services;
import eakerzt.jiv.config.api.Configs;
import eakerzt.jiv.config.api.IConfigRegistration;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.UUID;

public final class JivGuiSortingConfigRegistration {
	@Nullable
	private static JivGuiSortingConfigData sortingConfigData;

	private JivGuiSortingConfigRegistration() {

	}

	public static JivGuiSortingConfigData get() {
		if (sortingConfigData == null) {
			sortingConfigData = register();
		}
		return sortingConfigData;
	}

	private static JivGuiSortingConfigData register() {
		IConfigRegistration registration = Configs.forMod(ModIds.JIV_ID);
		Path jivConfigDirectory = Services.PLATFORM.getConfigHelper().createJivConfigDir();
		Minecraft minecraft = Minecraft.getInstance();
		@Nullable
		UUID profileId = null;
		if (minecraft != null) {
			profileId = minecraft.getUser().getProfileId();
		}
		return new JivGuiSortingConfigData(
			ModNameSortingConfig.create(registration, jivConfigDirectory, profileId),
			IngredientTypeSortingConfig.create(registration, jivConfigDirectory, profileId)
		);
	}
}
