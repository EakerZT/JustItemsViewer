package eakerzt.jiv.gui.overlay.bookmarks;

import eakerzt.jiv.common.util.ImmutableRect2i;
import eakerzt.jiv.gui.bookmarks.IBookmark;
import eakerzt.jiv.gui.overlay.elements.IElement;
import eakerzt.jiv.gui.overlay.ingredients.IngredientListSlot;

import java.util.ArrayList;
import java.util.List;

/** A drop area and the bookmark's destination index after it is removed from its old position. */
public record BookmarkDragTarget(ImmutableRect2i area, int index, int group) {
	public BookmarkDragTarget(ImmutableRect2i area, int index) {
		this(area, index, -1);
	}

	static int ownerIndex(List<IElement<?>> elements, IBookmark bookmark) {
		for (int i = 0; i < elements.size(); i++) {
			if (elements.get(i).getBookmark().orElse(null) == bookmark) return i;
		}
		return -1;
	}

	static List<BookmarkDragTarget> createSlotTargets(
			List<IngredientListSlot> slots, List<IElement<?>> elements, IBookmark draggedBookmark) {
		List<BookmarkDragTarget> targets = new ArrayList<>();
		int draggedIndex = ownerIndex(elements, draggedBookmark);
		int nextIndex = -1;
		// Walk backwards so gaps can use the next occupied slot as their insertion point.
		for (IngredientListSlot slot : slots.reversed()) {
			if (slot.isBlocked()) continue;
			IElement<?> element = slot.getOptionalElement().orElse(null);
			if (element != null) {
				int index =
						element.getBookmark().map(owner -> ownerIndex(elements, owner)).orElse(-1);
				if (index < 0 || slot.isBlocked()) continue;
				targets.add(new BookmarkDragTarget(slot.getArea(), index));
				nextIndex = index;
				if (draggedIndex < index) {
					nextIndex--;
				}
			} else if (nextIndex >= 0) {
				targets.add(new BookmarkDragTarget(slot.getArea(), nextIndex));
			}
		}
		return targets.reversed();
	}
}
