package eakerzt.jiv.gui.overlay.elements;

import eakerzt.jiv.api.gui.drawable.IDrawable;
import eakerzt.jiv.api.ingredients.IIngredientHelper;
import eakerzt.jiv.api.ingredients.IIngredientRenderer;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.runtime.IRecipesGui;
import eakerzt.jiv.common.gui.JivTooltip;
import eakerzt.jiv.common.input.IInternalKeyMappings;
import eakerzt.jiv.gui.bookmarks.IBookmark;
import eakerzt.jiv.common.input.UserInput;
import eakerzt.jiv.gui.overlay.ingredients.IngredientGridTooltipHelper;
import eakerzt.jiv.gui.util.FocusUtil;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface IElement<T> {
	ITypedIngredient<T> getTypedIngredient();

	/**
	 * @return the bookmark if this element represents an existing bookmark.
	 */
	Optional<IBookmark> getBookmark();

	@Nullable
	IDrawable createRenderOverlay();

	void show(IRecipesGui recipesGui, FocusUtil focusUtil, List<RecipeIngredientRole> roles);

	void getTooltip(JivTooltip tooltip, IngredientGridTooltipHelper tooltipHelper, IIngredientRenderer<T> ingredientRenderer, IIngredientHelper<T> ingredientHelper);

	boolean isVisible();

	void tick();

	default boolean handleClick(UserInput input, IInternalKeyMappings keyBindings) {
		return false;
	}
}
