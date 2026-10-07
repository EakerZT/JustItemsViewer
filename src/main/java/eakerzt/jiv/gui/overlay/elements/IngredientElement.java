package eakerzt.jiv.gui.overlay.elements;

import eakerzt.jiv.api.gui.drawable.IDrawable;
import eakerzt.jiv.api.ingredients.IIngredientHelper;
import eakerzt.jiv.api.ingredients.IIngredientRenderer;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.IFocus;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.runtime.IRecipesGui;
import eakerzt.jiv.common.gui.JivTooltip;
import eakerzt.jiv.gui.bookmarks.IBookmark;
import eakerzt.jiv.gui.overlay.ingredients.IngredientGridTooltipHelper;
import eakerzt.jiv.gui.util.FocusUtil;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class IngredientElement<T> implements IElement<T> {
	private final ITypedIngredient<T> ingredient;

	public IngredientElement(ITypedIngredient<T> ingredient) {
		this.ingredient = ingredient;
	}

	@Override
	public ITypedIngredient<T> getTypedIngredient() {
		return ingredient;
	}

	@Override
	public Optional<IBookmark> getBookmark() {
		return Optional.empty();
	}

	@Override
	public @Nullable IDrawable createRenderOverlay() {
		return null;
	}

	@Override
	public void show(IRecipesGui recipesGui, FocusUtil focusUtil, List<RecipeIngredientRole> roles) {
		ITypedIngredient<?> ingredient = getTypedIngredient();
		List<IFocus<?>> focuses = focusUtil.createFocuses(ingredient, roles);
		recipesGui.show(focuses);
	}

	@Override
	public void getTooltip(JivTooltip tooltip, IngredientGridTooltipHelper tooltipHelper, IIngredientRenderer<T> ingredientRenderer, IIngredientHelper<T> ingredientHelper) {
		tooltipHelper.getIngredientTooltip(tooltip, ingredient, ingredientRenderer, ingredientHelper);
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public void tick() {

	}
}
