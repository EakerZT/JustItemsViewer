package eakerzt.jiv.gui.input;

import net.minecraft.client.input.CharacterEvent;

public interface ICharTypedHandler {
	boolean hasKeyboardFocus();

	boolean onCharTyped(CharacterEvent event);
}
