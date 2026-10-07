package eakerzt.jiv.api.recipe.category.extensions.vanilla.brewing;

import eakerzt.jiv.api.constants.RecipeTypes;
import eakerzt.jiv.api.recipe.vanilla.IJivBrewingRecipe;
import eakerzt.jiv.api.recipe.vanilla.IVanillaRecipeFactory;
import eakerzt.jiv.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.util.context.ContextMap;

import java.util.List;

/**
 * Implement this interface to convert a custom platform brewing recipe into recipes
 * that can be displayed as part of {@link RecipeTypes#BREWING}.
 *
 * <p>
 * Register this extension by getting the extendable brewing category from
 * {@link IVanillaCategoryExtensionRegistration#getBrewingCategory()}
 * and then registering it with
 * {@link IExtendableBrewingRecipeCategory#addExtension(Class, IBrewingCategoryExtension)}.
 * </p>
 *
 * @param <R> the custom platform brewing recipe type
 * @since 29.22.0
 */
@FunctionalInterface
public interface IBrewingCategoryExtension<R> {
	/**
	 * Convert a custom platform brewing recipe into JIV brewing recipes (or none).
	 *
	 * <p>
	 * A single platform recipe may return multiple JIV recipes when its output depends on the input,
	 * such as a recipe that preserves regular, splash, and lingering potion containers.
	 * Return an empty list when the platform recipe should not be displayed.
	 * The returned list and its elements must not be null.
	 * Each returned recipe must have a UID that uniquely identifies its displayed inputs, ingredients, and output.
	 * </p>
	 *
	 * @param recipe the custom platform brewing recipe
	 * @param vanillaRecipeFactory factory for creating JIV brewing recipes
	 * @param contextMap context for resolving recipe displays
	 * @return the JIV brewing recipes represented by the custom platform recipe
	 * @since 29.22.0
	 */
	List<IJivBrewingRecipe> getBrewingRecipes(
		R recipe,
		IVanillaRecipeFactory vanillaRecipeFactory,
		ContextMap contextMap
	);
}
