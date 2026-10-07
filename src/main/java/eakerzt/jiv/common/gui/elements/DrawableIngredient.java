package eakerzt.jiv.common.gui.elements;

import eakerzt.jiv.api.gui.drawable.IDrawable;
import eakerzt.jiv.api.ingredients.IIngredientRenderer;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.common.util.SafeIngredientUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class DrawableIngredient<V> implements IDrawable {
	private final ITypedIngredient<V> typedIngredient;
	private final IIngredientRenderer<V> ingredientRenderer;

	public DrawableIngredient(ITypedIngredient<V> typedIngredient, IIngredientRenderer<V> ingredientRenderer) {
		this.typedIngredient = typedIngredient;
		this.ingredientRenderer = ingredientRenderer;
	}

	@Override
	public int getWidth() {
		return this.ingredientRenderer.getWidth();
	}

	@Override
	public int getHeight() {
		return this.ingredientRenderer.getHeight();
	}

	@Override
	public void draw(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset) {
		SafeIngredientUtil.render(guiGraphics, ingredientRenderer, typedIngredient, xOffset, yOffset);
	}
}
