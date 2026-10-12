package eakerzt.jiv.gui.bookmarks;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BookmarkGroupTest {
	@Test void plainClickReopensAltCollapsedGroupAndPreservesLayout() {
		for (boolean todo : new boolean[]{false, true}) {
			var group = new BookmarkGroup();
			group.todo = todo;
			group.linked = true;
			group.leftClick(true);
			assertTrue(group.collapsed);
			group.leftClick(false);
			assertFalse(group.collapsed);
			assertEquals(todo, group.todo);
			assertTrue(group.linked);
			group.leftClick(false);
			assertEquals(!todo, group.todo);
		}
	}

	@Test void altClickStillTogglesCollapseWithoutChangingLayout() {
		var group = new BookmarkGroup();
		group.todo = true;
		group.leftClick(true);
		assertTrue(group.collapsed);
		group.leftClick(true);
		assertFalse(group.collapsed);
		assertTrue(group.todo);
	}
}
