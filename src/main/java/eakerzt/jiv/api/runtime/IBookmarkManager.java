package eakerzt.jiv.api.runtime;

import eakerzt.jiv.api.ingredients.ITypedIngredient;
import org.jetbrains.annotations.ApiStatus;

/**
 * Gives access to JIV's ingredient bookmarks.
 *
 * Get the instance from {@link IJivRuntime#getBookmarkManager()}.
 *
 * @since 29.36.0
 */
@ApiStatus.NonExtendable
public interface IBookmarkManager {
	/**
	 * Returns whether the ingredient is bookmarked.
	 *
	 * @since 29.36.0
	 */
	boolean contains(ITypedIngredient<?> ingredient);

	/**
	 * Adds the ingredient to JIV's bookmarks.
	 *
	 * @return true if the ingredient was added, or false if it was already bookmarked.
	 * @since 29.36.0
	 */
	boolean add(ITypedIngredient<?> ingredient);

	/**
	 * Removes the ingredient from JIV's bookmarks.
	 *
	 * @return true if the ingredient was removed, or false if it was not bookmarked.
	 * @since 29.36.0
	 */
	boolean remove(ITypedIngredient<?> ingredient);
}
