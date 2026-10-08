package eakerzt.jiv.gui.bookmarks;

import static org.junit.jupiter.api.Assertions.*;

import eakerzt.jiv.gui.overlay.elements.IElement;
import eakerzt.jiv.gui.overlay.ingredients.IngredientListSlot;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Optional;

class BookmarkDragPlaceholderTest {
	@Test
	void placeholderKeepsSlotButCannotBeFocusedOrDragged() {
		IElement<Object> source =
				(IElement<Object>)
						Proxy.newProxyInstance(
								IElement.class.getClassLoader(),
								new Class<?>[] {IElement.class},
								(proxy, method, args) -> {
									if (method.getName().equals("getBookmark"))
										return Optional.empty();
									throw new AssertionError(
											"Placeholder must not request the hidden ingredient: "
													+ method.getName());
								});
		var placeholder = new BookmarkDragPlaceholder<>(source);
		var slot = new IngredientListSlot(10, 20, 18, 18, 1);
		slot.setElement(placeholder);
		assertSame(placeholder, slot.getOptionalElement().orElseThrow());
		assertTrue(slot.getArea().contains(11, 21));
		assertFalse(slot.isMouseOver(11, 21));
		assertTrue(slot.getClickableIngredient().isEmpty());
		assertTrue(slot.getDraggableIngredient().isEmpty());
		assertTrue(placeholder.isVisible());
		assertFalse(placeholder.isEmptySlot());
		assertTrue(placeholder.isDragPlaceholder());
		assertSame(source, placeholder.source());
	}
}
