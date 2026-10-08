package eakerzt.jiv.gui.overlay.bookmarks;

import com.mojang.blaze3d.platform.InputConstants;

import eakerzt.jiv.api.gui.handlers.IGuiProperties;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.gui.JivTooltip;
import eakerzt.jiv.common.input.*;
import eakerzt.jiv.common.util.ImmutableRect2i;
import eakerzt.jiv.gui.bookmarks.*;
import eakerzt.jiv.gui.overlay.elements.IElement;
import eakerzt.jiv.gui.overlay.ingredients.IngredientGridWithNavigation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.*;

/** NEI's seven-pixel gutter, row grouping gestures and quantities. */
public final class BookmarkGroupController implements IUserInputHandler {
	public static final int GUTTER = 7;
	private final BookmarkList bookmarks;
	private final IngredientGridWithNavigation contents;
	private final BookmarkOverlay overlay;
	private final BookmarkDragScroll groupScroll = new BookmarkDragScroll(System::nanoTime);
	private final PageFlipHover groupPageFlip = new PageFlipHover(System::currentTimeMillis);
	private int startRow = -1, endRow = -1, dragButton, startGroup;
	private boolean movingGroup;
	private int draggedGroup = -1;
	private List<BookmarkOverlay.DraggedCell> draggedCells = List.of();

	public eakerzt.jiv.gui.input.IDragHandler createDragHandler() {
		return new eakerzt.jiv.gui.input.IDragHandler() {
			public Optional<eakerzt.jiv.gui.input.IDragHandler> handleDragStart(
					Screen screen, UserInput input) {
				var minecraft = Minecraft.getInstance();
				if (!input.isMouseButton(0)
						|| !minecraft.hasControlDown()
						|| minecraft.hasShiftDown()
						|| minecraft.hasAltDown()
						|| !gutter(input.getMouseX(), input.getMouseY())
						|| !eakerzt.jiv.common.Internal.getClientConfigs()
								.getClientConfig()
								.dragToRearrangeBookmarksEnabled()
								.get()) return Optional.empty();
				int group = group(row(input.getMouseY()));
				if (group == 0 || bookmarks.getGroupBookmarks(group).isEmpty())
					return Optional.empty();
				draggedGroup = group;
				startRow = endRow = -1;
				var cells = new ArrayList<BookmarkOverlay.DraggedCell>();
				int firstX = 0, firstY = 0;
				for (var slot : contents.getAllSlots()) {
					var element = slot.getOptionalElement().orElse(null);
					if (slot.isBlocked()
							|| element == null
							|| element.getBookmark()
									.filter(b -> bookmarks.state(b).group == group)
									.isEmpty()) continue;
					var area = slot.getRenderArea();
					if (cells.isEmpty()) {
						firstX = area.x();
						firstY = area.y();
					}
					cells.add(
							new BookmarkOverlay.DraggedCell(
									element.getTypedIngredient(),
									area.x() - firstX,
									area.y() - firstY));
				}
				draggedCells = cells;
				contents.setKeepPositionOnRelayout(true);
				bookmarks.beginGroupDrag(group);
				return Optional.of(this);
			}

			public boolean handleDragComplete(Screen screen, UserInput input) {
				if (draggedGroup < 0) return false;
				if (input.isSimulate()) return true;
				updateGroupDrag((int) input.getMouseX(), (int) input.getMouseY());
				finishGroupDrag(true);
				return true;
			}

			public void handleDragCanceled() {
				finishGroupDrag(false);
			}
		};
	}

	private void finishGroupDrag(boolean commit) {
		if (draggedGroup < 0) return;
		bookmarks.finishDrag(commit);
		contents.setKeepPositionOnRelayout(false);
		draggedGroup = -1;
		draggedCells = List.of();
		groupPageFlip.update(null);
		contents.setPageButtonsForcePressed(false, false);
	}

	public void updateGroupDrag(int mouseX, int mouseY) {
		if (draggedGroup < 0) return;
		var area = grid();
		overlay.scrollDuringDrag(groupScroll, Math.max(mouseX, area.x() + 1), mouseY);
		PageFlipHover.Direction direction =
				contents.getNextPageButtonArea().contains(mouseX, mouseY)
						? PageFlipHover.Direction.NEXT
						: contents.getBackButtonArea().contains(mouseX, mouseY)
								? PageFlipHover.Direction.PREVIOUS
								: null;
		var flip = groupPageFlip.update(direction);
		if (flip == PageFlipHover.Direction.NEXT) contents.getPageDelegate().nextPage();
		else if (flip == PageFlipHover.Direction.PREVIOUS)
			contents.getPageDelegate().previousPage();
		contents.setPageButtonsForcePressed(
				direction == PageFlipHover.Direction.NEXT,
				direction == PageFlipHover.Direction.PREVIOUS);
		if (mouseX < area.x() - GUTTER || mouseX >= area.x() + area.width()
                || mouseY < area.y() || mouseY >= area.y() + area.height()) return;
		var members = bookmarks.getGroupBookmarks(draggedGroup);
		if (members.isEmpty()) return;
		var elements = bookmarks.getBookmarkElements();
		int sourceIndex = BookmarkDragTarget.ownerIndex(elements, members.getFirst());
		var target =
				rowElements(row(mouseY)).stream()
						.flatMap(e -> e.getBookmark().stream())
						.findFirst()
						.orElse(null);
		if (target == null || bookmarks.state(target).group == draggedGroup) return;
		int targetIndex = BookmarkDragTarget.ownerIndex(elements, target);
		bookmarks.moveGroupRelative(draggedGroup, target, targetIndex > sourceIndex);
	}

	public BookmarkGroupController(
			BookmarkList bookmarks,
			IngredientGridWithNavigation contents,
			BookmarkOverlay overlay) {
		this.bookmarks = bookmarks;
		this.contents = contents;
		this.overlay = overlay;
	}

	private ImmutableRect2i grid() {
		return contents.getIngredientGridArea();
	}

	private int row(double y) {
		var grid = grid();
		return Math.clamp((int) ((y - grid.y()) / 18), 0, Math.max(0, grid.height() / 18 - 1));
	}

	private boolean gutter(double x, double y) {
		var grid = grid();
		return grid.height() > 0
				&& x >= grid.x() - GUTTER
				&& x < grid.x()
				&& y >= grid.y()
				&& y < grid.y() + grid.height();
	}

	private List<IElement<?>> rowElements(int row) {
		int top = grid().y() + row * 18;
		return contents.getAllSlots().stream()
				.filter(s -> !s.isBlocked() && s.getArea().y() == top)
				.flatMap(s -> s.getOptionalElement().stream())
				.toList();
	}

	private int group(int row) {
		return rowElements(row).stream()
				.map(e -> e.getBookmark().map(b -> bookmarks.state(b).group).orElse(0))
				.findFirst()
				.orElse(0);
	}

	private ImmutableRect2i header() {
		var back = contents.getBackButtonArea();
		var next = contents.getNextPageButtonArea();
		var leading = contents.getNavigationLeadingButtonArea();
		int left = leading.isEmpty() ? back.x() + back.width() : leading.x() + leading.width() + 1;
		return new ImmutableRect2i(left, back.y(), Math.max(0, next.x() - left), back.height());
	}

	private Optional<BookmarkCell<?>> cell(double x, double y) {
		return contents.getIngredientUnderMouse(x, y)
				.map(c -> c.getElement())
				.filter(e -> e instanceof BookmarkCell<?>)
				.<BookmarkCell<?>>map(e -> (BookmarkCell<?>) e)
				.findFirst();
	}

	private int hoveredGroup(double x, double y) {
		return gutter(x, y) ? group(row(y)) : header().contains(x, y) ? 0 : -1;
	}

	private void clickGroup(int group, int button) {
		var state = bookmarks.page().groups.get(group);
		if (state == null) return;
		Minecraft minecraft = Minecraft.getInstance();
		if (button == 0 && minecraft.hasAltDown()) state.collapsed = !state.collapsed;
		else if (button == 0) state.todo = !state.todo;
		else if (button == 1) state.linked = !state.linked;
		bookmarks.changed();
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(
			Screen screen, IGuiProperties properties, UserInput input, IInternalKeyMappings keys) {
		double x = input.getMouseX(), y = input.getMouseY();
		Minecraft minecraft = Minecraft.getInstance();
		if (input.getKey().getType() != InputConstants.Type.MOUSE) {
			int group = hoveredGroup(x, y);
			if (group >= 0
					&& keys.getBookmark().isActiveAndMatchesAllowingExtraModifiers(input.getKey())
					&& minecraft.hasShiftDown()
					&& !minecraft.hasControlDown()
					&& !minecraft.hasAltDown()) {
				if (!input.isSimulate()) bookmarks.removeGroup(group);
				return Optional.of(this);
			}
			if (group >= 0 && input.getKey().getValue() == InputConstants.KEY_V) {
				if (!input.isSimulate())
					BookmarkContainerTransfer.pull(bookmarks, group, minecraft.hasShiftDown());
				return Optional.of(this);
			}
			if (group >= 0 && input.is(keys.getShowRecipe())) {
				if (!input.isSimulate())
					minecraft.setScreen(new BookmarkTreeScreen(screen, bookmarks, group));
				return Optional.of(this);
			}
			return Optional.empty();
		}
		int button = input.getKey().getValue();
		if (draggedGroup >= 0) return Optional.of(this);
		if (startRow >= 0 && !input.isSimulate()) {
			int first = Math.min(startRow, endRow), last = Math.max(startRow, endRow);
			if (endRow != startRow) {
				if (movingGroup) bookmarks.moveGroup(startGroup, group(endRow));
				else {
					List<IBookmark> selected = new ArrayList<>();
					for (int i = first; i <= last; i++)
						for (var e : rowElements(i))
							e.getBookmark()
									.ifPresent(
											b -> {
												if (selected.stream()
														.noneMatch(value -> value == b))
													selected.add(b);
											});
					bookmarks.groupBookmarks(
							selected, dragButton == 1 ? 0 : startGroup == 0 ? -1 : startGroup);
				}
			} else clickGroup(startGroup, dragButton);
			startRow = -1;
			endRow = -1;
			return Optional.of(this);
		}
		if (button > 1) return Optional.empty();

		if (gutter(x, y)) {
			if (button == 0 && minecraft.hasControlDown()) return Optional.of(this);
			if (input.isSimulate()) {
				startRow = endRow = row(y);
				startGroup = group(startRow);
				dragButton = button;
				movingGroup = minecraft.hasControlDown() && button == 0;
			} else clickGroup(group(row(y)), button);
			return Optional.of(this);
		}
		if (header().contains(x, y)) {
            if (button == 0 && !minecraft.hasAltDown()) return Optional.of(this);
			if (!input.isSimulate()) clickGroup(0, button);
			return Optional.of(this);
		}
		var cell = cell(x, y);
		if (cell.isPresent() && button == 0 && minecraft.hasAltDown()) {
			if (!input.isSimulate()) {
				var value = cell.get();
				if (value.bookmark != null) {
					var state = bookmarks.state(value.bookmark);
					state.collapsed = !state.collapsed;
					bookmarks.changed();
				}
			}
			return Optional.of(this);
		}
		if (cell.isPresent()
				&& button == 0
				&& minecraft.hasControlDown()
				&& !minecraft.hasShiftDown()) return Optional.of(this);
		return Optional.empty();
	}

	@Override
	public Optional<IUserInputHandler> handleMouseDragged(
			double x, double y, InputConstants.Key key, double dx, double dy) {
		if (startRow < 0) return Optional.empty();
		endRow = row(y);
		return Optional.of(this);
	}

	@Override
	public void unfocus() {
		finishGroupDrag(false);
		startRow = -1;
		endRow = -1;
	}

	@Override
	public Optional<IUserInputHandler> handleMouseScrolled(
			double x, double y, double dx, double dy) {
		Minecraft minecraft = Minecraft.getInstance();
		dy = choiceScrollDelta(dx, dy, minecraft.hasControlDown());
		if (dy == 0) return Optional.empty();

		var cell = cell(x, y);
		if (minecraft.hasControlDown() && cell.isPresent()) {
			if (cell.isPresent() && cell.get().role == RecipeIngredientRole.INPUT) {
				var material = cell.get();
				if (material.hasCandidates() && material.bookmark != null)
					bookmarks.cycleChoice(material.bookmark, material.slot, dy > 0 ? -1 : 1);
				return Optional.of(this);
			}
			long step = 1;
			if (minecraft.hasAltDown())
				step =
						cell.flatMap(c -> c.getTypedIngredient().getItemStack())
								.map(s -> (long) s.getMaxStackSize())
								.orElse(64L);
			long delta = dy > 0 ? step : -step;
			if (cell.isPresent() && cell.get().bookmark != null)
				bookmarks.changeQuantity(cell.get().bookmark, delta);
			return Optional.of(this);
		}
		return Optional.empty();
	}

	static double choiceScrollDelta(double horizontal, double vertical, boolean control) {
		return vertical != 0 ? vertical : control ? horizontal : 0;
	}

	public void draw(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		var grid = grid();
		int rows = grid.height() / 18;
		for (int row = 0; row < rows; ) {
			int group = group(row), end = row;
			while (end + 1 < rows && group(end + 1) == group) end++;
			if (group != 0) {
				var state = bookmarks.page().groups.get(group);
				int color =
						group == draggedGroup
								? 0xffaaaaaa
								: state != null && state.linked ? 0x6645DA75 : 0xff666666;
				int x = grid.x() - 4,
						top = grid.y() + row * 18 + 4,
						bottom = grid.y() + (end + 1) * 18 - 4;
				graphics.fill(x, top, x + 3, top + 1, color);
				graphics.fill(x, top, x + 1, bottom, color);
				graphics.fill(x, bottom, x + 3, bottom + 1, color);
			}
			row = end + 1;
		}
		if (startRow >= 0) {
			endRow = row(mouseY);
			int top = grid.y() + Math.min(startRow, endRow) * 18,
					bottom = grid.y() + (Math.max(startRow, endRow) + 1) * 18;
			graphics.fill(
					grid.x() - 7,
					top,
					grid.x() - 1,
					bottom,
					dragButton == 1 ? 0x66ff5555 : 0x6645DA75);
		}
		for (var slot : contents.getAllSlots())
			slot.getOptionalElement()
					.ifPresent(
							element -> {
								if (element instanceof BookmarkCell<?> cell)
									drawCell(graphics, cell, slot.getRenderArea());
							});
		if (draggedGroup >= 0) drawDraggedGroup(graphics, mouseX, mouseY);
	}

	private void drawDraggedGroup(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		for (var cell : draggedCells)
			BookmarkDrag.renderCell(
					graphics, cell.ingredient(), mouseX - 8 + cell.x(), mouseY - 8 + cell.y());
	}

	public void drawRecipeBackground(GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
		var hovered = cell(mouseX, mouseY).orElse(null);
		if (hovered == null || !(hovered.bookmark instanceof RecipeBookmark<?, ?>)) return;
		var group = bookmarks.page().groups.get(hovered.group);
		if (group == null || !group.todo) return;
		for (var slot : contents.getAllSlots()) {
			if (slot.isBlocked()) continue;
			slot.getOptionalElement()
					.filter(element -> element instanceof BookmarkCell<?>)
					.ifPresent(
							element -> {
								var material = (BookmarkCell<?>) element;
								if (material.bookmark != hovered.bookmark) return;
								int color =
										material.role == RecipeIngredientRole.OUTPUT
												? material.remainder ? 0x55A033A0 : 0x5566CCFF
												: 0x5545DA75;
								var area = slot.getArea();
								graphics.fill(
										area.x(),
										area.y(),
										area.x() + area.width(),
										area.y() + area.height(),
										color);
							});
		}
	}

	private void drawCell(
			GuiGraphicsExtractor graphics, BookmarkCell<?> cell, ImmutableRect2i area) {
		var group = bookmarks.page().groups.get(cell.group);
		if (cell.crafts > 1 && cell.role == RecipeIngredientRole.OUTPUT)
			label(graphics, "x" + compact(cell.crafts), area.x(), area.y(), false);
		if (cell.amount > 0 && !cell.reusable) {
			long amount = cell.amount;
			if (group != null && group.linked && Minecraft.getInstance().hasShiftDown()) {
				var key =
						BookmarkRecipeData.key(
								cell.getTypedIngredient(),
								eakerzt.jiv.common.Internal.getJivRuntime().getIngredientManager());
				amount =
						bookmarks
								.calculation(cell.group)
								.map(
										r ->
												cell.role == RecipeIngredientRole.INPUT
														? r.inputs().getOrDefault(key, 0L)
														: r.outputs()
																.getOrDefault(
																		key,
																		r.remainders()
																				.getOrDefault(
																						key, 0L)))
								.orElse(amount);
			}
			label(graphics, compact(amount), area.x() + 16, area.y() + 10, true);
		}
		if (cell.reusable) {
			new eakerzt.jiv.library.render.RecipeSlotDecorations(true, false, 1)
					.draw(graphics, area.x(), area.y(), 16, 16);
		}
	}

	public static String compact(long number) {
		if (number < 10_000) return Long.toString(number);
		if (number < 1_000_000)
			return String.format(java.util.Locale.ROOT, "%.1fk", number / 1000.0);
		if (number < 1_000_000_000)
			return String.format(java.util.Locale.ROOT, "%.1fM", number / 1_000_000.0);
		return String.format(java.util.Locale.ROOT, "%.1fG", number / 1_000_000_000.0);
	}

	private static void label(
			GuiGraphicsExtractor graphics, String text, int x, int y, boolean right) {
		var font = Minecraft.getInstance().font;
		var pose = graphics.pose();
		pose.pushMatrix();
		try {
			pose.translate(x, y);
			pose.scale(0.65f, 0.65f);
			graphics.text(font, text, right ? -font.width(text) : 0, 0, 0xffffffff, true);
		} finally {
			pose.popMatrix();
		}
	}

	public void drawTooltip(GuiGraphicsExtractor graphics, int x, int y) {
		int group = hoveredGroup(x, y);
		if (group < 0) return;
		JivTooltip tooltip = new JivTooltip();
		tooltip.add(Component.translatable("jiv.bookmarks.group"));
		tooltip.add(Component.translatable("jiv.bookmarks.controls.group"));
		tooltip.add(Component.translatable("jiv.bookmarks.controls.group_drag"));
		tooltip.add(Component.translatable("jiv.bookmarks.controls.group_extra"));
        tooltip.addKeyUsageComponent("jiv.bookmarks.controls.tree", Internal.getKeyMappings().getShowRecipe());
		bookmarks
				.calculation(group)
				.ifPresent(
						result -> {
							if (result.cycle())
								tooltip.add(Component.translatable("jiv.bookmarks.cycle"));
							if (result.uncertain())
								tooltip.add(Component.translatable("jiv.bookmarks.uncertain"));
							if (result.overflow())
								tooltip.add(Component.translatable("jiv.bookmarks.overflow"));
						});
		tooltip.draw(graphics, x, y);
	}
}
