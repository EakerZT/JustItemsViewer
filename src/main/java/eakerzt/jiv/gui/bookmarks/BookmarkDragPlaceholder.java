package eakerzt.jiv.gui.bookmarks;

import eakerzt.jiv.api.gui.drawable.IDrawable;
import eakerzt.jiv.api.ingredients.*;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.runtime.IRecipesGui;
import eakerzt.jiv.common.gui.JivTooltip;
import eakerzt.jiv.gui.overlay.elements.IElement;
import eakerzt.jiv.gui.overlay.ingredients.IngredientGridTooltipHelper;
import eakerzt.jiv.gui.util.FocusUtil;

import java.util.List;
import java.util.Optional;

/** Keeps the dragged cell's grid footprint while its ingredient follows the mouse. */
public record BookmarkDragPlaceholder<T>(IElement<T> source) implements IElement<T> {
	public ITypedIngredient<T> getTypedIngredient() {
		return source.getTypedIngredient();
	}

	public Optional<IBookmark> getBookmark() {
		return source.getBookmark();
	}

	public boolean isDragPlaceholder() {
		return true;
	}

	public boolean isVisible() {
		return true;
	}

	public IDrawable createRenderOverlay() {
		return null;
	}

	public void show(IRecipesGui gui, FocusUtil focus, List<RecipeIngredientRole> roles) {}

	public void getTooltip(
			JivTooltip tooltip,
			IngredientGridTooltipHelper helper,
			IIngredientRenderer<T> renderer,
			IIngredientHelper<T> ingredientHelper) {}

	public void tick() {}
}
