package eakerzt.jiv.api.helpers;

import eakerzt.jiv.api.ingredients.ITypedIngredient;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;
import java.util.Set;

/**
 * Helper class for getting mod names from their modIds.
 * Get an instance from {@link IJivHelpers#getModIdHelper()}
 */
@ApiStatus.NonExtendable
public interface IModIdHelper {
	/**
	 * Get the mod name for its modId
	 */
	String getModNameForModId(String modId);

	/**
	 * Returns true if JIV is configured to display mod names.
	 */
	boolean isDisplayingModNameEnabled();

	/**
	 * Returns the mod name with color formatting, as specified in JIV's config. (default is blue italic)
	 *
	 * @since 29.10.0
	 */
	Component getFormattedModNameComponentForModId(String modId);

	/**
	 * Returns alternative mod names, used for searching for a mod by a different name.
	 * @since 17.1.0
	 */
	Set<String> getModAliases(String modId);

	/**
	 * Gets the mod name for the tooltip with color formatting.
	 *
	 * If {@link #isDisplayingModNameEnabled()} is false,
	 * or another mod already adds the mod name, this will return {@link Optional#empty}.
	 *
	 * @since 19.5.1
	 */
	<T> Optional<Component> getModNameForTooltip(ITypedIngredient<T> typedIngredient);
}
