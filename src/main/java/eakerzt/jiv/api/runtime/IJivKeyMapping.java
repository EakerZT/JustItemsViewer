package eakerzt.jiv.api.runtime;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

/**
 * A key mapping used by JIV.
 * This can be used by mods that want to use the same keys that players bind for JIV.
 *
 * Get instances from {@link IJivKeyMappings}.
 *
 * @since 11.0.1
 */
@ApiStatus.NonExtendable
public interface IJivKeyMapping {
	/**
	 * Returns true if the key mapping matches the key,
	 * and the current key modifiers match any pressed key modifiers.
	 *
	 * This works for a mouse click or for a keyboard key, depending on what is bound.
	 *
	 * @since 11.0.1
	 */
	boolean isActiveAndMatches(InputConstants.Key key);

	/**
	 * @return true if there is no key bound to this mapping.
	 *
	 * @since 11.0.1
	 */
	boolean isUnbound();

	/**
	 * @return the name of the key that is bound.
	 *
	 * @since 11.0.1
	 */
	Component getTranslatedKeyMessage();
}
