package eakerzt.jiv.gui.overlay.ingredients;

import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.api.runtime.IScreenHelper;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.config.IClientConfig;
import eakerzt.jiv.common.config.IClientToggleState;
import eakerzt.jiv.common.config.IIngredientGridConfig;
import eakerzt.jiv.common.gui.elements.ScalableDrawable;
import eakerzt.jiv.common.network.IConnectionToServer;
import eakerzt.jiv.common.util.ImmutablePoint2i;
import eakerzt.jiv.common.util.ImmutableRect2i;
import eakerzt.jiv.gui.PageNavigation;
import eakerzt.jiv.gui.ghost.GhostIngredientDragManager;
import eakerzt.jiv.gui.ghost.GhostIngredientQuickMoveManager;
import eakerzt.jiv.gui.input.IClickableIngredientInternal;
import eakerzt.jiv.gui.input.IDragHandler;
import eakerzt.jiv.gui.input.IDraggableIngredientInternal;
import eakerzt.jiv.gui.input.IPaged;
import eakerzt.jiv.common.input.IUserInputHandler;
import eakerzt.jiv.common.input.handlers.CombinedInputHandler;
import eakerzt.jiv.gui.overlay.elements.IElement;
import eakerzt.jiv.gui.util.CommandUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Displays a list of ingredients with navigation at the top.
 */
public class IngredientGridWithNavigation implements IIngredientListOverlayContents {
	private final IngredientGridWithNavigationController controller;
	private final PageNavigation navigation;
	private final IngredientGridScrollbar scrollbar;
	private final IIngredientGridConfig gridConfig;
	private final IngredientGrid ingredientGrid;
	private final IIngredientGridSource ingredientSource;
	private final ScalableDrawable background;
	private final ScalableDrawable slotBackground;
	private final ScalableDrawable exclusionAreaShadow;
	private final GhostIngredientDragManager ghostIngredientDragManager;
	private final IUserInputHandler inputHandler;

	private ImmutableRect2i backgroundArea = ImmutableRect2i.EMPTY;
	private ImmutableRect2i slotBackgroundArea = ImmutableRect2i.EMPTY;
	@Nullable
	private ImmutableRect2i availableArea;
	private Set<ImmutableRect2i> guiExclusionAreas = Set.of();
	@Nullable
	private ImmutablePoint2i mouseExclusionPoint;
	private boolean active;
	private boolean layoutDirty;
	private boolean workspaceNavigation;

	public IngredientGridWithNavigation(
		String debugName,
		IIngredientGridSource ingredientSource,
		IngredientGrid ingredientGrid,
		IClientToggleState toggleState,
		IClientConfig clientConfig,
		IConnectionToServer serverConnection,
		IIngredientGridConfig gridConfig,
		ScalableDrawable background,
		ScalableDrawable slotBackground,
		ScalableDrawable exclusionAreaShadow,
		IScreenHelper screenHelper,
		IIngredientManager ingredientManager
	) {
		this.ingredientGrid = ingredientGrid;
		this.ingredientSource = ingredientSource;
		this.gridConfig = gridConfig;
		this.background = background;
		this.slotBackground = slotBackground;
		this.exclusionAreaShadow = exclusionAreaShadow;
		CommandUtil commandUtil = new CommandUtil(clientConfig, serverConnection);
		this.ghostIngredientDragManager = new GhostIngredientDragManager(this.ingredientGrid, screenHelper, ingredientManager, toggleState);
		GhostIngredientQuickMoveManager ghostIngredientQuickMoveManager = new GhostIngredientQuickMoveManager(this.ingredientGrid, screenHelper);
		this.controller = new IngredientGridWithNavigationController(
			ingredientSource,
			this.ingredientGrid,
			gridConfig,
			toggleState,
			clientConfig,
			commandUtil,
			ingredientManager,
			this::isMouseOver,
			ghostIngredientQuickMoveManager
		);
		this.navigation = new PageNavigation(this.controller, false);
		this.scrollbar = new IngredientGridScrollbar(this.controller);
		this.controller.setOnLayoutChanged(this.navigation::updatePageNumber);
		this.inputHandler = new CombinedInputHandler(
			debugName,
			this.scrollbar,
			this.controller,
			this.navigation.createInputHandler()
		);

		this.ingredientSource.addSourceListChangedListener(this::markLayoutDirty);
		addGridConfigListeners(gridConfig);
	}

	private void addGridConfigListeners(IIngredientGridConfig gridConfig) {
		Internal.registerRuntimeListenerRemoval(gridConfig.maxColumns().addListener(v -> markLayoutDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.maxRows().addListener(v -> markLayoutDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.drawBackground().addListener(v -> markLayoutDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.layoutMode().addListener(v -> markLayoutDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.navigationMode().addListener(v -> markLayoutDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.horizontalAlignment().addListener(v -> markLayoutDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.verticalAlignment().addListener(v -> markLayoutDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.navigationVisibility().addListener(v -> markLayoutDirty()));
	}

	private boolean keepPositionOnRelayout;

    /** Sorting inputs must not follow an ingredient anchor into a different row or page. */
    public void setKeepPositionOnRelayout(boolean keepPosition) {
        updateLayoutIfDirty();
        keepPositionOnRelayout = keepPosition;
    }

    private void markLayoutDirty() {
		this.layoutDirty = true;
	}

	private void updateLayoutIfDirty() {
		if (this.layoutDirty && this.availableArea != null) {
			IElement<?> pageAnchorElement = getPageAnchorElement();
			updateBounds(this.availableArea, this.guiExclusionAreas, this.mouseExclusionPoint);
			if (keepPositionOnRelayout) this.controller.updateLayoutKeepingPosition();
            else this.controller.updateLayoutKeepingPageAnchorVisible(pageAnchorElement);
        }
    }

	@Override
	public boolean hasRoom() {
		updateLayoutIfDirty();
		return this.active;
	}

	@Override
	public void updateLayoutToFirstPage() {
		this.controller.updateLayoutToFirstPage();
	}

	@Override
	public void updateLayoutKeepingPageAnchorVisible(@Nullable IElement<?> pageAnchorElement) {
		this.controller.updateLayoutKeepingPageAnchorVisible(pageAnchorElement);
	}

	public void setPageAnchorElement(IElement<?> pageAnchorElement) {
		this.controller.setPageAnchorElement(pageAnchorElement);
	}

	public List<IElement<?>> getPageElements() {
		updateLayoutIfDirty();
		return this.controller.getPageElements();
	}

	@Override
	public @Nullable IElement<?> getPageAnchorElement() {
		return this.controller.getPageAnchorElement();
	}

	@Override
	public void updateBounds(final ImmutableRect2i availableArea, Set<ImmutableRect2i> guiExclusionAreas, @Nullable ImmutablePoint2i mouseExclusionPoint) {
		this.availableArea = availableArea;
		this.guiExclusionAreas = guiExclusionAreas;
		this.mouseExclusionPoint = mouseExclusionPoint;
		this.layoutDirty = false;
		IngredientGridWithNavigationLayout layout = calculateLayout(
			availableArea,
			guiExclusionAreas,
			this.ingredientSource.getElements().size()
		);
		applyLayout(layout, guiExclusionAreas, mouseExclusionPoint);
	}

	private IngredientGridWithNavigationLayout calculateLayout(
		final ImmutableRect2i availableArea,
		Set<ImmutableRect2i> guiExclusionAreas,
		int ingredientCount
	) {
		if (this.gridConfig.navigationMode().get().usesScrollbar()) {
			var layout = IngredientGridScrollbarLayout.calculate(
				this.gridConfig,
				workspaceNavigation ? availableArea.cropTop(IngredientGridWithNavigationLayout.NAVIGATION_HEIGHT + IngredientGridWithNavigationLayout.INNER_PADDING) : availableArea,
				guiExclusionAreas,
				ingredientCount
			);
			if (workspaceNavigation && layout.hasRoom()) {
				var navigationArea = IngredientGridWithNavigationLayout.calculateNavigationArea(layout.slotBackgroundArea(), true);
				return IngredientGridWithNavigationLayout.fromGridArea(this.gridConfig,
						layout.ingredientGridArea(), layout.availableSlotCount(), navigationArea,
						navigationArea, true, layout.scrollbarArea(), layout.scrollbarEnabled());
			}
			return layout;
		}
		if (workspaceNavigation) return IngredientGridButtonNavigationLayout.calculateWithNavigation(this.gridConfig, availableArea, guiExclusionAreas);

		return IngredientGridButtonNavigationLayout.calculate(
			this.gridConfig,
			availableArea,
			guiExclusionAreas,
			ingredientCount
		);
	}

	private void applyLayout(
		IngredientGridWithNavigationLayout layout,
		Set<ImmutableRect2i> guiExclusionAreas,
		@Nullable ImmutablePoint2i mouseExclusionPoint
	) {
		this.guiExclusionAreas = guiExclusionAreas;
		if (!layout.hasRoom()) {
			clearLayout();
			return;
		}

		this.ingredientGrid.updateBounds(layout.ingredientGridArea(), guiExclusionAreas, mouseExclusionPoint);
		this.slotBackgroundArea = layout.slotBackgroundArea();
		this.navigation.updateBounds(layout.navigationArea());
		this.scrollbar.updateBounds(layout.scrollbarArea());
		this.backgroundArea = layout.backgroundArea();
		this.active = true;
	}

	private void clearLayout() {
		this.ingredientGrid.updateBounds(ImmutableRect2i.EMPTY, Set.of(), null);
		this.slotBackgroundArea = ImmutableRect2i.EMPTY;
		this.navigation.updateBounds(ImmutableRect2i.EMPTY);
		this.scrollbar.updateBounds(ImmutableRect2i.EMPTY);
		this.backgroundArea = ImmutableRect2i.EMPTY;
		this.active = false;
	}

	@Override
	public ImmutableRect2i getBackgroundArea() {
		updateLayoutIfDirty();
		return this.backgroundArea;
	}

	@Override
	public ImmutableRect2i getIngredientGridArea() {
		updateLayoutIfDirty();
		return this.ingredientGrid.getArea();
	}

	public ImmutableRect2i getSlotBackgroundArea() {
		updateLayoutIfDirty();
		return this.slotBackgroundArea;
	}

	public ImmutableRect2i getNextPageButtonArea() {
		updateLayoutIfDirty();
		return this.navigation.getNextButtonArea();
	}

	public ImmutableRect2i getBackButtonArea() {
		updateLayoutIfDirty();
		return this.navigation.getBackButtonArea();
	}

	public IPaged getPageDelegate() {
		return controller;
	}

	public void configureNavigation(IPaged paged, java.util.function.Supplier<String> displayText) {
		this.workspaceNavigation = true;
		this.navigation.configure(paged, displayText);
		markLayoutDirty();
	}

	public void setNavigationLeadingButton(eakerzt.jiv.gui.elements.IconButton button) {
		this.navigation.setLeadingButton(button);
		markLayoutDirty();
	}

	public ImmutableRect2i getNavigationLeadingButtonArea() {
		updateLayoutIfDirty();
		return this.navigation.getLeadingButtonArea();
	}

	public void scrollByPixels(double pixels) {
		updateLayoutIfDirty();
		this.controller.scrollByPixels(pixels);
	}

	public void setPageButtonsForcePressed(boolean nextButton, boolean backButton) {
		this.navigation.setForcePressed(nextButton, backButton);
	}

	@Override
	public void drawBackground(GuiGraphicsExtractor guiGraphics) {
		updateLayoutIfDirty();
		if (!this.active) {
			return;
		}
		if (this.gridConfig.drawBackground().get()) {
			this.background.draw(guiGraphics, this.backgroundArea);
			this.slotBackground.draw(guiGraphics, this.slotBackgroundArea);
			GuiExclusionAreaShadow.draw(guiGraphics, this.exclusionAreaShadow, this.backgroundArea, this.guiExclusionAreas);
		}
	}

	@Override
	public void drawForeground(Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (!this.active) {
			return;
		}
		this.ingredientGrid.draw(minecraft, guiGraphics, mouseX, mouseY);
		this.scrollbar.draw(guiGraphics, mouseX, mouseY);
		this.navigation.draw(minecraft, guiGraphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public void drawTooltips(Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		updateLayoutIfDirty();
		if (!this.active) {
			return;
		}
		this.ghostIngredientDragManager.drawTooltips(minecraft, guiGraphics, mouseX, mouseY);
		this.ingredientGrid.drawTooltips(minecraft, guiGraphics, mouseX, mouseY);
		this.navigation.drawTooltips(guiGraphics, mouseX, mouseY);
	}

	@Override
	public void tick() {
		if (!this.active) {
			return;
		}
		this.ingredientGrid.tick();
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		updateLayoutIfDirty();
		return this.active &&
			this.backgroundArea.contains(mouseX, mouseY) &&
			this.guiExclusionAreas.stream()
				.noneMatch(area -> area.contains(mouseX, mouseY));
	}

	@Override
	public IUserInputHandler createDeleteItemInputHandler() {
		return this.ingredientGrid.getInputHandler();
	}

	@Override
	public IUserInputHandler createInputHandler() {
		return this.inputHandler;
	}

	@Override
	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		updateLayoutIfDirty();
		if (!this.active) {
			return Stream.empty();
		}
		return this.ingredientGrid.getIngredientUnderMouse(mouseX, mouseY)
			.map(this.controller::createPageAnchorIngredient);
	}

	@Override
	public Stream<IDraggableIngredientInternal<?>> getDraggableIngredientUnderMouse(double mouseX, double mouseY) {
		updateLayoutIfDirty();
		if (!this.active) {
			return Stream.empty();
		}
		return this.ingredientGrid.getDraggableIngredientUnderMouse(mouseX, mouseY);
	}

	@Override
	public <T> Stream<T> getVisibleIngredients(IIngredientType<T> ingredientType) {
		updateLayoutIfDirty();
		if (!this.active) {
			return Stream.empty();
		}
		return this.ingredientGrid.getVisibleIngredients(ingredientType);
	}

	@Override
	public boolean isEmpty() {
		return this.ingredientSource.getElements().isEmpty();
	}

	@Override
	public void close() {
		clearLayout();
		this.ghostIngredientDragManager.stopDrag();
	}

	@Override
	public void drawOnForeground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		updateLayoutIfDirty();
		if (!this.active) {
			return;
		}
		this.ghostIngredientDragManager.drawOnForeground(guiGraphics, mouseX, mouseY);
	}

	@Override
	public IDragHandler createDragHandler() {
		return this.ghostIngredientDragManager.createDragHandler();
	}

	public int size() {
		if (!this.active) {
			return 0;
		}
		return this.ingredientGrid.size();
	}

	public Stream<IngredientListSlot> getSlots() {
		updateLayoutIfDirty();
		if (!this.active) {
			return Stream.empty();
		}
		return this.ingredientGrid.getSlots();
	}

	public List<IngredientListSlot> getAllSlots() {
		updateLayoutIfDirty();
		if (!this.active) {
			return List.of();
		}
		return this.ingredientGrid.getAllSlots();
	}
}
