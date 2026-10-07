package eakerzt.jiv.library.gui;

import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.runtime.IBookmarkOverlay;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class BookmarkOverlayDummy implements IBookmarkOverlay {
	public static final IBookmarkOverlay INSTANCE = new BookmarkOverlayDummy();

	private BookmarkOverlayDummy() {

	}

	@Override
	public Optional<ITypedIngredient<?>> getIngredientUnderMouse() {
		return Optional.empty();
	}

	@Nullable
	@Override
	public <T> T getIngredientUnderMouse(IIngredientType<T> ingredientType) {
		return null;
	}
}
