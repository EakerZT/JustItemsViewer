package eakerzt.jiv.gui.input.handlers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;

class BookmarkInputHandlerTest {
	@Test
	void bookmarkModifiersSelectThreeDistinctActions() {
		assertEquals(BookmarkInputHandler.Action.INGREDIENT, BookmarkInputHandler.action(0));
		assertEquals(
				BookmarkInputHandler.Action.SINGLE_OUTPUT,
				BookmarkInputHandler.action(GLFW.GLFW_MOD_CONTROL));
		assertEquals(
				BookmarkInputHandler.Action.ALL_OUTPUTS,
				BookmarkInputHandler.action(GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_SHIFT));
	}

	@Test
	void oldShiftShortcutAndUnrelatedModifiersDoNotAddRecipes() {
		assertEquals(
				BookmarkInputHandler.Action.NONE, BookmarkInputHandler.action(GLFW.GLFW_MOD_SHIFT));
		assertEquals(
				BookmarkInputHandler.Action.NONE, BookmarkInputHandler.action(GLFW.GLFW_MOD_ALT));
		assertEquals(
				BookmarkInputHandler.Action.NONE,
				BookmarkInputHandler.action(GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_ALT));
		assertEquals(
				BookmarkInputHandler.Action.NONE, BookmarkInputHandler.action(GLFW.GLFW_MOD_SUPER));
	}
}
