package eakerzt.jiv.library.gui.recipes;

import eakerzt.jiv.api.gui.drawable.IDrawable;
import eakerzt.jiv.common.gui.JivTooltip;
import eakerzt.jiv.common.util.ImmutableRect2i;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class ShapelessIcon {
	private final IDrawable icon;
	private final ImmutableRect2i area;

	public ShapelessIcon(IDrawable icon, int x, int y) {
		this.icon = icon;
		this.area = new ImmutableRect2i(x, y, icon.getWidth(), icon.getHeight());
	}

	public void draw(GuiGraphicsExtractor guiGraphics) {
		icon.draw(guiGraphics, area.getX(), area.getY());
	}

	public boolean isMouseOver(int mouseX, int mouseY) {
		return area.contains(mouseX, mouseY);
	}

	public void addTooltip(JivTooltip tooltip) {
		tooltip.add(Component.translatable("jiv.tooltip.shapeless.recipe"));
	}
}
