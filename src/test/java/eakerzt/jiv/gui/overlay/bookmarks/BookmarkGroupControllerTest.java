package eakerzt.jiv.gui.overlay.bookmarks;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BookmarkGroupControllerTest {
	private record Owner(String id) implements eakerzt.jiv.gui.bookmarks.IBookmark {
		public eakerzt.jiv.gui.bookmarks.BookmarkType getType() { return eakerzt.jiv.gui.bookmarks.BookmarkType.RECIPE; }
		public eakerzt.jiv.gui.overlay.elements.IElement<?> getElement() { throw new UnsupportedOperationException(); }
		public boolean isVisible() { return true; }
		public void setVisible(boolean visible) {}
	}

	private static eakerzt.jiv.gui.overlay.ingredients.IngredientListSlot rowSlot(int y, Owner owner, int group) {
		var slot = new eakerzt.jiv.gui.overlay.ingredients.IngredientListSlot(10, y, 18, 18, 1);
		slot.setElement(new eakerzt.jiv.gui.bookmarks.BookmarkCell<>(null, owner, group, 0,
				eakerzt.jiv.api.recipe.RecipeIngredientRole.OUTPUT, 1, 1, false, 1, true, false));
		return slot;
	}

	@Test void groupDropUsesTargetRowEdgeAndAcceptsTrailingEmptySpace() {
		var moving = new Owner("moving");
		var target = new Owner("target");
		var sourceState = new eakerzt.jiv.gui.bookmarks.BookmarkState();
		sourceState.group = 1;
		var targetState = new eakerzt.jiv.gui.bookmarks.BookmarkState();
		targetState.group = 2;
		java.util.function.Function<eakerzt.jiv.gui.bookmarks.IBookmark, eakerzt.jiv.gui.bookmarks.BookmarkState>
				states = b -> b == moving ? sourceState : targetState;
		var source = rowSlot(0, moving, 1);
		var destination = rowSlot(18, target, 2);
		var slots = java.util.List.of(source, destination);
		assertNull(BookmarkGroupController.groupDropTarget(slots, 4, 1, states));
		var before = BookmarkGroupController.groupDropTarget(slots, 20, 1, states);
		assertSame(target, before.bookmark());
		assertFalse(before.after());
		var after = BookmarkGroupController.groupDropTarget(slots, 32, 1, states);
		assertSame(target, after.bookmark());
		assertTrue(after.after());
		// Changing the source's relative position must not reverse this insertion edge.
		assertEquals(after, BookmarkGroupController.groupDropTarget(
				java.util.List.of(destination, rowSlot(36, moving, 1)), 32, 1, states));
		var trailing = BookmarkGroupController.groupDropTarget(slots, 50, 1, states);
		assertSame(target, trailing.bookmark());
		assertTrue(trailing.after());
	}

	@Test void recipeRowIndentationAndDragPlaceholdersRetainTheirGroup() {
		var owner = new eakerzt.jiv.gui.bookmarks.IBookmark() {
			public eakerzt.jiv.gui.bookmarks.BookmarkType getType() { return eakerzt.jiv.gui.bookmarks.BookmarkType.RECIPE; }
			public eakerzt.jiv.gui.overlay.elements.IElement<?> getElement() { throw new UnsupportedOperationException(); }
			public boolean isVisible() { return true; }
			public void setVisible(boolean visible) {}
		};
		var state = new eakerzt.jiv.gui.bookmarks.BookmarkState();
		state.group = 3;
		var cell = new eakerzt.jiv.gui.bookmarks.BookmarkCell<>(null, owner, 3, 0,
				eakerzt.jiv.api.recipe.RecipeIngredientRole.INPUT, 1, 1, false, 1, false, false);
		assertEquals(3, BookmarkGroupController.rowGroup(java.util.List.of(
				eakerzt.jiv.gui.bookmarks.BookmarkCell.gap(0), cell), b -> state));
		assertEquals(3, BookmarkGroupController.rowGroup(java.util.List.of(
				eakerzt.jiv.gui.bookmarks.BookmarkCell.gap(3),
				new eakerzt.jiv.gui.bookmarks.BookmarkDragPlaceholder<>(cell)), b -> state));
		assertEquals(3, BookmarkGroupController.rowGroup(java.util.List.of(
				eakerzt.jiv.gui.bookmarks.BookmarkCell.gap(3)), b -> state));
		assertEquals(0, BookmarkGroupController.rowGroup(java.util.List.of(), b -> state));
	}

	@Test void ctrlScrollAcceptsHorizontalWheelEvents() {
		assertEquals(-1, BookmarkGroupController.choiceScrollDelta(-1, 0, true));
		assertEquals(1, BookmarkGroupController.choiceScrollDelta(1, 0, true));
		assertEquals(2, BookmarkGroupController.choiceScrollDelta(1, 2, true));
		assertEquals(0, BookmarkGroupController.choiceScrollDelta(1, 0, false));
	}
}
