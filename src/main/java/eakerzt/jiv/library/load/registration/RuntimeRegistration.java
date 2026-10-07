package eakerzt.jiv.library.load.registration;

import eakerzt.jiv.api.helpers.IJivHelpers;
import eakerzt.jiv.api.recipe.IRecipeManager;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferManager;
import eakerzt.jiv.api.registration.IRuntimeRegistration;
import eakerzt.jiv.api.runtime.IBookmarkManager;
import eakerzt.jiv.api.runtime.IBookmarkOverlay;
import eakerzt.jiv.api.runtime.IEditModeConfig;
import eakerzt.jiv.api.runtime.IIngredientFilter;
import eakerzt.jiv.api.runtime.IIngredientListOverlay;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.api.runtime.IRecipesGui;
import eakerzt.jiv.api.runtime.IScreenHelper;
import eakerzt.jiv.api.search.ISearchStorageBuilderFactory;
import eakerzt.jiv.library.gui.BookmarkManagerDummy;
import eakerzt.jiv.library.gui.BookmarkOverlayDummy;
import eakerzt.jiv.library.gui.IngredientListOverlayDummy;
import eakerzt.jiv.library.gui.recipes.RecipesGuiDummy;
import eakerzt.jiv.library.ingredients.IngredientFilterApiDummy;

public class RuntimeRegistration implements IRuntimeRegistration {
	private final IRecipeManager recipeManager;
	private final IJivHelpers jivHelpers;
	private final IEditModeConfig editModeConfig;
	private final IIngredientManager ingredientManager;
	private final IRecipeTransferManager recipeTransferManager;
	private final IScreenHelper screenHelper;
	private final ISearchStorageBuilderFactory searchStorageBuilderFactory;

	private IIngredientListOverlay ingredientListOverlay = IngredientListOverlayDummy.INSTANCE;
	private IBookmarkOverlay bookmarkOverlay = BookmarkOverlayDummy.INSTANCE;
	private IBookmarkManager bookmarkManager = BookmarkManagerDummy.INSTANCE;
	private IRecipesGui recipesGui = RecipesGuiDummy.INSTANCE;
	private IIngredientFilter ingredientFilter = IngredientFilterApiDummy.INSTANCE;

	public RuntimeRegistration(
		IRecipeManager recipeManager,
		IJivHelpers jivHelpers,
		IEditModeConfig editModeConfig,
		IIngredientManager ingredientManager,
		IRecipeTransferManager recipeTransferManager,
		IScreenHelper screenHelper,
		ISearchStorageBuilderFactory searchStorageBuilderFactory
	) {
		this.recipeManager = recipeManager;
		this.jivHelpers = jivHelpers;
		this.editModeConfig = editModeConfig;
		this.ingredientManager = ingredientManager;
		this.recipeTransferManager = recipeTransferManager;
		this.screenHelper = screenHelper;
		this.searchStorageBuilderFactory = searchStorageBuilderFactory;
	}

	@Override
	public void setIngredientListOverlay(IIngredientListOverlay ingredientListOverlay) {
		this.ingredientListOverlay = ingredientListOverlay;
	}

	@Override
	public void setBookmarkOverlay(IBookmarkOverlay bookmarkOverlay) {
		this.bookmarkOverlay = bookmarkOverlay;
	}

	@Override
	public void setBookmarkManager(IBookmarkManager bookmarkManager) {
		this.bookmarkManager = bookmarkManager;
	}

	@Override
	public void setRecipesGui(IRecipesGui recipesGui) {
		this.recipesGui = recipesGui;
	}

	@Override
	public void setIngredientFilter(IIngredientFilter ingredientFilter) {
		this.ingredientFilter = ingredientFilter;
	}

	@Override
	public IRecipeManager getRecipeManager() {
		return this.recipeManager;
	}

	@Override
	public IJivHelpers getJivHelpers() {
		return this.jivHelpers;
	}

	@Override
	public IIngredientManager getIngredientManager() {
		return this.ingredientManager;
	}

	@Override
	public IScreenHelper getScreenHelper() {
		return this.screenHelper;
	}

	@Override
	public IRecipeTransferManager getRecipeTransferManager() {
		return this.recipeTransferManager;
	}

	@Override
	public IEditModeConfig getEditModeConfig() {
		return this.editModeConfig;
	}

	@Override
	public ISearchStorageBuilderFactory getSearchStorageBuilderFactory() {
		return searchStorageBuilderFactory;
	}

	public IIngredientListOverlay getIngredientListOverlay() {
		return ingredientListOverlay;
	}

	public IBookmarkOverlay getBookmarkOverlay() {
		return bookmarkOverlay;
	}

	public IBookmarkManager getBookmarkManager() {
		return bookmarkManager;
	}

	public IRecipesGui getRecipesGui() {
		return recipesGui;
	}

	public IIngredientFilter getIngredientFilter() {
		return this.ingredientFilter;
	}
}
