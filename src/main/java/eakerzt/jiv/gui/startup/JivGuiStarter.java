package eakerzt.jiv.gui.startup;

import com.mojang.serialization.Codec;
import eakerzt.jiv.api.helpers.ICodecHelper;
import eakerzt.jiv.api.helpers.IColorHelper;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.api.helpers.IJivHelpers;
import eakerzt.jiv.api.helpers.IModIdHelper;
import eakerzt.jiv.api.recipe.IFocusFactory;
import eakerzt.jiv.api.recipe.IRecipeManager;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferManager;
import eakerzt.jiv.api.registration.IRuntimeRegistration;
import eakerzt.jiv.api.runtime.IEditModeConfig;
import eakerzt.jiv.api.runtime.IIngredientFilter;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.api.runtime.IIngredientVisibility;
import eakerzt.jiv.api.runtime.IScreenHelper;
import eakerzt.jiv.api.search.ISearchStorageBuilderFactory;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.config.IClientConfig;
import eakerzt.jiv.common.config.IClientToggleState;
import eakerzt.jiv.common.config.IIngredientFilterConfig;
import eakerzt.jiv.common.config.IIngredientGridConfig;
import eakerzt.jiv.common.config.IClientConfigs;
import eakerzt.jiv.common.gui.JivGuiColors;
import eakerzt.jiv.common.gui.textures.Textures;
import eakerzt.jiv.common.input.IInternalKeyMappings;
import eakerzt.jiv.common.network.IConnectionToServer;
import eakerzt.jiv.common.transfer.RecipeTransferService;
import eakerzt.jiv.common.util.ErrorUtil;
import eakerzt.jiv.common.util.LoggedTimer;
import eakerzt.jiv.gui.bookmarks.BookmarkCodec;
import eakerzt.jiv.gui.bookmarks.BookmarkFactory;
import eakerzt.jiv.gui.bookmarks.BookmarkList;
import eakerzt.jiv.gui.bookmarks.IBookmark;
import eakerzt.jiv.gui.config.IBookmarkConfig;
import eakerzt.jiv.gui.config.ILookupHistoryConfig;
import eakerzt.jiv.gui.config.IngredientTypeSortingConfig;
import eakerzt.jiv.gui.config.JivGuiSortingConfigData;
import eakerzt.jiv.gui.config.ModNameSortingConfig;
import eakerzt.jiv.gui.events.GuiEventHandler;
import eakerzt.jiv.gui.filter.FilterTextSource;
import eakerzt.jiv.gui.filter.IFilterTextSource;
import eakerzt.jiv.gui.ingredients.IListElement;
import eakerzt.jiv.gui.ingredients.IListElementInfo;
import eakerzt.jiv.gui.ingredients.IngredientFilter;
import eakerzt.jiv.gui.ingredients.IngredientFilterApi;
import eakerzt.jiv.gui.ingredients.IngredientListElementFactory;
import eakerzt.jiv.gui.ingredients.IngredientSorter;
import eakerzt.jiv.gui.input.ClientInputHandler;
import eakerzt.jiv.gui.input.CombinedRecipeFocusSource;
import eakerzt.jiv.gui.input.GuiContainerWrapper;
import eakerzt.jiv.gui.input.ICharTypedHandler;
import eakerzt.jiv.gui.input.handlers.BookmarkInputHandler;
import eakerzt.jiv.gui.input.handlers.ChatLinkInputHandler;
import eakerzt.jiv.gui.input.handlers.CheatInputHandler;
import eakerzt.jiv.gui.input.handlers.DragRouter;
import eakerzt.jiv.gui.input.handlers.EditInputHandler;
import eakerzt.jiv.gui.input.handlers.ElementInputHandler;
import eakerzt.jiv.gui.input.handlers.FocusInputHandler;
import eakerzt.jiv.gui.input.handlers.GlobalInputHandler;
import eakerzt.jiv.gui.input.handlers.GuiAreaInputHandler;
import eakerzt.jiv.common.input.handlers.UserInputRouter;
import eakerzt.jiv.gui.overlay.IngredientListOverlay;
import eakerzt.jiv.gui.overlay.bookmarks.BookmarkOverlay;
import eakerzt.jiv.gui.overlay.bookmarks.history.LookupHistory;
import eakerzt.jiv.gui.recipes.RecipesGui;
import eakerzt.jiv.gui.util.FocusUtil;
import eakerzt.jiv.config.api.value.change.IConfigValueBatchChangeListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class JivGuiStarter {
	private static final Logger LOGGER = LogManager.getLogger();

	public static JivEventHandlers start(
		IRuntimeRegistration registration,
		JivGuiSortingConfigData sortingConfigData
	) {
		LOGGER.info("Starting JIV GUI");
		LoggedTimer timer = new LoggedTimer();

		IConnectionToServer serverConnection = Internal.getServerConnection();
		Textures textures = Internal.getTextures();
		IInternalKeyMappings keyMappings = Internal.getKeyMappings();

		IScreenHelper screenHelper = registration.getScreenHelper();
		IRecipeTransferManager recipeTransferManager = registration.getRecipeTransferManager();
		RecipeTransferService recipeTransferService = new RecipeTransferService(recipeTransferManager);
		IRecipeManager recipeManager = registration.getRecipeManager();
		IIngredientManager ingredientManager = registration.getIngredientManager();
		IEditModeConfig editModeConfig = registration.getEditModeConfig();
		ISearchStorageBuilderFactory searchStorageBuilderFactory = registration.getSearchStorageBuilderFactory();

		IJivHelpers jivHelpers = registration.getJivHelpers();
		IIngredientVisibility ingredientVisibility = jivHelpers.getIngredientVisibility();
		IColorHelper colorHelper = jivHelpers.getColorHelper();
		IModIdHelper modIdHelper = jivHelpers.getModIdHelper();
		IFocusFactory focusFactory = jivHelpers.getFocusFactory();
		IGuiHelper guiHelper = jivHelpers.getGuiHelper();
		ICodecHelper codecHelper = jivHelpers.getCodecHelper();

		IFilterTextSource filterTextSource = new FilterTextSource();
		Minecraft minecraft = Minecraft.getInstance();
		JivGuiColors.onResourceManagerReload(minecraft.getResourceManager());
		ClientLevel level = minecraft.level;
		ErrorUtil.checkNotNull(level, "minecraft.level");

		RegistryAccess registryAccess = level.registryAccess();

		IClientConfigs jivClientConfigs = Internal.getClientConfigs();
		IClientConfig clientConfig = jivClientConfigs.getClientConfig();
		registerJivRestartingConfigListeners(jivClientConfigs);
		IIngredientGridConfig ingredientListConfig = jivClientConfigs.getIngredientListConfig();
		IIngredientGridConfig bookmarkListConfig = jivClientConfigs.getBookmarkListConfig();
		IIngredientFilterConfig ingredientFilterConfig = jivClientConfigs.getIngredientFilterConfig();

		timer.start("Building ingredient list");
		List<IListElementInfo<?>> ingredientList = IngredientListElementFactory.createBaseList(ingredientManager, ingredientFilterConfig, modIdHelper);
		timer.stop();

		timer.start("Building ingredient filter");
		GuiConfigData configData = GuiConfigData.create(sortingConfigData);

		ModNameSortingConfig modNameSortingConfig = configData.modNameSortingConfig();
		IngredientTypeSortingConfig ingredientTypeSortingConfig = configData.ingredientTypeSortingConfig();
		IClientToggleState toggleState = Internal.getClientToggleState();
		IBookmarkConfig bookmarkConfig = configData.bookmarkConfig();
		ILookupHistoryConfig lookupHistoryConfig = configData.lookupHistoryConfig();

		Function<List<IListElementInfo<?>>, Comparator<IListElement<?>>> sortIndexUpdater = ingredients -> IngredientSorter.sortIngredients(
			clientConfig,
			modNameSortingConfig,
			ingredientTypeSortingConfig,
			ingredientManager,
			ingredients
		);

		IngredientFilter ingredientFilter = new IngredientFilter(
			filterTextSource,
			clientConfig,
			ingredientFilterConfig,
			ingredientManager,
			sortIndexUpdater,
			ingredientList,
			modIdHelper,
			ingredientVisibility,
			ingredientTypeSortingConfig,
			colorHelper,
			searchStorageBuilderFactory,
			toggleState
		);
		jivClientConfigs.registerRuntimeListenerRemoval(
			modNameSortingConfig.addChangeListener(ingredientFilter::onIngredientSortOrderConfigChanged)
		);
		jivClientConfigs.registerRuntimeListenerRemoval(
			ingredientTypeSortingConfig.addChangeListener(ingredientFilter::onIngredientTypeSortOrderConfigChanged)
		);
		ingredientManager.registerIngredientListener(ingredientFilter);
		ingredientVisibility.registerListener(ingredientFilter);
		timer.stop();

		IIngredientFilter ingredientFilterApi = new IngredientFilterApi(ingredientFilter, filterTextSource);
		registration.setIngredientFilter(ingredientFilterApi);

		BookmarkFactory bookmarkFactory = new BookmarkFactory(codecHelper, registryAccess, ingredientManager);
		Codec<IBookmark> bookmarkCodec = BookmarkCodec.create(codecHelper, ingredientManager, recipeManager, recipeTransferService, bookmarkFactory).codec();

		LookupHistory lookupHistory = new LookupHistory(
			recipeManager,
			ingredientManager,
			registryAccess,
			codecHelper,
			clientConfig.maxLookupHistoryIngredients(),
			lookupHistoryConfig,
			bookmarkCodec
		);

		IngredientListOverlay ingredientListOverlay = OverlayHelper.createIngredientListOverlay(
			ingredientManager,
			screenHelper,
			ingredientFilter,
			lookupHistory,
			filterTextSource,
			keyMappings,
			ingredientListConfig,
			clientConfig,
			toggleState,
			serverConnection,
			ingredientFilterConfig,
			textures,
			colorHelper
		);
		registration.setIngredientListOverlay(ingredientListOverlay);

		BookmarkList bookmarkList = new BookmarkList(recipeManager, focusFactory, ingredientManager, registryAccess, bookmarkConfig, clientConfig, guiHelper, codecHelper, bookmarkFactory, bookmarkCodec);
		bookmarkConfig.loadBookmarks(recipeManager, focusFactory, guiHelper, ingredientManager, registryAccess, bookmarkList, codecHelper, bookmarkCodec);
		registration.setBookmarkManager(bookmarkList);

		BookmarkOverlay bookmarkOverlay = OverlayHelper.createBookmarkOverlay(
			ingredientManager,
			screenHelper,
			bookmarkList,
			recipeTransferService,
			lookupHistory,
			keyMappings,
			bookmarkListConfig,
			ingredientFilterConfig,
			clientConfig,
			toggleState,
			serverConnection,
			textures,
			colorHelper
		);
		registration.setBookmarkOverlay(bookmarkOverlay);

		FocusUtil focusUtil = new FocusUtil(focusFactory, clientConfig, ingredientManager);

		RecipesGui recipesGui = new RecipesGui(
			recipeManager,
			ingredientManager,
			recipeTransferService,
			keyMappings,
			focusFactory,
			bookmarkList,
			lookupHistory,
			guiHelper,
			bookmarkFactory,
			focusUtil
		);
		registration.setRecipesGui(recipesGui);
		var recipesGuiForegroundInputLayer = recipesGui.getForegroundInputLayer();
		var bookmarkPreviewTooltipController = bookmarkOverlay.getPreviewTooltipController();

		GuiEventHandler guiEventHandler = new GuiEventHandler(
			screenHelper,
			bookmarkOverlay,
			ingredientListOverlay,
			recipesGuiForegroundInputLayer,
			bookmarkPreviewTooltipController
		);

		CombinedRecipeFocusSource recipeFocusSource = new CombinedRecipeFocusSource(
			bookmarkPreviewTooltipController,
			recipesGui,
			ingredientListOverlay,
			bookmarkOverlay,
			new GuiContainerWrapper(screenHelper)
		);

		List<ICharTypedHandler> charTypedHandlers = List.of(
			ingredientListOverlay
		);

		UserInputRouter userInputRouter = new UserInputRouter(
			"JIVGlobal",
			new eakerzt.jiv.gui.input.handlers.CopyIngredientNameInputHandler(recipeFocusSource, ingredientManager),
			recipesGuiForegroundInputLayer,
			bookmarkOverlay.createGroupScrollInputHandler(),
			bookmarkPreviewTooltipController,
			new EditInputHandler(recipeFocusSource, toggleState, editModeConfig),
			ingredientListOverlay.createDeleteItemInputHandler(),
			bookmarkOverlay.createDeleteItemInputHandler(),
			bookmarkOverlay.createGroupInputHandler(),
			new CheatInputHandler(recipeFocusSource, clientConfig, ingredientManager, toggleState, serverConnection),
			new ElementInputHandler(recipeFocusSource),
			ingredientListOverlay.createInputHandler(),
			bookmarkOverlay.createInputHandler(),
			new FocusInputHandler(recipeFocusSource, recipesGui, focusUtil, ingredientManager),
			new BookmarkInputHandler(
				recipeFocusSource,
				bookmarkList,
				bookmarkOverlay,
				bookmarkPreviewTooltipController,
				recipesGui
			),
			new GlobalInputHandler(toggleState),
			new GuiAreaInputHandler(screenHelper, recipesGui, focusFactory)
		);

		DragRouter dragRouter = new DragRouter(
			ingredientListOverlay.createDragHandler(),
			bookmarkOverlay.createDragHandler()
		);
		ClientInputHandler clientInputHandler = new ClientInputHandler(
			charTypedHandlers,
			new ChatLinkInputHandler(recipesGui, focusUtil, screenHelper, bookmarkList),
			userInputRouter,
			dragRouter,
			keyMappings,
			screenHelper
		);
		ResourceReloadHandler resourceReloadHandler = new ResourceReloadHandler(
			ingredientListOverlay,
			ingredientFilter
		);

		return new JivEventHandlers(
			guiEventHandler,
			clientInputHandler,
			resourceReloadHandler
		);
	}

	private static void registerJivRestartingConfigListeners(IClientConfigs clientConfigs) {
		IClientConfig clientConfig = clientConfigs.getClientConfig();
		IConfigValueBatchChangeListener restartJiv = changes -> Internal.restartJiv();
		clientConfigs.registerRuntimeListenerRemoval(
			clientConfig.showTagRecipesEnabled().addBatchListener(restartJiv)
		);
		clientConfigs.registerRuntimeListenerRemoval(
			clientConfig.showHiddenIngredients().addBatchListener(restartJiv)
		);
	}
}
