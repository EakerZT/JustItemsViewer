package eakerzt.jiv.library.runtime;

import eakerzt.jiv.api.helpers.IJivHelpers;
import eakerzt.jiv.api.recipe.IRecipeManager;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferManager;
import eakerzt.jiv.api.runtime.IBookmarkManager;
import eakerzt.jiv.api.runtime.IBookmarkOverlay;
import eakerzt.jiv.api.runtime.IEditModeConfig;
import eakerzt.jiv.api.runtime.IIngredientFilter;
import eakerzt.jiv.api.runtime.IIngredientListOverlay;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.api.runtime.IJivKeyMappings;
import eakerzt.jiv.api.runtime.IJivRuntime;
import eakerzt.jiv.api.runtime.IRecipesGui;
import eakerzt.jiv.api.runtime.IScreenHelper;

public class JivRuntime implements IJivRuntime {
	private final IRecipeManager recipeManager;
	private final IRecipeTransferManager recipeTransferManager;
	private final IEditModeConfig editModeConfig;
	private final IIngredientManager ingredientManager;
	private final IJivKeyMappings keyMappings;
	private final IJivHelpers jivHelpers;
	private final IScreenHelper screenHelper;
	private final IIngredientListOverlay ingredientListOverlay;
	private final IBookmarkOverlay bookmarkOverlay;
	private final IBookmarkManager bookmarkManager;
	private final IRecipesGui recipesGui;
	private final IIngredientFilter ingredientFilter;

	public JivRuntime(
		IRecipeManager recipeManager,
		IIngredientManager ingredientManager,
		IJivKeyMappings keyMappings,
		IJivHelpers jivHelpers,
		IScreenHelper screenHelper,
		IRecipeTransferManager recipeTransferManager,
		IEditModeConfig editModeConfig,
		IIngredientListOverlay ingredientListOverlay,
		IBookmarkOverlay bookmarkOverlay,
		IBookmarkManager bookmarkManager,
		IRecipesGui recipesGui,
		IIngredientFilter ingredientFilter
	) {
		this.recipeManager = recipeManager;
		this.recipeTransferManager = recipeTransferManager;
		this.editModeConfig = editModeConfig;
		this.ingredientListOverlay = ingredientListOverlay;
		this.bookmarkOverlay = bookmarkOverlay;
		this.bookmarkManager = bookmarkManager;
		this.recipesGui = recipesGui;
		this.ingredientFilter = ingredientFilter;
		this.ingredientManager = ingredientManager;
		this.keyMappings = keyMappings;
		this.jivHelpers = jivHelpers;
		this.screenHelper = screenHelper;
	}

	@Override
	public IRecipeManager getRecipeManager() {
		return recipeManager;
	}

	@Override
	public IIngredientFilter getIngredientFilter() {
		return ingredientFilter;
	}

	@Override
	public IIngredientListOverlay getIngredientListOverlay() {
		return ingredientListOverlay;
	}

	@Override
	public IIngredientManager getIngredientManager() {
		return ingredientManager;
	}

	@Override
	public IBookmarkOverlay getBookmarkOverlay() {
		return bookmarkOverlay;
	}

	@Override
	public IBookmarkManager getBookmarkManager() {
		return bookmarkManager;
	}

	@Override
	public IJivHelpers getJivHelpers() {
		return jivHelpers;
	}

	@Override
	public IRecipesGui getRecipesGui() {
		return recipesGui;
	}

	@Override
	public IJivKeyMappings getKeyMappings() {
		return keyMappings;
	}

	@Override
	public IScreenHelper getScreenHelper() {
		return screenHelper;
	}

	@Override
	public IRecipeTransferManager getRecipeTransferManager() {
		return recipeTransferManager;
	}

	@Override
	public IEditModeConfig getEditModeConfig() {
		return editModeConfig;
	}

}
