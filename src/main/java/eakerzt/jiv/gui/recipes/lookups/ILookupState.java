package eakerzt.jiv.gui.recipes.lookups;

import eakerzt.jiv.api.recipe.IFocusGroup;
import eakerzt.jiv.api.recipe.category.IRecipeCategory;
import eakerzt.jiv.gui.recipes.IRecipeLayoutWithButtons;
import eakerzt.jiv.gui.recipes.layouts.IRecipeLayoutList;

import java.util.List;

public interface ILookupState {
	List<IRecipeCategory<?>> getRecipeCategories();

	boolean moveToRecipeCategory(IRecipeCategory<?> recipeCategory);

	int getRecipesPerPage();

	void setRecipesPerPage(int recipesPerPage);

	int getRecipeIndex();

	IFocusGroup getFocuses();

	IFocusedRecipes<?> getFocusedRecipes();

	boolean nextRecipeCategory();

	boolean previousRecipeCategory();

	void goToFirstPage();

	boolean nextPage();

	boolean previousPage();

	int pageCount();

	default List<IRecipeLayoutWithButtons<?>> getVisible(IRecipeLayoutList recipes) {
		final int recipesPerPage = getRecipesPerPage();
		final int firstRecipeIndex = getRecipeIndex() - (getRecipeIndex() % recipesPerPage);
		final int maxIndex = Math.min(recipes.size(), firstRecipeIndex + recipesPerPage);
		if (firstRecipeIndex >= maxIndex) {
			return List.of();
		}
		return recipes.subList(firstRecipeIndex, maxIndex);
	}
}
