package eakerzt.jiv.gui.config;

import eakerzt.jiv.api.constants.ModIds;
import eakerzt.jiv.api.runtime.IJivKeyMapping;
import eakerzt.jiv.common.input.IInternalKeyMappings;
import eakerzt.jiv.common.input.keys.IJivKeyMappingCategoryBuilder;
import eakerzt.jiv.common.input.keys.IJivKeyMappingInternal;
import eakerzt.jiv.common.input.keys.IJivKeyMappingWithExtraModifiers;
import eakerzt.jiv.common.input.keys.JivKeyConflictContext;
import eakerzt.jiv.common.input.keys.JivKeyModifier;
import eakerzt.jiv.common.input.keys.JivMultiKeyMapping;
import eakerzt.jiv.common.platform.IPlatformInputHelper;
import eakerzt.jiv.common.platform.Services;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public final class InternalKeyMappings implements IInternalKeyMappings {
	private final IJivKeyMappingInternal toggleOverlay;
	private final IJivKeyMappingInternal focusSearch;
	private final IJivKeyMappingInternal toggleCheatMode;
	private final IJivKeyMappingInternal toggleEditMode;

	private final IJivKeyMappingInternal toggleCheatModeConfigButton;

	private final IJivKeyMappingInternal recipeBack;
	private final IJivKeyMappingInternal recipeForward;
	private final IJivKeyMappingInternal previousCategory;
	private final IJivKeyMappingInternal nextCategory;
	private final IJivKeyMappingInternal previousRecipePage;
	private final IJivKeyMappingInternal nextRecipePage;
	private final IJivKeyMappingInternal pauseRecipeCycling;

	private final IJivKeyMappingInternal previousPage;
	private final IJivKeyMappingInternal nextPage;

	private final IJivKeyMappingInternal bookmark;
	private final IJivKeyMappingInternal toggleBookmarkOverlay;
	private final IJivKeyMappingInternal transferRecipeBookmark;
	private final IJivKeyMappingInternal maxTransferRecipeBookmark;
	private final IJivKeyMappingInternal quickMove;
	private final IJivKeyMappingInternal shareToChat;

	private final IJivKeyMappingWithExtraModifiers showRecipe;
	private final IJivKeyMappingWithExtraModifiers showUses;

	private final IJivKeyMapping cheatOneItem;
	private final IJivKeyMapping cheatItemStack;

	private final IJivKeyMappingInternal toggleHideIngredient;
	private final IJivKeyMappingInternal toggleWildcardHideIngredient;

	private final IJivKeyMappingInternal hoveredClearSearchBar;
	private final IJivKeyMappingInternal previousSearch;
	private final IJivKeyMappingInternal nextSearch;

	private final IJivKeyMappingInternal copyRecipeId;

	private final IJivKeyMappingInternal closeRecipeGui;

	// internal only, unregistered and can't be changed because they match vanilla Minecraft hard-coded keys:
	private final IJivKeyMapping escapeKey;
	private final IJivKeyMapping leftClick;
	private final IJivKeyMapping rightClick;
	private final IJivKeyMapping enterKey;

	private final List<KeyMapping> configKeyMappings;

	private static KeyMapping.Category createUnregisteredCategory(String name) {
		Identifier id = Identifier.fromNamespaceAndPath(ModIds.JIV_ID, name);
		return new KeyMapping.Category(id);
	}

	private record CategoryBuilderFactory(
		IPlatformInputHelper inputHelper,
		Function<Identifier, KeyMapping.Category> createCategoryMethod
	) {
		public IJivKeyMappingCategoryBuilder create(String name) {
			Identifier id = Identifier.fromNamespaceAndPath(ModIds.JIV_ID, name);
			KeyMapping.Category category = createCategoryMethod.apply(id);
			return inputHelper.createKeyMappingCategoryBuilder(category);
		}
	}

	public InternalKeyMappings(Consumer<KeyMapping> registerMethod, Function<Identifier, KeyMapping.Category> createCategoryMethod) {
		IPlatformInputHelper inputHelper = Services.PLATFORM.getInputHelper();
		CategoryBuilderFactory categoryBuilderFactory = new CategoryBuilderFactory(inputHelper, createCategoryMethod);

		IJivKeyMappingInternal showRecipe1;
		IJivKeyMappingInternal showRecipe2;
		IJivKeyMappingInternal showUses1;
		IJivKeyMappingInternal showUses2;
		IJivKeyMappingInternal cheatOneItem1;
		IJivKeyMappingInternal cheatOneItem2;
		IJivKeyMappingInternal cheatItemStack1;
		IJivKeyMappingInternal cheatItemStack2;

		IJivKeyMappingCategoryBuilder overlay = categoryBuilderFactory.create("overlays");

		IJivKeyMappingCategoryBuilder mouseHover = categoryBuilderFactory.create("mouse.hover");

		IJivKeyMappingCategoryBuilder search = categoryBuilderFactory.create("search");

		IJivKeyMappingCategoryBuilder cheat = categoryBuilderFactory.create("cheat.mode");

		IJivKeyMappingCategoryBuilder hoverConfig = categoryBuilderFactory.create("hover.config.button");

		IJivKeyMappingCategoryBuilder editMode = categoryBuilderFactory.create("edit.mode");

		IJivKeyMappingCategoryBuilder recipeGui = categoryBuilderFactory.create("recipe.gui");

		IJivKeyMappingCategoryBuilder devTools = categoryBuilderFactory.create("dev.tools");

		// Overlay
		toggleOverlay = overlay.createMapping("key.jiv.toggleOverlay")
			.setContext(JivKeyConflictContext.GUI)
			.setModifier(JivKeyModifier.CONTROL_OR_COMMAND)
			.buildKeyboardKey(GLFW.GLFW_KEY_O)
			.register(registerMethod);

		focusSearch = overlay.createMapping("key.jiv.focusSearch")
			.setContext(JivKeyConflictContext.GUI)
			.setModifier(JivKeyModifier.CONTROL_OR_COMMAND)
			.buildKeyboardKey(GLFW.GLFW_KEY_F)
			.register(registerMethod);

		previousPage = overlay.createMapping("key.jiv.previousPage")
			.setContext(JivKeyConflictContext.GUI)
			.buildUnbound()
			.register(registerMethod);

		nextPage = overlay.createMapping("key.jiv.nextPage")
			.setContext(JivKeyConflictContext.GUI)
			.buildUnbound()
			.register(registerMethod);

		toggleBookmarkOverlay = overlay.createMapping("key.jiv.toggleBookmarkOverlay")
			.setContext(JivKeyConflictContext.GUI)
			.buildUnbound()
			.register(registerMethod);

		// Mouse Hover
		bookmark = mouseHover.createMapping("key.jiv.bookmark")
			.setContext(JivKeyConflictContext.JIV_GUI_HOVER)
			.buildKeyboardKey(GLFW.GLFW_KEY_A)
			.register(registerMethod);

		showRecipe1 = mouseHover.createMapping("key.jiv.showRecipe")
			.setContext(JivKeyConflictContext.JIV_GUI_HOVER)
			.buildKeyboardKey(GLFW.GLFW_KEY_R)
			.register(registerMethod);

		showRecipe2 = mouseHover.createMapping("key.jiv.showRecipe2")
			.setContext(JivKeyConflictContext.JIV_GUI_HOVER)
			.buildMouseLeft()
			.register(registerMethod);

		showUses1 = mouseHover.createMapping("key.jiv.showUses")
			.setContext(JivKeyConflictContext.JIV_GUI_HOVER)
			.buildKeyboardKey(GLFW.GLFW_KEY_U)
			.register(registerMethod);

		showUses2 = mouseHover.createMapping("key.jiv.showUses2")
			.setContext(JivKeyConflictContext.JIV_GUI_HOVER)
			.buildMouseRight()
			.register(registerMethod);

		transferRecipeBookmark = mouseHover.createMapping("key.jiv.transferRecipeBookmark")
			.setContext(JivKeyConflictContext.JIV_GUI_HOVER_BOOKMARK)
			.setModifier(JivKeyModifier.SHIFT)
			.buildMouseLeft()
			.register(registerMethod);

		maxTransferRecipeBookmark = mouseHover.createMapping("key.jiv.maxTransferRecipeBookmark")
			.setContext(JivKeyConflictContext.JIV_GUI_HOVER_BOOKMARK)
			.setModifier(JivKeyModifier.CONTROL_OR_COMMAND)
			.buildMouseLeft()
			.register(registerMethod);

		quickMove = mouseHover.createMapping("key.jiv.quickMove")
			.setContext(JivKeyConflictContext.JIV_GUI_HOVER)
			.setModifier(JivKeyModifier.SHIFT)
			.buildMouseLeft()
			.register(registerMethod);

		shareToChat = mouseHover.createMapping("key.jiv.shareToChat")
			.setContext(JivKeyConflictContext.JIV_GUI_HOVER)
			.buildUnbound()
			.register(registerMethod);

		// Search Bar
		hoveredClearSearchBar = search.createMapping("key.jiv.clearSearchBar")
			.setContext(JivKeyConflictContext.JIV_GUI_HOVER_SEARCH)
			.buildMouseRight()
			.register(registerMethod);

		previousSearch = search.createMapping("key.jiv.previousSearch")
			.setContext(JivKeyConflictContext.JIV_GUI_FOCUSED_SEARCH)
			.buildKeyboardKey(GLFW.GLFW_KEY_UP)
			.register(registerMethod);

		nextSearch = search.createMapping("key.jiv.nextSearch")
			.setContext(JivKeyConflictContext.JIV_GUI_FOCUSED_SEARCH)
			.buildKeyboardKey(GLFW.GLFW_KEY_DOWN)
			.register(registerMethod);

		// Cheat Mode
		toggleCheatMode = cheat.createMapping("key.jiv.toggleCheatMode")
			.setContext(JivKeyConflictContext.GUI)
			.buildUnbound()
			.register(registerMethod);

		cheatOneItem1 = cheat.createMapping("key.jiv.cheatOneItem")
			.setContext(JivKeyConflictContext.JIV_GUI_HOVER_CHEAT_MODE)
			.buildMouseLeft()
			.register(registerMethod);

		cheatOneItem2 = cheat.createMapping("key.jiv.cheatOneItem2")
			.setContext(JivKeyConflictContext.JIV_GUI_HOVER_CHEAT_MODE)
			.buildMouseRight()
			.register(registerMethod);

		cheatItemStack1 = cheat.createMapping("key.jiv.cheatItemStack")
			.setContext(JivKeyConflictContext.JIV_GUI_HOVER_CHEAT_MODE)
			.setModifier(JivKeyModifier.SHIFT)
			.buildMouseLeft()
			.register(registerMethod);

		cheatItemStack2 = cheat.createMapping("key.jiv.cheatItemStack2")
			.setContext(JivKeyConflictContext.JIV_GUI_HOVER_CHEAT_MODE)
			.buildMouseMiddle()
			.register(registerMethod);

		// Hovering over config button
		toggleCheatModeConfigButton = hoverConfig.createMapping("key.jiv.toggleCheatModeConfigButton")
			.setContext(JivKeyConflictContext.JIV_GUI_HOVER_CONFIG_BUTTON)
			.setModifier(JivKeyModifier.CONTROL_OR_COMMAND)
			.buildMouseLeft()
			.register(registerMethod);

		// Edit Mode
		toggleEditMode = editMode.createMapping("key.jiv.toggleEditMode")
			.setContext(JivKeyConflictContext.GUI)
			.buildUnbound()
			.register(registerMethod);

		toggleHideIngredient = editMode.createMapping("key.jiv.toggleHideIngredient")
			.setContext(JivKeyConflictContext.JIV_GUI_HOVER_INGREDIENT)
			.setModifier(JivKeyModifier.CONTROL_OR_COMMAND)
			.buildMouseLeft()
			.register(registerMethod);

		toggleWildcardHideIngredient = editMode.createMapping("key.jiv.toggleWildcardHideIngredient")
			.setContext(JivKeyConflictContext.JIV_GUI_HOVER_INGREDIENT)
			.setModifier(JivKeyModifier.CONTROL_OR_COMMAND)
			.buildMouseRight()
			.register(registerMethod);

		// Recipes
		recipeBack = recipeGui.createMapping("key.jiv.recipeBack")
			.setContext(JivKeyConflictContext.GUI)
			.buildKeyboardKey(GLFW.GLFW_KEY_BACKSPACE)
			.register(registerMethod);

		recipeForward = recipeGui.createMapping("key.jiv.recipeForward")
			.setContext(JivKeyConflictContext.GUI)
			.buildUnbound()
			.register(registerMethod);

		previousRecipePage = recipeGui.createMapping("key.jiv.previousRecipePage")
			.setContext(JivKeyConflictContext.GUI)
			.buildKeyboardKey(GLFW.GLFW_KEY_PAGE_UP)
			.register(registerMethod);

		nextRecipePage = recipeGui.createMapping("key.jiv.nextRecipePage")
			.setContext(JivKeyConflictContext.GUI)
			.buildKeyboardKey(GLFW.GLFW_KEY_PAGE_DOWN)
			.register(registerMethod);

		pauseRecipeCycling = recipeGui.createMapping("key.jiv.pauseRecipeCycling")
			.setContext(JivKeyConflictContext.GUI)
			.buildKeyboardKey(GLFW.GLFW_KEY_LEFT_SHIFT)
			.register(registerMethod);

		previousCategory = recipeGui.createMapping("key.jiv.previousCategory")
			.setContext(JivKeyConflictContext.GUI)
			.setModifier(JivKeyModifier.SHIFT)
			.buildKeyboardKey(GLFW.GLFW_KEY_PAGE_UP)
			.register(registerMethod);

		nextCategory = recipeGui.createMapping("key.jiv.nextCategory")
			.setContext(JivKeyConflictContext.GUI)
			.setModifier(JivKeyModifier.SHIFT)
			.buildKeyboardKey(GLFW.GLFW_KEY_PAGE_DOWN)
			.register(registerMethod);

		closeRecipeGui = recipeGui.createMapping("key.jiv.closeRecipeGui")
			.setContext(JivKeyConflictContext.GUI)
			.buildKeyboardKey(GLFW.GLFW_KEY_ESCAPE)
			.register(registerMethod);

		// Dev Tools
		copyRecipeId = devTools.createMapping("key.jiv.copy.recipe.id")
			.setContext(JivKeyConflictContext.GUI)
			.buildUnbound()
			.register(registerMethod);

		configKeyMappings = Stream.of(
				focusSearch,
				hoveredClearSearchBar,
				previousSearch,
				nextSearch,
				showRecipe1,
				showRecipe2,
				showUses1,
				showUses2,
				bookmark,
				transferRecipeBookmark,
				maxTransferRecipeBookmark,
				quickMove,
				shareToChat,
				toggleOverlay,
				toggleBookmarkOverlay,
				previousPage,
				nextPage,
				recipeBack,
				recipeForward,
				previousRecipePage,
				nextRecipePage,
				pauseRecipeCycling,
				previousCategory,
				nextCategory,
				closeRecipeGui,
				toggleCheatMode,
				toggleCheatModeConfigButton,
				cheatOneItem1,
				cheatOneItem2,
				cheatItemStack1,
				cheatItemStack2,
				toggleEditMode,
				toggleHideIngredient,
				toggleWildcardHideIngredient,
				copyRecipeId
			)
			.map(IJivKeyMappingInternal::getKeyMapping)
			.toList();

		showRecipe = new JivMultiKeyMapping(showRecipe1, showRecipe2);
		showUses = new JivMultiKeyMapping(showUses1, showUses2);
		cheatOneItem = new JivMultiKeyMapping(cheatOneItem1, cheatOneItem2);
		cheatItemStack = new JivMultiKeyMapping(cheatItemStack1, cheatItemStack2);

		var jivHiddenInternalCategory = createUnregisteredCategory("hidden.internal");
		IJivKeyMappingCategoryBuilder jivHidden = inputHelper.createKeyMappingCategoryBuilder(jivHiddenInternalCategory);

		escapeKey = jivHidden.createMapping("key.jiv.internal.escape.key")
			.setContext(JivKeyConflictContext.GUI)
			.buildKeyboardKey(GLFW.GLFW_KEY_ESCAPE);

		leftClick = jivHidden.createMapping("key.jiv.internal.left.click")
			.setContext(JivKeyConflictContext.GUI)
			.buildMouseLeft();

		rightClick = jivHidden.createMapping("key.jiv.internal.right.click")
			.setContext(JivKeyConflictContext.GUI)
			.buildMouseRight();

		enterKey = new JivMultiKeyMapping(
			jivHidden.createMapping("key.jiv.internal.enter.key")
				.setContext(JivKeyConflictContext.GUI)
				.buildKeyboardKey(GLFW.GLFW_KEY_ENTER),

			jivHidden.createMapping("key.jiv.internal.enter.key2")
				.setContext(JivKeyConflictContext.GUI)
				.buildKeyboardKey(GLFW.GLFW_KEY_KP_ENTER)
		);
	}

	@Override
	public IJivKeyMapping getToggleOverlay() {
		return toggleOverlay;
	}

	@Override
	public IJivKeyMapping getFocusSearch() {
		return focusSearch;
	}

	@Override
	public IJivKeyMapping getToggleCheatMode() {
		return toggleCheatMode;
	}

	@Override
	public IJivKeyMapping getToggleEditMode() {
		return toggleEditMode;
	}

	@Override
	public IJivKeyMapping getToggleCheatModeConfigButton() {
		return toggleCheatModeConfigButton;
	}

	@Override
	public IJivKeyMapping getRecipeBack() {
		return recipeBack;
	}

	@Override
	public IJivKeyMapping getRecipeForward() {
		return recipeForward;
	}

	@Override
	public IJivKeyMapping getPreviousCategory() {
		return previousCategory;
	}

	@Override
	public IJivKeyMapping getNextCategory() {
		return nextCategory;
	}

	@Override
	public IJivKeyMapping getPreviousRecipePage() {
		return previousRecipePage;
	}

	@Override
	public IJivKeyMapping getNextRecipePage() {
		return nextRecipePage;
	}

	@Override
	public IJivKeyMappingInternal getPauseRecipeCycling() {
		return pauseRecipeCycling;
	}

	@Override
	public IJivKeyMapping getPreviousPage() {
		return previousPage;
	}

	@Override
	public IJivKeyMapping getNextPage() {
		return nextPage;
	}

	@Override
	public IJivKeyMapping getCloseRecipeGui() {
		return closeRecipeGui;
	}

	@Override
	public IJivKeyMappingWithExtraModifiers getBookmark() {
		return bookmark;
	}

	@Override
	public IJivKeyMapping getToggleBookmarkOverlay() {
		return toggleBookmarkOverlay;
	}

	@Override
	public IJivKeyMappingWithExtraModifiers getShowRecipe() {
		return showRecipe;
	}

	@Override
	public IJivKeyMappingWithExtraModifiers getShowUses() {
		return showUses;
	}

	@Override
	public IJivKeyMapping getTransferRecipeBookmark() {
		return transferRecipeBookmark;
	}

	@Override
	public IJivKeyMapping getMaxTransferRecipeBookmark() {
		return maxTransferRecipeBookmark;
	}

	@Override
	public IJivKeyMapping getQuickMove() {
		return quickMove;
	}

	@Override
	public IJivKeyMapping getShareToChat() {
		return shareToChat;
	}

	@Override
	public IJivKeyMapping getCheatOneItem() {
		return cheatOneItem;
	}

	@Override
	public IJivKeyMapping getCheatItemStack() {
		return cheatItemStack;
	}

	@Override
	public IJivKeyMapping getToggleHideIngredient() {
		return toggleHideIngredient;
	}

	@Override
	public IJivKeyMapping getToggleWildcardHideIngredient() {
		return toggleWildcardHideIngredient;
	}

	@Override
	public IJivKeyMapping getHoveredClearSearchBar() {
		return hoveredClearSearchBar;
	}

	@Override
	public IJivKeyMapping getPreviousSearch() {
		return previousSearch;
	}

	@Override
	public IJivKeyMapping getNextSearch() {
		return nextSearch;
	}

	@Override
	public IJivKeyMapping getCopyRecipeId() {
		return copyRecipeId;
	}

	@Override
	public List<KeyMapping> getConfigKeyMappings() {
		return configKeyMappings;
	}

	@Override
	public IJivKeyMapping getEscapeKey() {
		return escapeKey;
	}

	@Override
	public IJivKeyMapping getLeftClick() {
		return leftClick;
	}

	@Override
	public IJivKeyMapping getRightClick() {
		return rightClick;
	}

	@Override
	public IJivKeyMapping getEnterKey() {
		return enterKey;
	}
}
