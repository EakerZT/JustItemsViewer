package eakerzt.jiv.gui.bookmarks;

/** NEI's two group layouts and independent chain/collapse switches. */
public final class BookmarkGroup {
	public boolean todo;
	public boolean linked;
	public boolean collapsed;

	public void leftClick(boolean alt) {
		if (alt) collapsed = !collapsed;
		else if (collapsed) collapsed = false;
		else todo = !todo;
	}

	public BookmarkGroup copy() {
		BookmarkGroup copy = new BookmarkGroup();
		copy.todo = todo;
		copy.linked = linked;
		copy.collapsed = collapsed;
		return copy;
	}
}
