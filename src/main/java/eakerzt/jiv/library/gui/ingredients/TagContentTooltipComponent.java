package eakerzt.jiv.library.gui.ingredients;

import eakerzt.jiv.api.ingredients.IIngredientRenderer;
import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.gui.IngredientGridTooltipComponent;
import eakerzt.jiv.common.util.SafeIngredientUtil;
import eakerzt.jiv.common.util.LazyMappedList;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

public class TagContentTooltipComponent extends IngredientGridTooltipComponent<ITypedIngredient<?>> {
	private final IIngredientManager ingredientManager;

	public TagContentTooltipComponent(IIngredientManager ingredientManager, List<ITypedIngredient<?>> ingredients) {
		super(new LazyMappedList<>(ingredients, ingredientManager::normalizeTypedIngredient, MAX_VISIBLE_INGREDIENTS));
		this.ingredientManager = ingredientManager;
	}

	@Override
	protected void drawIngredient(
		GuiGraphicsExtractor guiGraphics,
		ITypedIngredient<?> ingredient,
		int index,
		int x,
		int y,
		boolean hovered
	) {
		drawIngredient(guiGraphics, ingredient, x, y);
	}

	private <T> void drawIngredient(
		GuiGraphicsExtractor guiGraphics,
		ITypedIngredient<T> ingredient,
		int x,
		int y
	) {
		IIngredientType<T> ingredientType = ingredient.getType();
		IIngredientRenderer<T> renderer = this.ingredientManager.getIngredientRenderer(ingredientType);
		SafeIngredientUtil.render(guiGraphics, renderer, ingredient, x, y);
	}
}
