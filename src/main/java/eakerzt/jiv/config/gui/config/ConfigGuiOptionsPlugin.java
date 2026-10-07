package eakerzt.jiv.config.gui.config;

import eakerzt.jiv.config.gui.api.ConfigGuiPlugin;
import eakerzt.jiv.config.gui.api.IConfigGuiPlugin;
import eakerzt.jiv.config.gui.api.IConfigGuiRegistration;

/**
 * Registers MezzConfig GUI's own user-facing options.
 */
@ConfigGuiPlugin
public final class ConfigGuiOptionsPlugin implements IConfigGuiPlugin {
	@Override
	public String getModId() {
		return ConfigGuiOptions.MOD_ID;
	}

	@Override
	public void register(IConfigGuiRegistration registration) {
		registration.configureScreen(screen -> ConfigGuiOptions.getModNavigationConfig().configureCategory(screen.configureCategory("modList")));
	}
}
