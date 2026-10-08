package eakerzt.jiv.gui.overlay.bookmarks;

import eakerzt.jiv.api.ingredients.IIngredientRenderer;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.config.IClientConfig;
import eakerzt.jiv.common.input.UserInput;
import eakerzt.jiv.common.util.ImmutableRect2i;
import eakerzt.jiv.common.util.SafeIngredientUtil;
import eakerzt.jiv.gui.bookmarks.BookmarkCell;
import eakerzt.jiv.gui.bookmarks.IBookmark;
import eakerzt.jiv.gui.input.IPaged;
import eakerzt.jiv.gui.overlay.bookmarks.PageFlipHover.Direction;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.phys.Vec2;

import org.jspecify.annotations.Nullable;

public class BookmarkDrag<T> {
	private final BookmarkOverlay bookmarkOverlay;
	private final IIngredientRenderer<T> ingredientRenderer;
	private final ITypedIngredient<T> ingredient;
	private final double mouseStartX;
	private final double mouseStartY;
	private final IBookmark bookmark;
	private final ImmutableRect2i origin;
	private final @Nullable BookmarkCell<?> inputCell;
	private boolean dragging;
	private boolean validPreview;
	private java.util.List<BookmarkOverlay.DraggedCell> draggedCells = java.util.List.of();
	private final long dragCanStartTime;
	private final PageFlipHover pageFlipHover = new PageFlipHover(System::currentTimeMillis);
	private final BookmarkDragScroll dragScroll = new BookmarkDragScroll(System::nanoTime);

	public BookmarkDrag(
			BookmarkOverlay bookmarkOverlay,
			IIngredientRenderer<T> ingredientRenderer,
			ITypedIngredient<T> ingredient,
			IBookmark bookmark,
			double mouseX,
			double mouseY,
			ImmutableRect2i origin,
			@Nullable BookmarkCell<?> inputCell) {
		this.bookmarkOverlay = bookmarkOverlay;
		this.ingredientRenderer = ingredientRenderer;
		this.ingredient = ingredient;
		this.bookmark = bookmark;
		this.origin = origin;
		this.inputCell = inputCell;
		this.mouseStartX = mouseX;
		this.mouseStartY = mouseY;
		IClientConfig clientConfig = Internal.getClientConfigs().getClientConfig();
		this.dragCanStartTime = System.currentTimeMillis() + clientConfig.dragDelayMs().get();
	}

	public static boolean canStart(BookmarkDrag<?> drag, double mouseX, double mouseY) {
		if (System.currentTimeMillis() < drag.dragCanStartTime) {
			return false;
		}
		ImmutableRect2i origin = drag.origin;
		final Vec2 center;
		if (origin.isEmpty()) {
			center = new Vec2((float) drag.mouseStartX, (float) drag.mouseStartY);
		} else {
			if (origin.contains(mouseX, mouseY)) {
				return false;
			}
			center =
					new Vec2(
							origin.getX() + (origin.getWidth() / 2.0f),
							origin.getY() + (origin.getHeight() / 2.0f));
		}

		double mouseXDist = center.x - mouseX;
		double mouseYDist = center.y - mouseY;
		double mouseDistSq = mouseXDist * mouseXDist + mouseYDist * mouseYDist;
		return mouseDistSq > 64.0;
	}

	public void update(int mouseX, int mouseY) {
		if (!dragging && !canStart(this, mouseX, mouseY)) {
			return;
		}

		if (!dragging) {
			draggedCells = bookmarkOverlay.captureDraggedCells(bookmark, inputCell);
			dragging = true;
			bookmarkOverlay.beginDrag(bookmark, inputCell == null ? -1 : inputCell.slot);
		}
		validPreview |= bookmarkOverlay.previewDrag(bookmark, inputCell, mouseX, mouseY);
		// Sorting keeps grid slots fixed. A mouse exclusion would block the target
		// slot and repack unrelated recipes on every mouse move.
		if (inputCell != null) return;

		bookmarkOverlay.scrollDuringDrag(dragScroll, mouseX, mouseY);

		Direction hoveredDirection = bookmarkOverlay.getHoveredPageEdge(mouseX, mouseY);
		Direction flipDirection = pageFlipHover.update(hoveredDirection);
		if (flipDirection != null) {
			IPaged pageDelegate = bookmarkOverlay.getPageDelegate();
			switch (flipDirection) {
				case NEXT -> pageDelegate.nextPage();
				case PREVIOUS -> pageDelegate.previousPage();
			}
		}
		bookmarkOverlay.setPageButtonsForcePressed(
				hoveredDirection == Direction.NEXT, hoveredDirection == Direction.PREVIOUS);
	}

	public boolean isDragging() {
		return dragging;
	}

	public boolean drawItem(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (!dragging) {
			return false;
		}

		if (draggedCells.isEmpty()) {
			SafeIngredientUtil.render(
					guiGraphics, ingredientRenderer, ingredient, mouseX - 8, mouseY - 8);
		} else {
			for (var cell : draggedCells)
				renderCell(
						guiGraphics,
						cell.ingredient(),
						mouseX - 8 + cell.x(),
						mouseY - 8 + cell.y());
		}
		return true;
	}

	static <V> void renderCell(
			GuiGraphicsExtractor graphics, ITypedIngredient<V> value, int x, int y) {
		var renderer =
				Internal.getJivRuntime()
						.getIngredientManager()
						.getIngredientRenderer(value.getType());
		SafeIngredientUtil.render(graphics, renderer, value, x, y);
	}

	public boolean onClick(UserInput input) {
		if (!dragging) {
			return false;
		}

		if (input.isSimulate()) return true;
		boolean success =
				validPreview
						| bookmarkOverlay.previewDrag(
								bookmark, inputCell, input.getMouseX(), input.getMouseY());
		bookmarkOverlay.finishDrag(success);
		stop();
		return success;
	}

	public void stop() {
		bookmarkOverlay.setPageButtonsForcePressed(false, false);
		bookmarkOverlay.finishDrag(false);
		dragging = false;
	}
}
