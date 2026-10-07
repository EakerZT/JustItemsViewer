package eakerzt.jiv.library.gui.recipes;

import com.mojang.blaze3d.platform.InputConstants;
import eakerzt.jiv.api.gui.inputs.IJivGuiEventListener;
import eakerzt.jiv.api.gui.inputs.IJivInputHandler;
import eakerzt.jiv.api.gui.inputs.IJivUserInput;
import eakerzt.jiv.common.util.MathUtil;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.Rect2i;

import java.util.ArrayList;
import java.util.List;

public class RecipeLayoutInputHandler<T> implements IJivInputHandler {
	private final RecipeLayout<T> recipeLayout;
	private final List<IJivInputHandler> inputHandlers;
	private final List<IJivGuiEventListener> guiEventListeners;

	public RecipeLayoutInputHandler(
		RecipeLayout<T> recipeLayout
	) {
		this.recipeLayout = recipeLayout;
		this.inputHandlers = new ArrayList<>();
		this.guiEventListeners = new ArrayList<>();
	}

	@Override
	public ScreenRectangle getArea() {
		Rect2i area = recipeLayout.getRect();
		return new ScreenRectangle(area.getX(), area.getY(), area.getWidth(), area.getHeight());
	}

	@Override
	public boolean handleInput(double mouseX, double mouseY, IJivUserInput userInput) {
		if (!recipeLayout.isMouseOver(mouseX, mouseY)) {
			return false;
		}

		Rect2i area = recipeLayout.getRect();
		final double recipeMouseX = mouseX - area.getX();
		final double recipeMouseY = mouseY - area.getY();

		for (IJivInputHandler inputHandler : inputHandlers) {
			ScreenRectangle widgetArea = inputHandler.getArea();
			if (MathUtil.contains(widgetArea, recipeMouseX, recipeMouseY)) {
				ScreenPosition position = widgetArea.position();
				double relativeMouseX = recipeMouseX - position.x();
				double relativeMouseY = recipeMouseY - position.y();
				if (inputHandler.handleInput(relativeMouseX, relativeMouseY, userInput)) {
					return true;
				}
			}
		}
		for (IJivGuiEventListener guiEventListener : guiEventListeners) {
			ScreenRectangle widgetArea = guiEventListener.getArea();
			if (MathUtil.contains(widgetArea, recipeMouseX, recipeMouseY)) {
				ScreenPosition position = widgetArea.position();
				double relativeMouseX = recipeMouseX - position.x();
				double relativeMouseY = recipeMouseY - position.y();
				if (handleInput(guiEventListener, relativeMouseX, relativeMouseY, userInput)) {
					return true;
				}
			}
		}

		return false;
	}

	private static boolean handleInput(IJivGuiEventListener guiEventListener, double relativeMouseX, double relativeMouseY, IJivUserInput userInput) {
		InputConstants.Key key = userInput.getKey();
		switch (key.getType()) {
			case MOUSE -> {
				if (userInput.isSimulate()) {
					return guiEventListener.mouseClicked(relativeMouseX, relativeMouseY, key.getValue());
				} else {
					return guiEventListener.mouseReleased(relativeMouseX, relativeMouseY, key.getValue());
				}
			}
			case KEYSYM -> {
				if (!userInput.isSimulate()) {
					return guiEventListener.keyPressed(relativeMouseX, relativeMouseY, key.getValue(), 0, userInput.getModifiers());
				}
			}
			default -> {
				return false;
			}
		}
		return false;
	}

	@Override
	public boolean handleMouseDragged(double mouseX, double mouseY, InputConstants.Key mouseKey, double dragX, double dragY) {
		if (!recipeLayout.isMouseOver(mouseX, mouseY)) {
			return false;
		}

		Rect2i area = recipeLayout.getRect();
		final double recipeMouseX = mouseX - area.getX();
		final double recipeMouseY = mouseY - area.getY();

		for (IJivInputHandler inputHandler : inputHandlers) {
			ScreenRectangle widgetArea = inputHandler.getArea();
			if (MathUtil.contains(widgetArea, recipeMouseX, recipeMouseY)) {
				ScreenPosition position = widgetArea.position();
				double relativeMouseX = recipeMouseX - position.x();
				double relativeMouseY = recipeMouseY - position.y();
				if (inputHandler.handleMouseDragged(relativeMouseX, relativeMouseY, mouseKey, dragX, dragY)) {
					return true;
				}
			}
		}
		for (IJivGuiEventListener guiEventListener : guiEventListeners) {
			ScreenRectangle widgetArea = guiEventListener.getArea();
			if (MathUtil.contains(widgetArea, recipeMouseX, recipeMouseY)) {
				ScreenPosition position = widgetArea.position();
				double relativeMouseX = recipeMouseX - position.x();
				double relativeMouseY = recipeMouseY - position.y();
				if (guiEventListener.mouseDragged(relativeMouseX, relativeMouseY, mouseKey.getValue(), dragX, dragY)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
		if (!recipeLayout.isMouseOver(mouseX, mouseY)) {
			return false;
		}

		Rect2i area = recipeLayout.getRect();
		final double recipeMouseX = mouseX - area.getX();
		final double recipeMouseY = mouseY - area.getY();

		for (IJivInputHandler inputHandler : inputHandlers) {
			ScreenRectangle widgetArea = inputHandler.getArea();
			if (MathUtil.contains(widgetArea, recipeMouseX, recipeMouseY)) {
				ScreenPosition position = widgetArea.position();
				double relativeMouseX = recipeMouseX - position.x();
				double relativeMouseY = recipeMouseY - position.y();
				if (inputHandler.handleMouseScrolled(relativeMouseX, relativeMouseY, scrollDeltaX, scrollDeltaY)) {
					return true;
				}
			}
		}
		for (IJivGuiEventListener guiEventListener : guiEventListeners) {
			ScreenRectangle widgetArea = guiEventListener.getArea();
			if (MathUtil.contains(widgetArea, recipeMouseX, recipeMouseY)) {
				ScreenPosition position = widgetArea.position();
				double relativeMouseX = recipeMouseX - position.x();
				double relativeMouseY = recipeMouseY - position.y();
				if (guiEventListener.mouseScrolled(relativeMouseX, relativeMouseY, scrollDeltaX, scrollDeltaY)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void handleMouseMoved(double mouseX, double mouseY) {
		if (!recipeLayout.isMouseOver(mouseX, mouseY)) {
			return;
		}

		Rect2i area = recipeLayout.getRect();
		final double recipeMouseX = mouseX - area.getX();
		final double recipeMouseY = mouseY - area.getY();

		for (IJivInputHandler inputHandler : inputHandlers) {
			ScreenRectangle widgetArea = inputHandler.getArea();
			if (MathUtil.contains(widgetArea, recipeMouseX, recipeMouseY)) {
				ScreenPosition position = widgetArea.position();
				double relativeMouseX = recipeMouseX - position.x();
				double relativeMouseY = recipeMouseY - position.y();
				inputHandler.handleMouseMoved(relativeMouseX, relativeMouseY);
			}
		}
		for (IJivGuiEventListener guiEventListener : guiEventListeners) {
			ScreenRectangle widgetArea = guiEventListener.getArea();
			if (MathUtil.contains(widgetArea, recipeMouseX, recipeMouseY)) {
				ScreenPosition position = widgetArea.position();
				double relativeMouseX = recipeMouseX - position.x();
				double relativeMouseY = recipeMouseY - position.y();
				guiEventListener.mouseMoved(relativeMouseX, relativeMouseY);
			}
		}
	}

	public void addInputHandler(IJivInputHandler inputHandler) {
		this.inputHandlers.add(inputHandler);
	}

	public void addGuiEventListener(IJivGuiEventListener guiEventListener) {
		this.guiEventListeners.add(guiEventListener);
	}
}
