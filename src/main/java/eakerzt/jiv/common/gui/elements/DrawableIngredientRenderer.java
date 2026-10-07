package eakerzt.jiv.common.gui.elements;

import eakerzt.jiv.api.gui.drawable.IDrawable;
import eakerzt.jiv.api.ingredients.IIngredientRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class DrawableIngredientRenderer<T> implements IDrawable {
	private final IIngredientRenderer<T> ingredientRenderer;
	private final T ingredient;

	public DrawableIngredientRenderer(IIngredientRenderer<T> ingredientRenderer, T ingredient) {
		this.ingredientRenderer = ingredientRenderer;
		this.ingredient = ingredient;
	}

	@Override
	public int getWidth() {
		return ingredientRenderer.getWidth();
	}

	@Override
	public int getHeight() {
		return ingredientRenderer.getHeight();
	}

	@Override
	public void draw(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset) {
		ingredientRenderer.render(guiGraphics, ingredient, xOffset, yOffset);
	}
}
