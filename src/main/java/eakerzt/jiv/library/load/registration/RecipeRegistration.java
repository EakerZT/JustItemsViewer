package eakerzt.jiv.library.load.registration;

import eakerzt.jiv.api.constants.RecipeTypes;
import eakerzt.jiv.api.helpers.IJivHelpers;
import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import eakerzt.jiv.api.recipe.vanilla.IJivIngredientInfoRecipe;
import eakerzt.jiv.api.recipe.vanilla.IVanillaRecipeFactory;
import eakerzt.jiv.api.registration.IRecipeRegistration;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.util.ErrorUtil;
import eakerzt.jiv.library.plugins.jiv.info.IngredientInfoRecipe;
import eakerzt.jiv.library.recipes.RecipeManagerInternal;
import net.minecraft.network.chat.Component;
import net.minecraft.util.context.ContextMap;

import java.util.List;

public class RecipeRegistration implements IRecipeRegistration {
	private final IJivHelpers jivHelpers;
	private final IIngredientManager ingredientManager;
	private final RecipeManagerInternal recipeManager;
	private final ContextMap contextMap;

	public RecipeRegistration(
		IJivHelpers jivHelpers,
		IIngredientManager ingredientManager,
		RecipeManagerInternal recipeManager,
		ContextMap contextMap
	) {
		this.jivHelpers = jivHelpers;
		this.ingredientManager = ingredientManager;
		this.recipeManager = recipeManager;
		this.contextMap = contextMap;
	}

	@Override
	public IJivHelpers getJivHelpers() {
		return jivHelpers;
	}

	@Override
	public IIngredientManager getIngredientManager() {
		return ingredientManager;
	}

	@Override
	public IVanillaRecipeFactory getVanillaRecipeFactory() {
		return jivHelpers.getVanillaRecipeFactory();
	}

	@Override
	public ContextMap getContextMap() {
		return contextMap;
	}

	@Override
	public <T> void addRecipes(IRecipeType<T> recipeType, List<T> recipes) {
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.checkNotNull(recipes, "recipes");
		this.recipeManager.addRecipes(recipeType, recipes, contextMap);
	}

	@Override
	public <T> void addIngredientInfo(T ingredient, IIngredientType<T> ingredientType, Component... descriptionComponents) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotEmpty(descriptionComponents, "descriptionComponents");

		addIngredientInfo(List.of(ingredient), ingredientType, descriptionComponents);
	}

	@Override
	public <T> void addIngredientInfo(List<T> ingredients, IIngredientType<T> ingredientType, Component... descriptionComponents) {
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotEmpty(descriptionComponents, "descriptionComponents");

		IJivIngredientInfoRecipe recipe = IngredientInfoRecipe.create(ingredientManager, ingredients, ingredientType, descriptionComponents);
		addRecipes(RecipeTypes.INFORMATION, List.of(recipe));
	}
}
