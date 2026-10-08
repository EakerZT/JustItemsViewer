package eakerzt.jiv.gui.overlay.bookmarks;

import eakerzt.jiv.api.gui.handlers.IGuiProperties;
import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.runtime.IBookmarkOverlay;
import eakerzt.jiv.api.runtime.IScreenHelper;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.config.IClientConfig;
import eakerzt.jiv.common.config.IClientToggleState;
import eakerzt.jiv.common.config.IIngredientGridConfig;
import eakerzt.jiv.common.gui.JivGuiColors;
import eakerzt.jiv.common.gui.JivGuiColors.GuiColor;
import eakerzt.jiv.common.input.IInternalKeyMappings;
import eakerzt.jiv.common.input.UserInput;
import eakerzt.jiv.common.transfer.RecipeTransferService;
import eakerzt.jiv.common.util.ImmutablePoint2i;
import eakerzt.jiv.common.util.ImmutableRect2i;
import eakerzt.jiv.common.util.MathUtil;
import eakerzt.jiv.gui.bookmarks.BookmarkList;
import eakerzt.jiv.gui.bookmarks.IBookmark;
import eakerzt.jiv.gui.input.IClickableIngredientInternal;
import eakerzt.jiv.gui.input.IDragHandler;
import eakerzt.jiv.gui.input.IDraggableIngredientInternal;
import eakerzt.jiv.gui.input.IPaged;
import eakerzt.jiv.gui.input.IRecipeFocusSource;
import eakerzt.jiv.common.input.IUserInputHandler;
import eakerzt.jiv.common.input.MouseUtil;
import eakerzt.jiv.gui.input.handlers.CombinedDragHandler;
import eakerzt.jiv.gui.input.handlers.NullDragHandler;
import eakerzt.jiv.gui.input.handlers.NullInputHandler;
import eakerzt.jiv.gui.input.handlers.ProxyDragHandler;
import eakerzt.jiv.gui.input.handlers.ProxyInputHandler;
import eakerzt.jiv.gui.overlay.ingredients.IngredientGridWithNavigation;
import eakerzt.jiv.gui.overlay.ingredients.IIngredientGridSource;
import eakerzt.jiv.gui.overlay.ingredients.IngredientGridLayout;
import eakerzt.jiv.gui.overlay.IScreenPropertiesUpdater;
import eakerzt.jiv.gui.overlay.GuiPropertiesCache;
import eakerzt.jiv.gui.overlay.bookmarks.history.LookupHistoryOverlay;
import eakerzt.jiv.gui.overlay.elements.IElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

public class BookmarkOverlay implements IRecipeFocusSource, IBookmarkOverlay {
	private static final int BORDER_MARGIN = 6;
	private static final int INNER_PADDING = 2;
	private static final int LOOKUP_HISTORY_BOTTOM_PADDING = BORDER_MARGIN;
	private static final int LOOKUP_HISTORY_PADDING_EXTRA = LOOKUP_HISTORY_BOTTOM_PADDING - INNER_PADDING;

	// input
	private final BookmarkDragManager bookmarkDragManager;
	private final BookmarkGroupController groupController;

	// areas
	private final GuiPropertiesCache<Screen> guiPropertiesCache;

	// display elements
	private final IngredientGridWithNavigation contents;
	private final LookupHistoryOverlay lookupHistoryOverlay;

	// data
	private final BookmarkList bookmarkList;
	private final IClientToggleState toggleState;
	private final IClientConfig clientConfig;
	private final IIngredientGridConfig bookmarkListConfig;
	private final BookmarkPreviewTooltipController previewTooltipController;
	private boolean screenPropertiesDirty;

	public BookmarkOverlay(
		BookmarkList bookmarkList,
		RecipeTransferService recipeTransferService,
		IngredientGridWithNavigation contents,
		LookupHistoryOverlay lookupHistoryOverlay,
		IClientToggleState toggleState,
		IClientConfig clientConfig,
		IIngredientGridConfig bookmarkListConfig,
		IScreenHelper screenHelper,
		IInternalKeyMappings keyBindings
	) {
		this.bookmarkList = bookmarkList;
		this.toggleState = toggleState;
		this.clientConfig = clientConfig;
		this.bookmarkListConfig = bookmarkListConfig;
		this.contents = contents;
		var workspaceNavigation = new eakerzt.jiv.gui.bookmarks.BookmarkWorkspaceNavigation(bookmarkList, contents.getPageDelegate(), contents::updateLayoutToFirstPage);
		this.contents.configureNavigation(workspaceNavigation, workspaceNavigation::label);
		this.contents.setNavigationLeadingButton(new eakerzt.jiv.gui.elements.IconButton(new BookmarkModeButtonController(bookmarkList)));
		this.groupController = new BookmarkGroupController(bookmarkList,contents,this);
		this.lookupHistoryOverlay = lookupHistoryOverlay;
		this.guiPropertiesCache = new GuiPropertiesCache<>(
			screen -> screenHelper.getGuiProperties(screen)
				.orElse(null)
		);
		this.bookmarkDragManager = new BookmarkDragManager(this);
		this.previewTooltipController = new BookmarkPreviewTooltipController(this, recipeTransferService);
		toggleState.setBookmarkEnabled(clientConfig.bookmarkEnabled().get());
		Internal.registerRuntimeListenerRemoval(clientConfig.bookmarkEnabled().addListener(value -> {
			toggleState.setBookmarkEnabled(clientConfig.bookmarkEnabled().get());
			markScreenPropertiesDirty();
		}));
		Internal.registerRuntimeListenerRemoval(toggleState.addBookmarkEnabledListener(value -> clientConfig.bookmarkEnabled().set(value)));
		bookmarkList.addSourceListChangedListener(() -> {
			markScreenPropertiesDirty();
		});
		lookupHistoryOverlay.getLookupHistory().addSourceListChangedListener(this::markScreenPropertiesDirty);

		Internal.registerRuntimeListenerRemoval(clientConfig.lookupHistoryEnabled().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(clientConfig.maxLookupHistoryRows().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(clientConfig.lookupHistoryDisplaySide().addListener(v -> markScreenPropertiesDirty()));
		addGridConfigListeners(bookmarkListConfig);
	}

	public boolean isListDisplayed() {
		updateScreenPropertiesIfDirty();
		return toggleState.isBookmarkOverlayEnabled() &&
			guiPropertiesCache.hasValidScreen() &&
			contents.hasRoom() &&
			!bookmarkList.isEmpty();
	}

	public boolean hasRoom() {
		updateScreenPropertiesIfDirty();
		return contents.hasRoom();
	}

	private void markScreenPropertiesDirty() {
		this.screenPropertiesDirty = true;
	}

	private void addGridConfigListeners(IIngredientGridConfig gridConfig) {
		Internal.registerRuntimeListenerRemoval(gridConfig.maxColumns().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.maxRows().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.drawBackground().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.layoutMode().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.navigationMode().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.horizontalAlignment().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.verticalAlignment().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.navigationVisibility().addListener(v -> markScreenPropertiesDirty()));
	}

	private void updateScreenPropertiesIfDirty() {
		if (this.screenPropertiesDirty) {
			this.screenPropertiesDirty = false;
			Minecraft minecraft = Minecraft.getInstance();
			this.getScreenPropertiesUpdater()
				.updateScreen(minecraft.screen)
				.forceUpdate();
		}
	}

	public IScreenPropertiesUpdater getScreenPropertiesUpdater() {
		return this.guiPropertiesCache.createUpdater(this::onGuiPropertiesChanged);
	}

	private void onGuiPropertiesChanged() {
		IGuiProperties guiProperties = this.guiPropertiesCache.getGuiProperties();
		if (guiProperties == null) {
			this.contents.close();
			this.lookupHistoryOverlay.close();
			return;
		}
		updateBounds(guiProperties, this.guiPropertiesCache.getGuiExclusionAreas());
	}

	private void updateBounds(IGuiProperties guiProperties, Set<ImmutableRect2i> guiExclusionAreas) {
		ImmutableRect2i displayArea = getDisplayArea(guiProperties);
		ImmutablePoint2i mouseExclusionArea = this.guiPropertiesCache.getMouseExclusionArea();
		ImmutableRect2i availableContentsArea = displayArea;
		Optional<ImmutableRect2i> historyArea = Optional.empty();
		if (clientConfig.lookupHistoryEnabled().get() && lookupHistoryOverlay.isDisplayedOnThisSide()) {
			int lookupHistoryDisplayHeight = lookupHistoryOverlay.getDisplayHeight();
			if (lookupHistoryDisplayHeight > 0) {
				ImmutableRect2i area = displayArea
					.insetBy(BORDER_MARGIN)
					.cropBottom(LOOKUP_HISTORY_BOTTOM_PADDING)
					.keepBottom(lookupHistoryDisplayHeight);
				historyArea = Optional.of(area);
				availableContentsArea = cropBottomTo(
					availableContentsArea,
					area.y() - LOOKUP_HISTORY_PADDING_EXTRA
				);
			}
		}
		IElement<?> pageAnchorElement = this.contents.getPageAnchorElement();
        this.contents.updateBounds(availableContentsArea.cropLeft(BookmarkGroupController.GUTTER), guiExclusionAreas, mouseExclusionArea);
        this.bookmarkList.setLayoutColumns(this.contents.getIngredientGridArea().width() / IngredientGridLayout.INGREDIENT_WIDTH);
		this.contents.updateLayoutKeepingPageAnchorVisible(pageAnchorElement);

		historyArea.ifPresent(area -> {
			this.lookupHistoryOverlay.updateBounds(alignLookupHistoryArea(area), guiExclusionAreas, mouseExclusionArea);
			this.lookupHistoryOverlay.updateLayout();
		});

	}

	private ImmutableRect2i alignLookupHistoryArea(ImmutableRect2i lookupHistoryArea) {
		ImmutableRect2i ingredientGridArea = this.contents.getIngredientGridArea();
		if (ingredientGridArea.isEmpty()) {
			return lookupHistoryArea;
		}
		return lookupHistoryArea.matchWidthAndX(ingredientGridArea);
	}

	private static ImmutableRect2i getDisplayArea(IGuiProperties guiProperties) {
		int width = guiProperties.guiLeft();
		if (width <= 0) {
			width = 0;
		}
		int screenHeight = guiProperties.screenHeight();
		return new ImmutableRect2i(0, 0, width, screenHeight);
	}

	private static ImmutableRect2i cropBottomTo(ImmutableRect2i area, int bottomY) {
		int cropAmount = getBottom(area) - bottomY;
		if (cropAmount <= 0) {
			return area;
		}
		return area.cropBottom(cropAmount);
	}

	private static int getBottom(ImmutableRect2i area) {
		return area.y() + area.height();
	}

	public void drawScreen(Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		updateScreenPropertiesIfDirty();
		drawBackground(guiGraphics);
		drawForeground(minecraft, guiGraphics, mouseX, mouseY, partialTicks);
	}

	public void drawBackground(GuiGraphicsExtractor guiGraphics) {
		updateScreenPropertiesIfDirty();
		if (isListDisplayed()) {
			this.contents.drawBackground(guiGraphics);
		}
		if (guiPropertiesCache.hasValidScreen() && toggleState.isOverlayEnabled()) {
			this.lookupHistoryOverlay.drawBackground(guiGraphics);
		}
	}

	public void drawForeground(Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		updateScreenPropertiesIfDirty();
		if (isListDisplayed()) {
			this.groupController.updateGroupDrag(mouseX, mouseY);
            this.bookmarkDragManager.updateDrag(mouseX, mouseY);
			drawPageFlipEdgeHighlights(guiGraphics, mouseX, mouseY);
			this.groupController.drawRecipeBackground(guiGraphics, mouseX, mouseY);
			this.contents.drawForeground(minecraft, guiGraphics, mouseX, mouseY, partialTicks);
			this.groupController.draw(guiGraphics,mouseX,mouseY);
		}
		if (guiPropertiesCache.hasValidScreen() && toggleState.isOverlayEnabled()) {
			this.lookupHistoryOverlay.draw(minecraft, guiGraphics, mouseX, mouseY, partialTicks);
		}
	}

	private void drawPageFlipEdgeHighlights(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (!this.bookmarkDragManager.isDragging() || !canFlipPage()) {
			return;
		}
		PageFlipHover.Direction hoveredDirection = getHoveredPageEdge(mouseX, mouseY);
		drawPageFlipEdgeHighlight(guiGraphics, getNextPageEdgeArea(), hoveredDirection == PageFlipHover.Direction.NEXT);
		drawPageFlipEdgeHighlight(guiGraphics, getBackPageEdgeArea(), hoveredDirection == PageFlipHover.Direction.PREVIOUS);
	}

	private static void drawPageFlipEdgeHighlight(GuiGraphicsExtractor guiGraphics, ImmutableRect2i area, boolean hovered) {
		GuiColor color;
		if (hovered) {
			color = GuiColor.BOOKMARK_DRAG_PAGE_FLIP_HIGHLIGHT;
		} else {
			color = GuiColor.BOOKMARK_DRAG_PAGE_FLIP_HINT;
		}
		guiGraphics.fill(
			area.getX(),
			area.getY(),
			area.getX() + area.getWidth(),
			area.getY() + area.getHeight(),
			JivGuiColors.getColor(color)
		);
	}

	public BookmarkPreviewTooltipController getPreviewTooltipController() {
		return previewTooltipController;
	}

	Stream<PreviewSource> getPreviewSourcesUnderMouse(double mouseX, double mouseY) {
		Stream<PreviewSource> bookmarkSources = contents.getIngredientUnderMouse(mouseX, mouseY)
			.map(ingredient -> new PreviewSource(ingredient, bookmarkList, this::isListDisplayed));
		IIngredientGridSource lookupHistory = lookupHistoryOverlay.getLookupHistory();
		Stream<PreviewSource> lookupHistorySources = lookupHistoryOverlay.getIngredientUnderMouse(mouseX, mouseY)
			.map(ingredient -> new PreviewSource(ingredient, lookupHistory, lookupHistoryOverlay::isListDisplayed));
		return Stream.concat(bookmarkSources, lookupHistorySources);
	}

	record PreviewSource(
		IClickableIngredientInternal<?> ingredient,
		IIngredientGridSource owner,
		BooleanSupplier ownerDisplayed
	) {
		boolean isPresentAndVisible() {
			IElement<?> element = ingredient.getElement();
			return ownerDisplayed.getAsBoolean() &&
				element.isVisible() &&
				owner.containsElement(element);
		}
	}

	public void drawTooltips(Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		updateScreenPropertiesIfDirty();
		if (!this.bookmarkDragManager.drawDraggedItem(guiGraphics, mouseX, mouseY)) {
			if (isListDisplayed() && !previewTooltipController.isVisible()) {
				this.contents.drawTooltips(minecraft, guiGraphics, mouseX, mouseY);
				this.groupController.drawTooltip(guiGraphics,mouseX,mouseY);
			}
			if (guiPropertiesCache.hasValidScreen() && toggleState.isOverlayEnabled()) {
				this.lookupHistoryOverlay.drawTooltips(minecraft, guiGraphics, mouseX, mouseY);
			}
		}
	}

	public void tick() {
		if (isListDisplayed()) {
			this.contents.tick();
		}
		if (guiPropertiesCache.hasValidScreen() && toggleState.isOverlayEnabled()) {
			this.lookupHistoryOverlay.tick();
		}
	}

	@Override
	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		updateScreenPropertiesIfDirty();
		if (isListDisplayed()) {
			return Stream.concat(this.contents.getIngredientUnderMouse(mouseX, mouseY), this.lookupHistoryOverlay.getIngredientUnderMouse(mouseX, mouseY));
		}
		if (this.lookupHistoryOverlay.isListDisplayed()) {
			return this.lookupHistoryOverlay.getIngredientUnderMouse(mouseX, mouseY);
		}
		return Stream.empty();
	}

	@Override
	public Stream<IDraggableIngredientInternal<?>> getDraggableIngredientUnderMouse(double mouseX, double mouseY) {
		updateScreenPropertiesIfDirty();
		if (isListDisplayed()) {
			return Stream.concat(this.contents.getDraggableIngredientUnderMouse(mouseX, mouseY), this.lookupHistoryOverlay.getDraggableIngredientUnderMouse(mouseX, mouseY));
		}
		if (this.lookupHistoryOverlay.isListDisplayed()) {
			return this.lookupHistoryOverlay.getDraggableIngredientUnderMouse(mouseX, mouseY);
		}
		return Stream.empty();
	}

	@Override
	public Optional<ITypedIngredient<?>> getIngredientUnderMouse() {
		double mouseX = MouseUtil.getX();
		double mouseY = MouseUtil.getY();
		return getIngredientUnderMouse(mouseX, mouseY)
			.<ITypedIngredient<?>>map(IClickableIngredientInternal::getTypedIngredient)
			.findFirst();
	}

	@Nullable
	@Override
	public <T> T getIngredientUnderMouse(IIngredientType<T> ingredientType) {
		double mouseX = MouseUtil.getX();
		double mouseY = MouseUtil.getY();
		return getIngredientUnderMouse(mouseX, mouseY)
			.map(IClickableIngredientInternal::getTypedIngredient)
			.map(i -> i.getIngredient(ingredientType))
			.flatMap(Optional::stream)
			.findFirst()
			.orElse(null);
	}

    public IUserInputHandler createGroupInputHandler() {
        return new ProxyInputHandler(() -> isListDisplayed() ? groupController : NullInputHandler.INSTANCE);
    }

	public IUserInputHandler createGroupScrollInputHandler() {
		return new IUserInputHandler() {
			@Override
			public Optional<IUserInputHandler> handleUserInput(Screen screen, IGuiProperties properties, UserInput input, IInternalKeyMappings keys) {
				return Optional.empty();
			}

			@Override
			public Optional<IUserInputHandler> handleMouseScrolled(double x, double y, double dx, double dy) {
				return isListDisplayed() && !previewTooltipController.isMouseOver(x, y) ? groupController.handleMouseScrolled(x, y, dx, dy) : Optional.empty();
			}
		};
	}

	public IUserInputHandler createInputHandler() {
		final IUserInputHandler displayedInputHandler = this.contents.createInputHandler();
		return new ProxyInputHandler(() -> isListDisplayed() ? displayedInputHandler : NullInputHandler.INSTANCE);
	}

	public IUserInputHandler createDeleteItemInputHandler() {
		final IUserInputHandler deleteItemInputHandler = this.contents.createDeleteItemInputHandler();

		return new ProxyInputHandler(() -> {
			if (isListDisplayed()) {
				return deleteItemInputHandler;
			}
			return NullInputHandler.INSTANCE;
		});
	}

	public IDragHandler createDragHandler() {
		final IDragHandler lookupHistoryDragHandler = this.lookupHistoryOverlay.createDragHandler();
		final IDragHandler combinedDragHandlers = new CombinedDragHandler(
            this.groupController.createDragHandler(),
			this.bookmarkDragManager.createDragHandler(),
			this.contents.createDragHandler(),
			lookupHistoryDragHandler
		);

		return new ProxyDragHandler(() -> {
			if (isListDisplayed()) {
				return combinedDragHandlers;
			}
			if (lookupHistoryOverlay.isListDisplayed()) {
				return lookupHistoryDragHandler;
			}
			return NullDragHandler.INSTANCE;
		});
	}

	public void drawOnForeground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		updateScreenPropertiesIfDirty();
		if (isListDisplayed()) {
			this.contents.drawOnForeground(guiGraphics, mouseX, mouseY);
		}
		this.lookupHistoryOverlay.drawOnForeground(guiGraphics, mouseX, mouseY);
	}

	public List<BookmarkDragTarget> createBookmarkDragTargets(IBookmark draggedBookmark) {
		updateScreenPropertiesIfDirty();
		List<IElement<?>> elements = this.bookmarkList.getBookmarkElements();
        List<BookmarkDragTarget> targets = new java.util.ArrayList<>(
            BookmarkDragTarget.createSlotTargets(contents.getAllSlots(), elements, draggedBookmark));
        List<IBookmark> pageBookmarks = contents.getPageElements().stream()
            .flatMap(e -> e.getBookmark().stream()).toList();
        if (pageBookmarks.isEmpty()) return targets;
        int firstIndex = BookmarkDragTarget.ownerIndex(elements, pageBookmarks.getFirst());
        int lastIndex = BookmarkDragTarget.ownerIndex(elements, pageBookmarks.getLast());
        if (firstIndex < 0 || lastIndex < 0) return targets;

		if (canFlipPage()) {
			targets.add(new BookmarkDragTarget(this.contents.getNextPageButtonArea(), (lastIndex + 1) % elements.size()));
			targets.add(new BookmarkDragTarget(this.contents.getBackButtonArea(), Math.floorMod(firstIndex - 1, elements.size())));
		}

		// The lower edge of the final subgroup is an explicit outside-group destination.
        if (lastIndex == elements.size() - 1 && bookmarkList.state(pageBookmarks.getLast()).group != 0) {
            int bottom = contents.getAllSlots().stream().filter(slot -> !slot.isBlocked() && slot.getOptionalElement().isPresent())
                .mapToInt(slot -> slot.getArea().y() + slot.getArea().height()).max().orElse(0);
            var area = contents.getSlotBackgroundArea();
            int top = Math.max(area.y(), bottom - 4);
            targets.addFirst(new BookmarkDragTarget(new ImmutableRect2i(area.x(), top, area.width(), Math.max(0, area.y() + area.height() - top)), lastIndex, 0));
            targets.add(new BookmarkDragTarget(area, lastIndex, 0));
        } else targets.add(new BookmarkDragTarget(contents.getSlotBackgroundArea(), lastIndex));
		return targets;
	}

    public record DraggedCell(eakerzt.jiv.api.ingredients.ITypedIngredient<?> ingredient, int x, int y) {}

    public List<DraggedCell> captureDraggedCells(IBookmark bookmark, eakerzt.jiv.gui.bookmarks.@org.jspecify.annotations.Nullable BookmarkCell<?> source) {
        List<DraggedCell> cells = new java.util.ArrayList<>();
        int firstX = 0, firstY = 0;
        for (var slot : contents.getAllSlots()) {
            if (slot.isBlocked()) continue;
            var element = slot.getOptionalElement().orElse(null);
            if (element == null || element.getBookmark().orElse(null) != bookmark) continue;
            if (source != null && (!(element instanceof eakerzt.jiv.gui.bookmarks.BookmarkCell<?> cell) || cell.slot != source.slot)) continue;
            var area = slot.getRenderArea();
            if (cells.isEmpty()) { firstX = area.x(); firstY = area.y(); }
            cells.add(new DraggedCell(element.getTypedIngredient(), area.x() - firstX, area.y() - firstY));
        }
        return cells;
    }

    public void beginDrag(IBookmark bookmark, int inputSlot) {
        contents.setKeepPositionOnRelayout(inputSlot >= 0);
        bookmarkList.beginDrag(bookmark, inputSlot);
    }
    public void finishDrag(boolean commit) {
        bookmarkList.finishDrag(commit);
        // Flush the final input order while the viewport is still fixed.
        contents.setKeepPositionOnRelayout(false);
    }

    public boolean previewDrag(IBookmark bookmark, eakerzt.jiv.gui.bookmarks.@org.jspecify.annotations.Nullable BookmarkCell<?> source, double x, double y) {
        if (source != null) return moveRecipeInput(source, x, y);
        for (BookmarkDragTarget target : createBookmarkDragTargets(bookmark)) {
            if (target.area().contains(x, y)) {
                if (target.group() >= 0) bookmarkList.moveBookmarkToGroup(bookmark, target.index(), target.group());
                else bookmarkList.moveBookmark(bookmark, target.index());
                return true;
            }
        }
        return false;
    }

    public boolean moveRecipeInput(eakerzt.jiv.gui.bookmarks.BookmarkCell<?> source, double x, double y) {
        for (var slot : contents.getAllSlots()) {
            if (slot.isBlocked() || !slot.getArea().contains(x, y)) continue;
            var element = slot.getOptionalElement().orElse(null);
            if (element instanceof eakerzt.jiv.gui.bookmarks.BookmarkDragPlaceholder<?> placeholder) element = placeholder.source();
            if (element instanceof eakerzt.jiv.gui.bookmarks.BookmarkCell<?> target && source.bookmark != null && target.bookmark == source.bookmark && target.role == eakerzt.jiv.api.recipe.RecipeIngredientRole.INPUT) {
                if (source.slot == target.slot) return true;
                return bookmarkList.moveRecipeInput(source.bookmark, source.slot, target.slot);
            }
        }
        return false;
    }

	public void moveBookmark(IBookmark bookmark, int index) {
		this.bookmarkList.moveBookmark(bookmark, index);
		// Keep the dropped bookmark visible when its visibility and the cursor's slot are restored.
		this.bookmarkList.getElements().stream().filter(e->e.getBookmark().filter(b->b==bookmark).isPresent()).findFirst()
            .ifPresent(this.contents::setPageAnchorElement);
	}

	public IPaged getPageDelegate() {
		return this.contents.getPageDelegate();
	}

	public void setPageButtonsForcePressed(boolean nextButton, boolean backButton) {
		this.contents.setPageButtonsForcePressed(nextButton, backButton);
	}

	private ImmutableRect2i getNextPageEdgeArea() {
		return this.contents.getSlotBackgroundArea()
			.keepRight(IngredientGridLayout.INGREDIENT_WIDTH / 2);
	}

	private ImmutableRect2i getBackPageEdgeArea() {
		return this.contents.getSlotBackgroundArea()
			.keepLeft(IngredientGridLayout.INGREDIENT_WIDTH / 2);
	}

	PageFlipHover.@Nullable Direction getHoveredPageEdge(double mouseX, double mouseY) {
		if (!canFlipPage()) {
			return null;
		}
		if (MathUtil.contains(getNextPageEdgeArea(), mouseX, mouseY)) {
			return PageFlipHover.Direction.NEXT;
		}
		if (MathUtil.contains(getBackPageEdgeArea(), mouseX, mouseY)) {
			return PageFlipHover.Direction.PREVIOUS;
		}
		return null;
	}

	private boolean canFlipPage() {
		return !this.bookmarkListConfig.navigationMode().get().usesScrollbar() &&
			getPageDelegate().getPageCount() > 1;
	}

	void scrollDuringDrag(BookmarkDragScroll dragScroll, double mouseX, double mouseY) {
		double pixels = dragScroll.update(this.contents.getSlotBackgroundArea(), this.bookmarkListConfig.navigationMode().get(), mouseX, mouseY);
		if (pixels != 0) {
			this.contents.scrollByPixels(pixels);
		}
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.contents.isMouseOver(mouseX, mouseY);
	}

	public boolean isBookmarkElementUnderMouse(IElement<?> element, double mouseX, double mouseY) {
		return isListDisplayed() &&
			element.isVisible() &&
			bookmarkList.containsElement(element) &&
			contents.getIngredientUnderMouse(mouseX, mouseY)
				.anyMatch(ingredient -> ingredient.getElement() == element);
	}
}
