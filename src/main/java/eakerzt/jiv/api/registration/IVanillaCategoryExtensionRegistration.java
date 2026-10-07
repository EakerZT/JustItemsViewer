package eakerzt.jiv.api.registration;

import eakerzt.jiv.api.IModPlugin;
import eakerzt.jiv.api.helpers.IJivHelpers;
import eakerzt.jiv.api.recipe.category.extensions.vanilla.brewing.IExtendableBrewingRecipeCategory;
import eakerzt.jiv.api.recipe.category.extensions.vanilla.crafting.IExtendableCraftingRecipeCategory;
import eakerzt.jiv.api.recipe.category.extensions.vanilla.smithing.IExtendableSmithingRecipeCategory;
import org.jetbrains.annotations.ApiStatus;

/**
 * This allows you to register extensions to vanilla recipe categories, to customize their behavior.
 *
 * An instance of this is passed to you mod's plugin in
 * {@link IModPlugin#registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration)}
 */
@ApiStatus.NonExtendable
public interface IVanillaCategoryExtensionRegistration {
	/**
	 * {@link IJivHelpers} provides helpers and tools for addon mods.
	 *
	 * @since 13.1.0
	 */
	IJivHelpers getJivHelpers();

	/**
	 * Get the vanilla crafting category, to extend it with your own mod's crafting category extensions.
	 */
	IExtendableCraftingRecipeCategory getCraftingCategory();

	/**
	 * Get the vanilla smithing category, to extend it with your own mod's smithing category extensions.
	 * @since 19.5.0
	 */
	IExtendableSmithingRecipeCategory getSmithingCategory();

	/**
	 * Get the vanilla brewing category, to extend it with custom platform brewing recipe extensions.
	 * @since 29.22.0
	 */
	IExtendableBrewingRecipeCategory getBrewingCategory();
}
