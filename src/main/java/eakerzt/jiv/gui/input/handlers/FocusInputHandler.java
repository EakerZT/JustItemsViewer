package eakerzt.jiv.gui.input.handlers;

import eakerzt.jiv.api.gui.handlers.IGuiProperties;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.api.runtime.IRecipesGui;
import eakerzt.jiv.common.chat.JivChatItemLinks;
import eakerzt.jiv.common.input.IInternalKeyMappings;
import eakerzt.jiv.common.input.handlers.SameElementInputHandler;
import eakerzt.jiv.gui.input.CombinedRecipeFocusSource;
import eakerzt.jiv.common.input.IUserInputHandler;
import eakerzt.jiv.gui.input.PinnedTooltipManager;
import eakerzt.jiv.common.input.UserInput;
import eakerzt.jiv.gui.util.FocusUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.Optional;

public class FocusInputHandler implements IUserInputHandler {
	private final CombinedRecipeFocusSource focusSource;
	private final IRecipesGui recipesGui;
	private final FocusUtil focusUtil;
	private final IIngredientManager ingredientManager;

	public FocusInputHandler(
		CombinedRecipeFocusSource focusSource,
		IRecipesGui recipesGui,
		FocusUtil focusUtil,
		IIngredientManager ingredientManager
	) {
		this.focusSource = focusSource;
		this.recipesGui = recipesGui;
		this.focusUtil = focusUtil;
		this.ingredientManager = ingredientManager;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, IGuiProperties guiProperties, UserInput input, IInternalKeyMappings keyBindings) {
		if (PinnedTooltipManager.matchesInput(input.getKey(), keyBindings.getShowRecipe(), keyBindings.getPauseRecipeCycling())) {
			return handleShow(input, List.of(RecipeIngredientRole.OUTPUT), keyBindings);
		}

		if (input.is(keyBindings.getShareToChat())) {
			return handleShareToChat(input, keyBindings);
		}

		if (PinnedTooltipManager.matchesInput(input.getKey(), keyBindings.getShowUses(), keyBindings.getPauseRecipeCycling())) {
			return handleShow(input, List.of(RecipeIngredientRole.INPUT, RecipeIngredientRole.CRAFTING_STATION), keyBindings);
		}

		return Optional.empty();
	}

	private Optional<IUserInputHandler> handleShow(UserInput input, List<RecipeIngredientRole> roles, IInternalKeyMappings keyBindings) {
		return focusSource.getIngredientUnderMouse(input, keyBindings)
			.filter(clicked -> clicked.getElement().isVisible())
			.findFirst()
			.map(clicked -> {
				if (!input.isSimulate()) {
					clicked.show(recipesGui, focusUtil, roles);
				}
				return new SameElementInputHandler(this, clicked::isMouseOver);
			});
	}

	private Optional<IUserInputHandler> handleShareToChat(UserInput input, IInternalKeyMappings keyBindings) {
		return focusSource.getIngredientUnderMouse(input, keyBindings)
			.filter(clicked -> clicked.getElement().isVisible())
			.findFirst()
			.map(clicked -> {
				if (!input.isSimulate()) {
					ITypedIngredient<?> typedIngredient = clicked.getTypedIngredient();
					String chatText = JivChatItemLinks.createLinkMarker(typedIngredient, ingredientManager);
					Minecraft minecraft = Minecraft.getInstance();
					minecraft.schedule(() -> {
						ChatScreen chatScreen = new ChatScreen(chatText, false);
						minecraft.setScreenAndShow(chatScreen);
					});
				}
				return new SameElementInputHandler(this, clicked::isMouseOver);
			});
	}
}
