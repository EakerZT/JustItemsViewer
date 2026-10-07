package eakerzt.jiv.common.gui;

import com.mojang.blaze3d.platform.InputConstants;
import eakerzt.jiv.api.gui.inputs.IJivInputHandler;
import eakerzt.jiv.api.gui.inputs.IJivUserInput;
import eakerzt.jiv.common.util.MathUtil;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;

import java.util.function.Supplier;

public class OffsetJivInputHandler implements IJivInputHandler {
	private final IJivInputHandler inputHandler;
	private final Supplier<ScreenPosition> offset;

	public OffsetJivInputHandler(IJivInputHandler inputHandler, Supplier<ScreenPosition> offset) {
		this.inputHandler = inputHandler;
		this.offset = offset;
	}

	@Override
	public boolean handleInput(double mouseX, double mouseY, IJivUserInput input) {
		ScreenPosition screenPosition = offset.get();
		final double offsetMouseX = mouseX - screenPosition.x();
		final double offsetMouseY = mouseY - screenPosition.y();

		ScreenRectangle originalArea = inputHandler.getArea();
		if (MathUtil.contains(originalArea, offsetMouseX, offsetMouseY)) {
			ScreenPosition position = originalArea.position();
			double relativeMouseX = offsetMouseX - position.x();
			double relativeMouseY = offsetMouseY - position.y();
			return inputHandler.handleInput(relativeMouseX, relativeMouseY, input);
		}

		return false;
	}

	@Override
	public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
		ScreenPosition screenPosition = offset.get();
		final double offsetMouseX = mouseX - screenPosition.x();
		final double offsetMouseY = mouseY - screenPosition.y();

		ScreenRectangle originalArea = inputHandler.getArea();
		if (MathUtil.contains(originalArea, offsetMouseX, offsetMouseY)) {
			ScreenPosition position = originalArea.position();
			double relativeMouseX = offsetMouseX - position.x();
			double relativeMouseY = offsetMouseY - position.y();
			return inputHandler.handleMouseScrolled(relativeMouseX, relativeMouseY, scrollDeltaX, scrollDeltaY);
		}

		return false;
	}

	@Override
	public boolean handleMouseDragged(double mouseX, double mouseY, InputConstants.Key mouseKey, double dragX, double dragY) {
		ScreenPosition screenPosition = offset.get();
		final double offsetMouseX = mouseX - screenPosition.x();
		final double offsetMouseY = mouseY - screenPosition.y();

		ScreenRectangle originalArea = inputHandler.getArea();
		if (MathUtil.contains(originalArea, offsetMouseX, offsetMouseY)) {
			ScreenPosition position = originalArea.position();
			double relativeMouseX = offsetMouseX - position.x();
			double relativeMouseY = offsetMouseY - position.y();
			return inputHandler.handleMouseDragged(relativeMouseX, relativeMouseY, mouseKey, dragX, dragY);
		}

		return false;
	}

	@Override
	public void handleMouseMoved(double mouseX, double mouseY) {
		ScreenPosition screenPosition = offset.get();
		final double offsetMouseX = mouseX - screenPosition.x();
		final double offsetMouseY = mouseY - screenPosition.y();

		ScreenRectangle originalArea = inputHandler.getArea();
		if (MathUtil.contains(originalArea, offsetMouseX, offsetMouseY)) {
			ScreenPosition position = originalArea.position();
			double relativeMouseX = offsetMouseX - position.x();
			double relativeMouseY = offsetMouseY - position.y();
			inputHandler.handleMouseMoved(relativeMouseX, relativeMouseY);
		}
	}

	@Override
	public ScreenRectangle getArea() {
		ScreenRectangle area = inputHandler.getArea();
		return new ScreenRectangle(offset.get(), area.width(), area.height());
	}
}
