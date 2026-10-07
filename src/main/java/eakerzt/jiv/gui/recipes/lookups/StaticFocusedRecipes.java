package eakerzt.jiv.gui.recipes.lookups;

import eakerzt.jiv.api.recipe.category.IRecipeCategory;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public record StaticFocusedRecipes<T>(
	IRecipeCategory<T> recipeCategory,
	List<T> recipes
) implements IFocusedRecipes<T> {
	@Override
	public IRecipeCategory<T> getRecipeCategory() {
		return recipeCategory;
	}

	@Override
	public @Unmodifiable List<T> getRecipes() {
		return recipes;
	}
}
