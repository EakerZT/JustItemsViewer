package eakerzt.jiv.gui.startup;

import eakerzt.jiv.api.helpers.IColorHelper;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.api.runtime.IScreenHelper;
import eakerzt.jiv.common.config.HistoryDisplaySide;
import eakerzt.jiv.common.config.IClientConfig;
import eakerzt.jiv.common.config.IClientToggleState;
import eakerzt.jiv.common.config.IIngredientFilterConfig;
import eakerzt.jiv.common.config.IIngredientGridConfig;
import eakerzt.jiv.common.gui.elements.ScalableDrawable;
import eakerzt.jiv.common.gui.textures.Textures;
import eakerzt.jiv.common.input.IInternalKeyMappings;
import eakerzt.jiv.common.network.IConnectionToServer;
import eakerzt.jiv.common.transfer.RecipeTransferService;
import eakerzt.jiv.gui.bookmarks.BookmarkList;
import eakerzt.jiv.gui.filter.IFilterTextSource;
import eakerzt.jiv.gui.overlay.ingredients.IIngredientGridSource;
import eakerzt.jiv.gui.overlay.ingredients.IngredientGrid;
import eakerzt.jiv.gui.overlay.ingredients.IngredientGridWithNavigation;
import eakerzt.jiv.gui.overlay.IngredientListOverlay;
import eakerzt.jiv.gui.overlay.bookmarks.BookmarkOverlay;
import eakerzt.jiv.gui.overlay.bookmarks.history.LookupHistoryOverlay;

public final class OverlayHelper {
	private OverlayHelper() {}

	public static IngredientGridWithNavigation createIngredientGridWithNavigation(
		String debugName,
		IIngredientGridSource ingredientFilter,
		IIngredientManager ingredientManager,
		IIngredientGridConfig ingredientGridConfig,
		ScalableDrawable background,
		ScalableDrawable slotBackground,
		ScalableDrawable exclusionAreaShadow,
		IInternalKeyMappings keyMappings,
		IIngredientFilterConfig ingredientFilterConfig,
		IClientConfig clientConfig,
		IClientToggleState toggleState,
		IConnectionToServer serverConnection,
		IColorHelper colorHelper,
		IScreenHelper screenHelper,
		boolean supportsEditMode
	) {
		IngredientGrid ingredientListGrid = new IngredientGrid(
			ingredientManager,
			ingredientGridConfig,
			ingredientFilterConfig,
			clientConfig,
			toggleState,
			serverConnection,
			keyMappings,
			colorHelper,
			supportsEditMode
		);

		return new IngredientGridWithNavigation(
			debugName,
			ingredientFilter,
			ingredientListGrid,
			toggleState,
			clientConfig,
			serverConnection,
			ingredientGridConfig,
			background,
			slotBackground,
			exclusionAreaShadow,
			screenHelper,
			ingredientManager
		);
	}

	public static IngredientListOverlay createIngredientListOverlay(
		IIngredientManager ingredientManager,
		IScreenHelper screenHelper,
		IIngredientGridSource ingredientFilter,
		IIngredientGridSource historyList,
		IFilterTextSource filterTextSource,
		IInternalKeyMappings keyMappings,
		IIngredientGridConfig ingredientGridConfig,
		IClientConfig clientConfig,
		IClientToggleState toggleState,
		IConnectionToServer serverConnection,
		IIngredientFilterConfig ingredientFilterConfig,
		Textures textures,
		IColorHelper colorHelper
	) {
		IngredientGridWithNavigation ingredientListGridNavigation = createIngredientGridWithNavigation(
			"IngredientListOverlay",
			ingredientFilter,
			ingredientManager,
			ingredientGridConfig,
			textures.getIngredientListBackground(),
			textures.getIngredientListSlotBackground(),
			textures.getExclusionAreaShadow(),
			keyMappings,
			ingredientFilterConfig,
			clientConfig,
			toggleState,
			serverConnection,
			colorHelper,
			screenHelper,
			true
		);

		LookupHistoryOverlay lookupHistoryOverlay = new LookupHistoryOverlay(
			ingredientManager,
			historyList,
			keyMappings,
			ingredientGridConfig,
			ingredientFilterConfig,
			textures.getIngredientListBackground(),
			textures.getIngredientListSlotBackground(),
			textures.getExclusionAreaShadow(),
			clientConfig,
			HistoryDisplaySide.RIGHT,
			toggleState,
			screenHelper,
			serverConnection,
			colorHelper
		);

		return new IngredientListOverlay(
			ingredientFilter,
			filterTextSource,
			screenHelper,
			ingredientListGridNavigation,
			lookupHistoryOverlay,
			ingredientGridConfig,
			clientConfig,
			toggleState,
			keyMappings
		);
	}

	public static BookmarkOverlay createBookmarkOverlay(
		IIngredientManager ingredientManager,
		IScreenHelper screenHelper,
		BookmarkList bookmarkList,
		RecipeTransferService recipeTransferService,
		IIngredientGridSource lookupHistory,
		IInternalKeyMappings keyMappings,
		IIngredientGridConfig bookmarkListConfig,
		IIngredientFilterConfig ingredientFilterConfig,
		IClientConfig clientConfig,
		IClientToggleState toggleState,
		IConnectionToServer serverConnection,
		Textures textures,
		IColorHelper colorHelper
	) {
		IngredientGridWithNavigation bookmarkListGridNavigation = createIngredientGridWithNavigation(
			"BookmarkOverlay",
			bookmarkList,
			ingredientManager,
			bookmarkListConfig,
			textures.getBookmarkListBackground(),
			textures.getBookmarkListSlotBackground(),
			textures.getExclusionAreaShadow(),
			keyMappings,
			ingredientFilterConfig,
			clientConfig,
			toggleState,
			serverConnection,
			colorHelper,
			screenHelper,
			false
		);

		LookupHistoryOverlay lookupHistoryOverlay = new LookupHistoryOverlay(
			ingredientManager,
			lookupHistory,
			keyMappings,
			bookmarkListConfig,
			ingredientFilterConfig,
			textures.getBookmarkListBackground(),
			textures.getBookmarkListSlotBackground(),
			textures.getExclusionAreaShadow(),
			clientConfig,
			HistoryDisplaySide.LEFT,
			toggleState,
			screenHelper,
			serverConnection,
			colorHelper
		);

		return new BookmarkOverlay(
			bookmarkList,
			recipeTransferService,
			bookmarkListGridNavigation,
			lookupHistoryOverlay,
			toggleState,
			clientConfig,
			bookmarkListConfig,
			screenHelper,
			keyMappings
		);
	}
}
