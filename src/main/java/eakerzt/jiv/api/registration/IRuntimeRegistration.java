package eakerzt.jiv.api.registration;

import eakerzt.jiv.api.IModPlugin;
import eakerzt.jiv.api.helpers.IJivHelpers;
import eakerzt.jiv.api.recipe.IRecipeManager;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferManager;
import eakerzt.jiv.api.runtime.IBookmarkManager;
import eakerzt.jiv.api.runtime.IBookmarkOverlay;
import eakerzt.jiv.api.runtime.IEditModeConfig;
import eakerzt.jiv.api.runtime.IIngredientFilter;
import eakerzt.jiv.api.runtime.IIngredientListOverlay;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.api.runtime.IRecipesGui;
import eakerzt.jiv.api.runtime.IScreenHelper;
import eakerzt.jiv.api.search.ISearchStorageBuilderFactory;
import org.jetbrains.annotations.ApiStatus;

/**
 * Allows mods to override the runtime classes for JIV with their own implementation.
 *
 * @since 12.0.2
 */
@ApiStatus.NonExtendable
public interface IRuntimeRegistration {
	/**
	 * Set the ingredient list overlay.
	 *
	 * This is used by JIV's GUI and can be used by other mods
	 * that want to override JIV's GUI and have it still work with mods that use JIV's API.
	 */
	void setIngredientListOverlay(IIngredientListOverlay ingredientListOverlay);

	/**
	 * Set the bookmark list overlay.
	 *
	 * This is used by JIV's GUI and can be used by other mods
	 * that want to override JIV's GUI and have it still work with mods that use JIV's API.
	 */
	void setBookmarkOverlay(IBookmarkOverlay bookmarkOverlay);

	/**
	 * Set the bookmark manager.
	 *
	 * This is used by JIV's GUI and can be used by other mods
	 * that want to override JIV's bookmark management and have it still work with mods that use JIV's API.
	 *
	 * @since 29.36.0
	 */
	void setBookmarkManager(IBookmarkManager bookmarkManager);

	/**
	 * Set the Recipe GUI.
	 *
	 * This is used by JIV's GUI and can be used by other mods
	 * that want to override JIV's GUI and have it still work with mods that use JIV's API.
	 */
	void setRecipesGui(IRecipesGui recipesGui);

	/**
	 * Set the Ingredient Filter.
	 *
	 * This is used by JIV's GUI and can be used by other mods
	 * that want to override JIV's GUI and have it still work with mods that use JIV's API.
	 */
	void setIngredientFilter(IIngredientFilter ingredientFilter);

	/**
	 * The {@link IRecipeManager} offers several functions for retrieving and handling recipes.
	 */
	IRecipeManager getRecipeManager();

	/**
	 * {@link IJivHelpers} provides helpers and tools for addon mods.
	 */
	IJivHelpers getJivHelpers();

	/**
	 * The {@link IIngredientManager} has some useful functions related to recipe ingredients.
	 */
	IIngredientManager getIngredientManager();

	/**
	 * Get a helper for all runtime Screen functions.
	 * This is used by JIV's GUI and can be used by other mods that want to use the same information from JIV.
	 */
	IScreenHelper getScreenHelper();

	/**
	 * Get a manager that holds all the registered recipe transfer handlers.
	 * This is used by JIV's GUI and can be used by other mods that want to use the same information from JIV.
	 */
	IRecipeTransferManager getRecipeTransferManager();

	/**
	 * Get access to the edit-mode config, which lets users hide ingredients from JIV.
	 * This is used by JIV's GUI and can be used by other mods that want to use the same information from JIV.
	 */
	IEditModeConfig getEditModeConfig();

	/**
	 * Get the search storage builder factory used by JIV's ingredient filter.
	 * This can be overridden for advanced search behavior with
	 * {@link IModPlugin#registerAdvancedSearch(IAdvancedSearchRegistration)} and
	 * {@link IAdvancedSearchRegistration#replaceSearchStorage}.
	 *
	 * <p>
	 * This returns a factory for builders that construct independent search storage.
	 * JIV creates a fresh builder for each independent search index, adds the initial ingredient data to it, and then
	 * calls {@link eakerzt.jiv.api.search.ISearchStorageBuilder#build()} to create the storage used by the ingredient
	 * filter.
	 * </p>
	 *
	 * <p>
	 * This build phase lets implementations preprocess or bake the initial index for faster searches. The storage
	 * returned by the builder is still used for runtime ingredient additions after startup.
	 * </p>
	 *
	 * @since 29.19.0
	 */
	ISearchStorageBuilderFactory getSearchStorageBuilderFactory();
}
