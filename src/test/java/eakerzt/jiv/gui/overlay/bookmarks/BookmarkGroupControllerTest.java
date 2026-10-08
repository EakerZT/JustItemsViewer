package eakerzt.jiv.gui.overlay.bookmarks;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BookmarkGroupControllerTest {
	@Test void ctrlScrollAcceptsHorizontalWheelEvents() {
		assertEquals(-1, BookmarkGroupController.choiceScrollDelta(-1, 0, true));
		assertEquals(1, BookmarkGroupController.choiceScrollDelta(1, 0, true));
		assertEquals(2, BookmarkGroupController.choiceScrollDelta(1, 2, true));
		assertEquals(0, BookmarkGroupController.choiceScrollDelta(1, 0, false));
	}
}
