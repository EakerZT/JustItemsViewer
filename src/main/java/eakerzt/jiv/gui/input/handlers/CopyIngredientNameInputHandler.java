package eakerzt.jiv.gui.input.handlers;

import eakerzt.jiv.api.gui.handlers.IGuiProperties;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.input.IInternalKeyMappings;
import eakerzt.jiv.common.input.IUserInputHandler;
import eakerzt.jiv.common.input.UserInput;
import eakerzt.jiv.gui.input.CombinedRecipeFocusSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;

public final class CopyIngredientNameInputHandler implements IUserInputHandler {
	private final CombinedRecipeFocusSource focusSource;
	private final IIngredientManager ingredients;

	public CopyIngredientNameInputHandler(CombinedRecipeFocusSource focusSource, IIngredientManager ingredients) {
		this.focusSource = focusSource;
		this.ingredients = ingredients;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, IGuiProperties properties, UserInput input, IInternalKeyMappings keys) {
		if (!keys.getCopyIngredientName().isActiveAndMatchesAllowingExtraModifiers(input.getKey())) return Optional.empty();
		return focusSource.getIngredientUnderMouse(input, keys)
				.filter(clicked -> !clicked.getElement().isEmptySlot() && clicked.getElement().isVisible())
				.findFirst()
				.map(clicked -> {
					if (!input.isSimulate()) Minecraft.getInstance().keyboardHandler.setClipboard(displayName(clicked.getTypedIngredient(), ingredients));
					return this;
				});
	}

	static <T> String displayName(ITypedIngredient<T> ingredient, IIngredientManager ingredients) {
		return ingredients.getIngredientHelper(ingredient.getType()).getDisplayName(ingredient.getIngredient());
	}
}
