package eakerzt.jiv.gui.bookmarks;

import eakerzt.jiv.gui.input.IPaged;

/** Top arrows switch independent bookmark workspaces; the label also shows their material pages. */
public final class BookmarkWorkspaceNavigation implements IPaged {
	private final BookmarkList bookmarks;
	private final IPaged materialPages;
	private final Runnable resetPage;

	public BookmarkWorkspaceNavigation(BookmarkList bookmarks, IPaged materialPages, Runnable resetPage) {
		this.bookmarks = bookmarks;
		this.materialPages = materialPages;
		this.resetPage = resetPage;
	}

	@Override public boolean nextPage() {
		if (!hasNext()) return false;
		if (bookmarks.isDragActive()) {
			if (!bookmarks.browseDragNamespace(1)) return false;
			resetPage.run();
			return true;
		}
		bookmarks.changeNamespace(1);
		resetPage.run();
		return true;
	}

	@Override public boolean previousPage() {
		if (!hasPrevious()) return false;
		if (bookmarks.isDragActive()) {
			if (!bookmarks.browseDragNamespace(-1)) return false;
			resetPage.run();
			return true;
		}
		bookmarks.changeNamespace(-1);
		resetPage.run();
		return true;
	}

	@Override public boolean hasNext() {
		if (bookmarks.isDragActive()) return getPageNumber() < getPageCount() - 1;
		return getPageCount() > 1 || !bookmarks.page().bookmarks.isEmpty();
	}
	@Override public boolean hasPrevious() { return bookmarks.isDragActive() ? getPageNumber() > 0 : getPageCount() > 1; }
	@Override public int getPageCount() { return bookmarks.getPages().size(); }
	@Override public int getPageNumber() { return bookmarks.getNamespace(); }

	public String label() {
		String groups = String.format("%d/%d", getPageNumber() + 1, getPageCount());
		return materialPages.getPageCount() <= 1 ? groups : groups + String.format(" %d/%d", materialPages.getPageNumber() + 1, materialPages.getPageCount());
	}
}
