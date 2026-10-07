package eakerzt.jiv.gui.input;

import eakerzt.jiv.common.input.UserInput;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;

public interface IDragHandler {
	Optional<IDragHandler> handleDragStart(Screen screen, UserInput input);

	boolean handleDragComplete(Screen screen, UserInput input);

	default void handleDragCanceled() {

	}
}
