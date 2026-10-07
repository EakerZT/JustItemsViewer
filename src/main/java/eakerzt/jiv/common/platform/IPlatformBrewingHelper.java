package eakerzt.jiv.common.platform;

import eakerzt.jiv.api.recipe.category.extensions.vanilla.brewing.IExtendableBrewingRecipeCategory;
import eakerzt.jiv.api.recipe.vanilla.IJivBrewingRecipe;
import eakerzt.jiv.api.recipe.vanilla.IVanillaRecipeFactory;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.recipes.BrewingExtensionHelper;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.alchemy.PotionBrewing;

import java.util.List;

public interface IPlatformBrewingHelper {
	/**
	 * Register handlers for platform brewing recipe objects.
	 * Platforms that expose brewing mixtures directly have no extensions to register.
	 */
	default void registerCategoryExtensions(
		IExtendableBrewingRecipeCategory brewingCategory,
		IIngredientManager ingredientManager
	) {
	}

	/**
	 * Discover the platform's brewing recipes and convert them for JIV.
	 */
	List<IJivBrewingRecipe> getBrewingRecipes(
		IIngredientManager ingredientManager,
		IVanillaRecipeFactory vanillaRecipeFactory,
		PotionBrewing potionBrewing,
		ContextMap contextMap,
		BrewingExtensionHelper brewingExtensionHelper
	);
}
