package eakerzt.jiv.gui.input;

import com.mojang.blaze3d.platform.InputConstants;
import eakerzt.jiv.api.gui.handlers.IGuiProperties;
import eakerzt.jiv.api.runtime.IScreenHelper;
import eakerzt.jiv.common.input.IInternalKeyMappings;
import eakerzt.jiv.common.input.UserInput;
import eakerzt.jiv.common.util.ReflectionUtil;
import eakerzt.jiv.gui.input.handlers.ChatLinkInputHandler;
import eakerzt.jiv.gui.input.handlers.DragRouter;
import eakerzt.jiv.common.input.handlers.UserInputRouter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.InputQuirks;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class ClientInputHandler {
	private final List<ICharTypedHandler> charTypedHandlers;
	private final ChatLinkInputHandler chatLinkInputHandler;
	private final UserInputRouter inputRouter;
	private final DragRouter dragRouter;
	private final IInternalKeyMappings keybindings;
	private final IScreenHelper screenHelper;
	private final ReflectionUtil reflectionUtil = new ReflectionUtil();
	private boolean physicalCtrlLeftClick;

	public ClientInputHandler(
		List<ICharTypedHandler> charTypedHandlers,
		ChatLinkInputHandler chatLinkInputHandler,
		UserInputRouter inputRouter,
		DragRouter dragRouter,
		IInternalKeyMappings keybindings,
		IScreenHelper screenHelper
	) {
		this.charTypedHandlers = charTypedHandlers;
		this.chatLinkInputHandler = chatLinkInputHandler;
		this.inputRouter = inputRouter;
		this.dragRouter = dragRouter;
		this.keybindings = keybindings;
		this.screenHelper = screenHelper;
	}

	public void onInitGui() {
		this.physicalCtrlLeftClick = false;
		this.chatLinkInputHandler.handleGuiChange();
		this.inputRouter.handleGuiChange();
		this.dragRouter.handleGuiChange();
	}

	/**
	 * When we have keyboard focus, use Pre
	 */
	public boolean onKeyboardKeyPressedPre(Screen screen, UserInput input) {
		if (this.chatLinkInputHandler.handleUserInput(screen, input, keybindings)) {
			return true;
		}

		// Focus-search explicitly transfers focus, including from the creative inventory's always-focused search box.
		if (input.is(keybindings.getFocusSearch()) || keybindings.getCopyIngredientName().isActiveAndMatchesAllowingExtraModifiers(input.getKey()) || !isContainerTextFieldFocused(screen)) {
			IGuiProperties guiProperties = screenHelper.getGuiProperties(screen).orElse(null);
			if (guiProperties != null) {
				return this.inputRouter.handleUserInput(screen, guiProperties, input, keybindings);
			}
		}
		return false;
	}

	/**
	 * Without keyboard focus, use Post
	 */
	public boolean onKeyboardKeyPressedPost(Screen screen, UserInput input) {
		if (isContainerTextFieldFocused(screen)) {
			IGuiProperties guiProperties = screenHelper.getGuiProperties(screen).orElse(null);
			if (guiProperties != null) {
				return this.inputRouter.handleUserInput(screen, guiProperties, input, keybindings);
			}
		}
		return false;
	}

	/**
	 * When we have keyboard focus, use Pre
	 */
	public boolean onKeyboardCharTypedPre(Screen screen, CharacterEvent event) {
		if (!isContainerTextFieldFocused(screen)) {
			return handleCharTyped(event);
		}
		return false;
	}

	/**
	 * Without keyboard focus, use Post
	 */
	public void onKeyboardCharTypedPost(Screen screen, CharacterEvent event) {
		if (isContainerTextFieldFocused(screen)) {
			handleCharTyped(event);
		}
	}

	public boolean onGuiMouseClicked(Screen screen, UserInput input) {
		if (InputQuirks.SIMULATE_RIGHT_CLICK_WITH_LONG_LEFT_CLICK && input.isMouseButton(1)
				&& Minecraft.getInstance().hasControlDown()
				&& GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().handle(), 0) == GLFW.GLFW_PRESS
				&& GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().handle(), 1) != GLFW.GLFW_PRESS) {
			physicalCtrlLeftClick = true;
			input = input.withMouseButton(0);
		}
		if (this.chatLinkInputHandler.handleUserInput(screen, input, keybindings)) {
			return true;
		}

		IGuiProperties guiProperties = screenHelper.getGuiProperties(screen).orElse(null);
		if (guiProperties == null) {
			return false;
		}

		if (this.dragRouter.isDragging() && input.isMouseButton(0)) {
			// an extra left click during a drag (i.e. multi-touch) must not cancel it; it ends on release
			return true;
		}

		boolean handled = this.inputRouter.handleUserInput(screen, guiProperties, input, keybindings);

		if (Minecraft.getInstance().screen == screen && input.isMouseButton(0)) {
			handled |= this.dragRouter.startDrag(screen, input);
		}
		return handled;
	}

	public boolean onGuiMouseReleased(Screen screen, UserInput input) {
		if (physicalCtrlLeftClick && input.isMouseButton(1)) {
			physicalCtrlLeftClick = false;
			input = input.withMouseButton(0);
		}
		if (this.chatLinkInputHandler.handleUserInput(screen, input, keybindings)) {
			return true;
		}

		IGuiProperties guiProperties = screenHelper.getGuiProperties(screen).orElse(null);
		if (guiProperties == null) {
			return false;
		}

		boolean handled = this.inputRouter.handleUserInput(screen, guiProperties, input, keybindings);

		if (input.isMouseButton(0)) {
			handled |= this.dragRouter.completeDrag(screen, input);
		}
		return handled;
	}

	public boolean onGuiMouseScroll(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
		return this.inputRouter.handleMouseScrolled(mouseX, mouseY, scrollDeltaX, scrollDeltaY);
	}

	public boolean onGuiMouseDragged(Screen screen, MouseButtonEvent event, double dragX, double dragY) {
		InputConstants.Key input = InputConstants.Type.MOUSE.getOrCreate(physicalCtrlLeftClick && event.button() == 1 ? 0 : event.button());
		return this.inputRouter.handleMouseDragged(event.x(), event.y(), input, dragX, dragY);
	}

	private boolean handleCharTyped(CharacterEvent event) {
		return this.charTypedHandlers.stream()
			.filter(ICharTypedHandler::hasKeyboardFocus)
			.anyMatch(handler -> handler.onCharTyped(event));
	}

	private boolean isContainerTextFieldFocused(Screen screen) {
		return reflectionUtil.getFieldWithClass(screen, EditBox.class)
			.anyMatch(textField -> textField.isActive() && textField.isFocused());
	}
}
