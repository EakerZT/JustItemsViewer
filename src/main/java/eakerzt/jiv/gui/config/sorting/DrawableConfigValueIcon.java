package eakerzt.jiv.gui.config.sorting;

import eakerzt.jiv.api.gui.drawable.IDrawable;
import eakerzt.jiv.config.gui.api.IConfigValueIcon;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;

record DrawableConfigValueIcon(IDrawable drawable) implements IConfigValueIcon {
	@Override
	public void draw(GuiGraphicsExtractor guiGraphics, Rect2i area) {
		int x = area.getX() + Math.round((area.getWidth() - drawable.getWidth()) / 2.0f);
		int y = area.getY() + Math.round((area.getHeight() - drawable.getHeight()) / 2.0f);
		drawable.draw(guiGraphics, x, y);
	}
}
