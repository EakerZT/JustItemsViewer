package eakerzt.jiv.api.runtime;

import org.jetbrains.annotations.ApiStatus;

/**
 * Gives access to key mappings used by JIV.
 * This can be used by mods that want to use the same keys that players bind for JIV.
 *
 * Get the instance from {@link IJivRuntime}.
 *
 * @since 11.0.1
 */
@ApiStatus.NonExtendable
public interface IJivKeyMappings {
	/**
	 * @return the key mapping to show recipes.
	 * The default bindings are 'Left Click' and 'R'.
	 *
	 * @since 11.0.1
	 */
	IJivKeyMapping getShowRecipe();

	/**
	 * @return the key mapping to show recipes.
	 * The default bindings are 'Right Click' and 'U'.
	 *
	 * @since 11.0.1
	 */
	IJivKeyMapping getShowUses();

	/**
	 * @return the key mapping to bookmark an ingredient.
	 * The default binding is 'A'.
	 *
	 * @since 29.36.0
	 */
	IJivKeyMapping getBookmark();
}
