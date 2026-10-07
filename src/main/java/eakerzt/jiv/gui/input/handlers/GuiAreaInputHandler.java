package eakerzt.jiv.gui.input.handlers;

import eakerzt.jiv.api.gui.handlers.IGuiProperties;
import eakerzt.jiv.api.recipe.IFocusFactory;
import eakerzt.jiv.api.runtime.IRecipesGui;
import eakerzt.jiv.api.runtime.IScreenHelper;
import eakerzt.jiv.common.input.IInternalKeyMappings;
import eakerzt.jiv.common.input.IUserInputHandler;
import eakerzt.jiv.common.input.UserInput;
import eakerzt.jiv.common.input.handlers.SameElementInputHandler;
import eakerzt.jiv.common.util.ImmutableRect2i;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.Optional;

public class GuiAreaInputHandler implements IUserInputHandler {
	private final IFocusFactory focusFactory;
	private final IScreenHelper screenHelper;
	private final IRecipesGui recipesGui;

	public GuiAreaInputHandler(IScreenHelper screenHelper, IRecipesGui recipesGui, IFocusFactory focusFactory) {
		this.focusFactory = focusFactory;
		this.screenHelper = screenHelper;
		this.recipesGui = recipesGui;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, IGuiProperties guiProperties, UserInput input, IInternalKeyMappings keyBindings) {
		if (input.is(keyBindings.getLeftClick())) {
			if (screen instanceof AbstractContainerScreen<?> guiContainer) {
				if (!guiContainer.getMenu().getCarried().isEmpty()) {
					return Optional.empty();
				}

				final int guiLeft = guiProperties.guiLeft();
				final int guiTop = guiProperties.guiTop();
				final double guiMouseX = input.getMouseX() - guiLeft;
				final double guiMouseY = input.getMouseY() - guiTop;
				return this.screenHelper.getGuiClickableArea(guiContainer, guiMouseX, guiMouseY)
					.findFirst()
					.map(clickableArea -> {
						if (!input.isSimulate()) {
							clickableArea.onClick(focusFactory, recipesGui);
						}

						ImmutableRect2i screenArea = new ImmutableRect2i(clickableArea.getArea())
							.addOffset(guiLeft, guiTop);
						return new SameElementInputHandler(this, screenArea::contains);
					});
			}
		}

		return Optional.empty();
	}

}
