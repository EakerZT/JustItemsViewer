package eakerzt.jiv.config.gui.entries;

import eakerzt.jiv.config.gui.textures.ConfigButtonIcon;
import eakerzt.jiv.config.gui.util.ImmutableRect2i;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Renders the reset-to-default icon used in config value rows.
 */
final class ConfigResetIcon {
	private ConfigResetIcon() {

	}

	public static void draw(GuiGraphicsExtractor guiGraphics, ImmutableRect2i buttonArea, boolean active) {
		ConfigButtonIcon.RESET.draw(guiGraphics, buttonArea, active);
	}
}
