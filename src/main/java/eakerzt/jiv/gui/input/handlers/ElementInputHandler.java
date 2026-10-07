package eakerzt.jiv.gui.input.handlers;

import eakerzt.jiv.api.gui.handlers.IGuiProperties;
import eakerzt.jiv.common.input.IInternalKeyMappings;
import eakerzt.jiv.gui.input.CombinedRecipeFocusSource;
import eakerzt.jiv.gui.input.IClickableIngredientInternal;
import eakerzt.jiv.common.input.IUserInputHandler;
import eakerzt.jiv.common.input.UserInput;
import eakerzt.jiv.common.input.handlers.SameElementInputHandler;
import eakerzt.jiv.gui.overlay.elements.IElement;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.Optional;

public class ElementInputHandler implements IUserInputHandler {
	private final CombinedRecipeFocusSource focusSource;

	public ElementInputHandler(CombinedRecipeFocusSource focusSource) {
		this.focusSource = focusSource;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, IGuiProperties guiProperties, UserInput input, IInternalKeyMappings keyBindings) {
		List<IClickableIngredientInternal<?>> ingredientUnderMouse = focusSource.getIngredientUnderMouse(input, keyBindings)
			.toList();

		for (IClickableIngredientInternal<?> clicked : ingredientUnderMouse) {
			IElement<?> element = clicked.getElement();
			if (element.handleClick(input, keyBindings)) {
				IUserInputHandler result = new SameElementInputHandler(this, clicked::isMouseOver);
				return Optional.of(result);
			}
		}
		return Optional.empty();
	}
}
