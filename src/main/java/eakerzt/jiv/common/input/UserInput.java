package eakerzt.jiv.common.input;

import com.mojang.datafixers.util.Either;
import com.mojang.blaze3d.platform.InputConstants;
import eakerzt.jiv.api.gui.inputs.IJivUserInput;
import eakerzt.jiv.api.runtime.IJivKeyMapping;
import eakerzt.jiv.common.platform.IPlatformInputHelper;
import eakerzt.jiv.common.platform.Services;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import org.apache.commons.lang3.function.ToBooleanBiFunction;

import java.util.Optional;
import java.util.function.Function;

public abstract class UserInput implements IJivUserInput {
	/** Mouse gesture routing must not reject clicks just because a modifier is held. */
	public final boolean isMouseButton(int button) {
		return getKey().getType() == InputConstants.Type.MOUSE && getKey().getValue() == button;
	}

	public final UserInput withMouseButton(int button) {
		return getEvent().map(data -> new MouseUserInput(
				new MouseButtonEvent(data.event().x(), data.event().y(),
						new MouseButtonInfo(button, data.event().modifiers())),
				data.doubleClicked(), getInputType()), key -> this);
	}
	@FunctionalInterface
	public interface MouseClickable {
		boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleClick);
	}

	public static UserInput fromVanilla(KeyEvent keyEvent, InputType inputType) {
		return new KeyUserInput(keyEvent, inputType);
	}

	public static Optional<UserInput> fromVanilla(MouseButtonEvent mouseButtonEvent, boolean doubleClick, InputType inputType) {
		int mouseButton = mouseButtonEvent.button();
		if (mouseButton < 0) {
			return Optional.empty();
		}
		UserInput userInput = new MouseUserInput(mouseButtonEvent, doubleClick, inputType);
		return Optional.of(userInput);
	}

	public abstract double getMouseX();

	public abstract double getMouseY();

	public abstract InputType getInputType();

	public abstract boolean isAllowedChatCharacter();

	@Override
	public final boolean is(IJivKeyMapping keyMapping) {
		return keyMapping.isActiveAndMatches(this.getKey());
	}

	@Override
	public final boolean is(KeyMapping keyMapping) {
		IPlatformInputHelper inputHelper = Services.PLATFORM.getInputHelper();
		return inputHelper.isActiveAndMatches(keyMapping, this.getKey(), this.getEvent());
	}

	public abstract Either<MouseButtonEventData, KeyEvent> getEvent();

	public final boolean ifMouseEvent(MouseClickable mouseClickable) {
		return getEvent().map(eventData -> mouseClickable.mouseClicked(eventData.event(), eventData.doubleClicked()), keyEvent -> false);
	}

	public final boolean callVanilla(
		ToBooleanBiFunction<Double, Double> isMouseOver,
		MouseClickable mouseClicked,
		Function<KeyEvent, Boolean> keyPressed
	) {
		return getEvent()
			.map(eventData -> {
				MouseButtonEvent event = eventData.event();
				boolean doubleClicked = eventData.doubleClicked();
				return isMouseOver.applyAsBoolean(event.x(), event.y()) &&
					mouseClicked.mouseClicked(event, doubleClicked);
			}, keyEvent -> {
				if (isSimulate()) {
					// key press simulate happens on key up, which we ignore
					return false;
				} else {
					return keyPressed.apply(keyEvent);
				}
			});
	}

}
