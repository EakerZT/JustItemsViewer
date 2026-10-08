package eakerzt.jiv.gui.bookmarks;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A namespace, distinct from the visible grid's pagination. */
public final class BookmarkPage {
	public final List<com.google.gson.JsonElement> unresolved = new ArrayList<>();
	public final List<IBookmark> bookmarks = new ArrayList<>();
	public final Map<IBookmark, BookmarkState> states = new IdentityHashMap<>();
	public final Map<Integer, BookmarkGroup> groups = new LinkedHashMap<>();

	public BookmarkPage() {
		groups.put(0, new BookmarkGroup());
	}

	public BookmarkState state(IBookmark bookmark) {
		return states.computeIfAbsent(bookmark, ignored -> new BookmarkState());
	}

	public int nextGroupId() {
		return groups.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
	}
}
