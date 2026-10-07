package eakerzt.jiv.gui.input.handlers;

import eakerzt.jiv.api.gui.handlers.IGuiProperties;
import eakerzt.jiv.common.input.handlers.SameElementInputHandler;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.runtime.IEditModeConfig;
import eakerzt.jiv.common.config.IClientToggleState;
import eakerzt.jiv.common.input.IInternalKeyMappings;
import eakerzt.jiv.gui.input.CombinedRecipeFocusSource;
import eakerzt.jiv.gui.input.IClickableIngredientInternal;
import eakerzt.jiv.common.input.IUserInputHandler;
import eakerzt.jiv.common.input.UserInput;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;
import java.util.Set;

public class EditInputHandler implements IUserInputHandler {
	private final CombinedRecipeFocusSource focusSource;
	private final IClientToggleState toggleState;
	private final IEditModeConfig editModeConfig;

	public EditInputHandler(CombinedRecipeFocusSource focusSource, IClientToggleState toggleState, IEditModeConfig editModeConfig) {
		this.focusSource = focusSource;
		this.toggleState = toggleState;
		this.editModeConfig = editModeConfig;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, IGuiProperties guiProperties, UserInput input, IInternalKeyMappings keyBindings) {
		if (!toggleState.isEditModeEnabled()) {
			return Optional.empty();
		}

		if (input.is(keyBindings.getToggleHideIngredient())) {
			return handle(input, keyBindings, IEditModeConfig.HideMode.SINGLE);
		}

		if (input.is(keyBindings.getToggleWildcardHideIngredient())) {
			return handle(input, keyBindings, IEditModeConfig.HideMode.WILDCARD);
		}

		return Optional.empty();
	}

	private Optional<IUserInputHandler> handle(UserInput input, IInternalKeyMappings keyBindings, IEditModeConfig.HideMode hideMode) {
		return focusSource.getIngredientUnderMouse(input, keyBindings)
			.findFirst()
			.map(clicked -> {
				if (!input.isSimulate()) {
					execute(clicked, hideMode);
				}
				return new SameElementInputHandler(this, clicked::isMouseOver);
			});
	}

	private <V> void execute(IClickableIngredientInternal<V> clicked, IEditModeConfig.HideMode hideMode) {
		ITypedIngredient<?> typedIngredient = clicked.getTypedIngredient();
		Set<IEditModeConfig.HideMode> hideModes = editModeConfig.getIngredientHiddenUsingConfigFile(typedIngredient);
		if (hideModes.contains(hideMode)) {
			editModeConfig.showIngredientUsingConfigFile(typedIngredient, hideMode);
		} else {
			editModeConfig.hideIngredientUsingConfigFile(typedIngredient, hideMode);
		}
	}
}
