package eakerzt.jiv.api.gui.inputs;

import com.mojang.blaze3d.platform.InputConstants;
import eakerzt.jiv.api.runtime.IJivKeyMapping;
import eakerzt.jiv.api.runtime.IJivKeyMappings;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.InputWithModifiers;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a click or key press.
 *
 * @since 19.6.0
 */
@ApiStatus.NonExtendable
public interface IJivUserInput {
	/**
	 * Vanilla information about a click or key press.
	 *
	 * @since 19.6.0
	 */
	InputConstants.Key getKey();

	/**
	 * Modifiers passed into methods like {@link GuiEventListener#mouseClicked}
	 *
	 * @since 19.6.0
	 */
	@InputWithModifiers.Modifiers
	int getModifiers();

	/**
	 * Get the backing {@link InputWithModifiers} that this {@link IJivUserInput} was created from.
	 *
	 * @since 27.1.0
	 */
	InputWithModifiers getInputWithModifiers();

	/**
	 * True on mouse down, used to check if a click could be handled.
	 *
	 * False on mouse up and key down: when the input should execute an action.
	 *
	 * Key up is ignored because JIV handles key down immediately.
	 *
	 * @since 19.6.0
	 */
	boolean isSimulate();

	/**
	 * Check if the input matches a given vanilla {@link KeyMapping}.
	 *
	 * @return true if this input and modifiers match the given key mapping.
	 *
	 * @since 19.6.0
	 */
	boolean is(KeyMapping keyMapping);

	/**
	 * Check if the input matches a given {@link IJivKeyMapping}.
	 * See all the mappings in {@link IJivKeyMappings}
	 *
	 * @return true if this input and modifiers match the given key mapping.
	 *
	 * @since 19.6.0
	 */
	boolean is(IJivKeyMapping keyMapping);
}
