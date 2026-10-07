package eakerzt.jiv.library.recipes;

import eakerzt.jiv.api.recipe.IFocus;
import eakerzt.jiv.api.recipe.IFocusGroup;
import eakerzt.jiv.api.recipe.IRecipeLookup;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.library.focus.FocusGroup;

import java.util.Collection;
import java.util.stream.Stream;

public class RecipeLookup<R> implements IRecipeLookup<R> {
	private final IRecipeType<R> recipeType;
	private final RecipeManagerInternal recipeManager;
	private final IIngredientManager ingredientManager;

	private boolean includeHidden = false;
	private IFocusGroup focusGroup = FocusGroup.EMPTY;

	public RecipeLookup(IRecipeType<R> recipeType, RecipeManagerInternal recipeManager, IIngredientManager ingredientManager) {
		this.recipeType = recipeType;
		this.recipeManager = recipeManager;
		this.ingredientManager = ingredientManager;
	}

	@Override
	public IRecipeLookup<R> limitFocus(Collection<? extends IFocus<?>> focuses) {
		this.focusGroup = FocusGroup.create(focuses, ingredientManager);
		return this;
	}

	@Override
	public IRecipeLookup<R> includeHidden() {
		this.includeHidden = true;
		return this;
	}

	@Override
	public Stream<R> get() {
		return recipeManager.getRecipesStream(recipeType, focusGroup, includeHidden);
	}
}
