package eakerzt.jiv.common.recipes;

import eakerzt.jiv.api.recipe.IFocusGroup;
import eakerzt.jiv.api.recipe.category.IRecipeCategory;

public interface IRecipeVisibility {
	<T> boolean isRecipeVisible(IRecipeCategory<T> recipeCategory, T recipe, IFocusGroup focuses);
}
