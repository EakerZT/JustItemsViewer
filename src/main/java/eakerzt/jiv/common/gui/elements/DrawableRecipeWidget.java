package eakerzt.jiv.common.gui.elements;

import eakerzt.jiv.api.gui.drawable.IDrawable;
import eakerzt.jiv.api.gui.widgets.IDrawableWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class DrawableRecipeWidget extends AbstractRecipeWidgetBuilder<IDrawableWidget> implements IDrawableWidget {
	private final IDrawable drawable;

	public DrawableRecipeWidget(IDrawable drawable) {
		super(0, 0);
		this.drawable = drawable;
	}

	@Override
	protected IDrawableWidget getThis() {
		return this;
	}

	@Override
	public int getWidth() {
		return drawable.getWidth();
	}

	@Override
	public int getHeight() {
		return drawable.getHeight();
	}

	@Override
	public void drawWidget(GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
		drawable.draw(guiGraphics);
	}
}
