package eakerzt.jiv.config.gui.jiv;

import eakerzt.jiv.api.IModPlugin;
import eakerzt.jiv.api.JivPlugin;
import eakerzt.jiv.api.gui.handlers.IGuiProperties;
import eakerzt.jiv.api.registration.IGuiHandlerRegistration;
import eakerzt.jiv.config.gui.ConfigScreen;
import eakerzt.jiv.config.gui.MezzConfigScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * Optional JIV integration for the config screen.
 */
@JivPlugin
public class ConfigGuiPlugin implements IModPlugin {
	private static final String CONFIG_GUI_MOD_ID = "jiv";

	@Override
	public Identifier getPluginUid() {
		return Identifier.fromNamespaceAndPath(CONFIG_GUI_MOD_ID, "config_gui");
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addGuiScreenHandler(MezzConfigScreen.class, ConfigGuiPlugin::getProperties);
		registration.addGlobalGuiHandler(new ConfigScreenGuiHandler());
	}

	@Nullable
	private static IGuiProperties getProperties(MezzConfigScreen screen) {
		if (ConfigScreen.isCapturingKeyBinding(screen)) {
			return null;
		}
		if (screen.width <= 0 || screen.height <= 0) {
			return null;
		}
		Rect2i area = screen.getScreenArea();
		if (area == null || area.getWidth() <= 0 || area.getHeight() <= 0) {
			return null;
		}
		return new ConfigGuiProperties(
			screen.getClass(),
			area.getX(),
			area.getY(),
			area.getWidth(),
			area.getHeight(),
			screen.width,
			screen.height
		);
	}

	private record ConfigGuiProperties(
		Class<? extends Screen> screenClass,
		int guiLeft,
		int guiTop,
		int guiXSize,
		int guiYSize,
		int screenWidth,
		int screenHeight
	) implements IGuiProperties {

	}
}
