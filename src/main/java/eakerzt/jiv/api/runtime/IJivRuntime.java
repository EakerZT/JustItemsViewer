package eakerzt.jiv.api.runtime;

import eakerzt.jiv.api.IModPlugin;
import eakerzt.jiv.api.helpers.IJivHelpers;
import eakerzt.jiv.api.recipe.IRecipeManager;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferManager;
import org.jetbrains.annotations.ApiStatus;

/**
 * Gives access to JIV functions that are available once everything has loaded.
 * The IJivRuntime instance is passed to your mod plugin in {@link IModPlugin#onRuntimeAvailable(IJivRuntime)}.
 */
@ApiStatus.NonExtendable

public interface IJivRuntime {
	/**
	 * The {@link IRecipeManager} offers several functions for retrieving and handling recipes.
	 */
	IRecipeManager getRecipeManager();

	/**
	 * The {@link IRecipesGui} is JIV's gui for displaying recipes.
	 * Use this interface to open the gui and display recipes.
	 */
	IRecipesGui getRecipesGui();

	/**
	 * The {@link IIngredientFilter} is JIV's filter that can be set by players or controlled by mods.
	 * Use this interface to get information from and interact with it.
	 */
	IIngredientFilter getIngredientFilter();

	/**
	 * The {@link IIngredientListOverlay} is JIV's gui that displays all the ingredients next to an open container gui.
	 * Use this interface to get information from and interact with it.
	 */
	IIngredientListOverlay getIngredientListOverlay();

	/**
	 * The {@link IBookmarkOverlay} is JIV's gui that displays all the bookmarked ingredients next to an open container gui.
	 * Use this interface to get information from it.
	 */
	IBookmarkOverlay getBookmarkOverlay();

	/**
	 * The {@link IBookmarkManager} gives access to JIV's ingredient bookmarks.
	 *
	 * @since 29.36.0
	 */
	IBookmarkManager getBookmarkManager();

	/**
	 * {@link IJivHelpers} provides helpers and tools for addon mods.
	 *
	 * @since 9.4.2
	 */
	IJivHelpers getJivHelpers();

	/**
	 * The {@link IIngredientManager} has some useful functions related to recipe ingredients.
	 */
	IIngredientManager getIngredientManager();

	/**
	 * The {@link IJivKeyMappings} gives access to key mappings used by JIV.
	 * This can be used by mods that want to use the same keys that players bind for JIV.
	 *
	 * @since 11.0.1
	 */
	IJivKeyMappings getKeyMappings();

	/**
	 * Get a helper for all runtime Screen functions.
	 * This is used by JIV's GUI and can be used by other mods that want to use the same information from JIV.
	 *
	 * @since 11.5.0
	 */
	IScreenHelper getScreenHelper();

	/**
	 * Get a manager that holds all the registered recipe transfer handlers.
	 * This is used by JIV's GUI and can be used by other mods that want to use the same information from JIV.
	 *
	 * @since 11.5.0
	 */
	IRecipeTransferManager getRecipeTransferManager();

	/**
	 * Get access to the edit-mode config, which lets users hide ingredients from JIV.
	 * This is used by JIV's GUI and can be used by other mods that want to use the same information from JIV.
	 *
	 * @since 11.5.0
	 */
	IEditModeConfig getEditModeConfig();

}
