package eakerzt.jiv.common.platform;

import eakerzt.jiv.api.constants.ModIds;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public interface IPlatformConfigHelper {
	Path getModConfigDir();

	Optional<Screen> getConfigScreen(String modId, @Nullable Screen parent);

	default Path createJivConfigDir() {
		Path configDir = getModConfigDir()
			.resolve(ModIds.JIV_ID);

		try {
			Files.createDirectories(configDir);
		} catch (IOException e) {
			throw new RuntimeException("Unable to create JIV config directory: " + configDir, e);
		}
		return configDir;
	}
}
