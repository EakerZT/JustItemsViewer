package eakerzt.jiv.api.ingredients.subtypes;

import eakerzt.jiv.api.gui.builder.IRecipeLayoutBuilder;
import eakerzt.jiv.api.recipe.IFocusGroup;
import eakerzt.jiv.api.recipe.category.IRecipeCategory;
import eakerzt.jiv.api.registration.IModIngredientRegistration;
import eakerzt.jiv.api.registration.IRecipeCatalystRegistration;
import eakerzt.jiv.api.registration.IRecipeTransferRegistration;
import eakerzt.jiv.api.runtime.IIngredientVisibility;

/**
 * Optional context used when getting Unique IDs for ingredients and subtypes.
 * Subtype interpreters can use this context to return different subtypes for recipes and ingredients.
 * When implementing this, {@link #Ingredient} subtypes should be more specific than {@link #Recipe} subtypes.
 *
 * @since 7.3.0
 */
public enum UidContext {
	/**
	 * Context used for comparing ingredients in the ingredient list.
	 * This is the main context and should be more specific than {@link #Recipe}.
	 *
	 * Used for:
	 * ingredients (see {@link IModIngredientRegistration}
	 * blacklists from the config
	 * debug info
	 * bookmarks
	 * ingredient visibility (see {@link IIngredientVisibility#hideIngredients})
	 */
	Ingredient,

	/**
	 * Context used for comparing ingredients in recipes.
	 * This is a secondary context and should be less specific than {@link #Ingredient}, to allow for broader matches in recipes.
	 *
	 * Used for:
	 * recipe lookups (see {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)})
	 * recipe catalysts (see {@link IRecipeCatalystRegistration})
	 * recipe transfer (since JIV 7.4.0) (see {@link IRecipeTransferRegistration}
	 * ingredient visibility in recipe slots and catalysts
	 * (see {@link IIngredientVisibility#hideIngredients})
	 */
	Recipe
}
