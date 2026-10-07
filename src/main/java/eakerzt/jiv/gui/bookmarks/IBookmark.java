package eakerzt.jiv.gui.bookmarks;

import eakerzt.jiv.gui.overlay.elements.IElement;

public interface IBookmark {
	BookmarkType getType();
	IElement<?> getElement();
	boolean isVisible();
	void setVisible(boolean visible);
}
