package eakerzt.jiv.api.registration;

import eakerzt.jiv.api.IModPlugin;
import eakerzt.jiv.api.helpers.IJivHelpers;
import eakerzt.jiv.api.recipe.category.IRecipeCategory;
import org.jetbrains.annotations.ApiStatus;

/**
 * This is given to your {@link IModPlugin#registerCategories(IRecipeCategoryRegistration)}.
 */
@ApiStatus.NonExtendable
public interface IRecipeCategoryRegistration {
	/**
	 * {@link IJivHelpers} provides helpers and tools for addon mods.
	 */
	IJivHelpers getJivHelpers();

	/**
	 * Add the recipe categories provided by this plugin.
	 */
	void addRecipeCategories(IRecipeCategory<?>... recipeCategories);
}
