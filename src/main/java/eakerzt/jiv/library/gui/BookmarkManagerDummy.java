package eakerzt.jiv.library.gui;

import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.runtime.IBookmarkManager;

public class BookmarkManagerDummy implements IBookmarkManager {
	public static final IBookmarkManager INSTANCE = new BookmarkManagerDummy();

	private BookmarkManagerDummy() {

	}

	@Override
	public boolean contains(ITypedIngredient<?> ingredient) {
		return false;
	}

	@Override
	public boolean add(ITypedIngredient<?> ingredient) {
		return false;
	}

	@Override
	public boolean remove(ITypedIngredient<?> ingredient) {
		return false;
	}
}
