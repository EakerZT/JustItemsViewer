package eakerzt.jiv.library.load.registration;

import eakerzt.jiv.api.recipe.IFocus;
import eakerzt.jiv.api.recipe.advanced.IRecipeManagerPluginHelper;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import eakerzt.jiv.library.recipes.RecipeManagerInternal;

public class RecipeManagerPluginHelper implements IRecipeManagerPluginHelper {
	private final RecipeManagerInternal recipeManager;

	public RecipeManagerPluginHelper(RecipeManagerInternal recipeManager) {
		this.recipeManager = recipeManager;
	}

	@Override
	public boolean isCraftingStation(IRecipeType<?> recipeType, IFocus<?> focus) {
		return recipeManager.isCraftingStation(recipeType, focus);
	}
}
