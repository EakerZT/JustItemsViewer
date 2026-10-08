package eakerzt.jiv.gui.bookmarks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;

/** Quantities are independent of normalized ingredients and bookmark equality. */
public final class BookmarkState {
	public int group;
	public long multiplier;
	public boolean collapsed;
	public final Set<Integer> removedSlots = new HashSet<>();
	public final List<Integer> inputOrder = new ArrayList<>();
	public final Map<Integer, Integer> choices = new HashMap<>();

	public BookmarkState copy() {
		BookmarkState copy = new BookmarkState();
		copy.group = group;
		copy.multiplier = multiplier;
		copy.collapsed = collapsed;
		copy.choices.putAll(choices);
		copy.inputOrder.addAll(inputOrder);
		copy.removedSlots.addAll(removedSlots);
		return copy;
	}
}
