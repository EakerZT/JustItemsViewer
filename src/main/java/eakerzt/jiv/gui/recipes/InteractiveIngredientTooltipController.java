package eakerzt.jiv.gui.recipes;

import com.mojang.blaze3d.platform.InputConstants;
import eakerzt.jiv.api.gui.handlers.IGuiProperties;
import eakerzt.jiv.api.gui.inputs.RecipeSlotUnderMouse;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.input.IInternalKeyMappings;
import eakerzt.jiv.common.input.IGuiInputLayer;
import eakerzt.jiv.gui.input.IClickableIngredientInternal;
import eakerzt.jiv.common.input.IMouseOverable;
import eakerzt.jiv.gui.input.IPinnedTooltipHolder;
import eakerzt.jiv.common.input.IUserInputHandler;
import eakerzt.jiv.gui.input.PinnedTooltipManager;
import eakerzt.jiv.common.input.UserInput;
import eakerzt.jiv.gui.util.FocusUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.stream.Stream;

final class InteractiveIngredientTooltipController implements IGuiInputLayer, IPinnedTooltipHolder {
	private final RecipesGui recipesGui;
	private final FocusUtil focusUtil;
	private final IGuiHelper guiHelper;
	private final IIngredientManager ingredientManager;
	private final RecipeSlotClickTargetFactory clickTargetFactory;

	private @Nullable InteractiveIngredientTooltip activeTooltip;

	public InteractiveIngredientTooltipController(
		RecipesGui recipesGui,
		FocusUtil focusUtil,
		IGuiHelper guiHelper,
		IIngredientManager ingredientManager,
		RecipeSlotClickTargetFactory clickTargetFactory
	) {
		this.recipesGui = recipesGui;
		this.focusUtil = focusUtil;
		this.guiHelper = guiHelper;
		this.ingredientManager = ingredientManager;
		this.clickTargetFactory = clickTargetFactory;
	}

	public boolean isVisible() {
		return this.activeTooltip != null;
	}

	boolean isActive(InteractiveIngredientTooltip tooltip) {
		return this.activeTooltip == tooltip;
	}

	public void hide() {
		InteractiveIngredientTooltip activeTooltip = this.activeTooltip;
		if (activeTooltip != null) {
			activeTooltip.unfocus();
			this.activeTooltip = null;
			PinnedTooltipManager.closed(this);
		}
	}

	void hide(InteractiveIngredientTooltip tooltip) {
		if (isActive(tooltip)) {
			hide();
		}
	}

	public boolean show(
		RecipeSlotUnderMouse sourceSlot,
		IMouseOverable sourceMouseOverable,
		double mouseX,
		double mouseY
	) {
		Optional<InteractiveIngredientTooltip> tooltip = InteractiveIngredientTooltip.create(
			this,
			this.recipesGui,
			this.focusUtil,
			this.guiHelper,
			this.ingredientManager,
			this.clickTargetFactory,
			sourceSlot,
			sourceMouseOverable,
			mouseX,
			mouseY
		);
		if (tooltip.isEmpty()) {
			return false;
		}
		hide();
		this.activeTooltip = tooltip.get();
		PinnedTooltipManager.opened(this);
		return true;
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		InteractiveIngredientTooltip activeTooltip = this.activeTooltip;
		return activeTooltip != null && activeTooltip.isMouseOver(mouseX, mouseY);
	}

	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		InteractiveIngredientTooltip activeTooltip = this.activeTooltip;
		if (activeTooltip == null) {
			return Stream.empty();
		}
		return activeTooltip.getIngredientUnderMouse(mouseX, mouseY);
	}

	@Override
	public void draw(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		InteractiveIngredientTooltip activeTooltip = this.activeTooltip;
		if (activeTooltip != null) {
			PinnedTooltipManager.draw(this, () -> activeTooltip.draw(guiGraphics, mouseX, mouseY));
		}
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(
		Screen screen,
		IGuiProperties guiProperties,
		UserInput input,
		IInternalKeyMappings keyBindings
	) {
		InteractiveIngredientTooltip activeTooltip = this.activeTooltip;
		if (activeTooltip == null) {
			return Optional.empty();
		}
		return activeTooltip.handleUserInput(screen, guiProperties, input, keyBindings);
	}

	@Override
	public Optional<IUserInputHandler> handleMouseScrolled(
		double mouseX,
		double mouseY,
		double scrollDeltaX,
		double scrollDeltaY
	) {
		InteractiveIngredientTooltip activeTooltip = this.activeTooltip;
		if (activeTooltip == null) {
			return Optional.empty();
		}
		return activeTooltip.handleMouseScrolled(mouseX, mouseY, scrollDeltaX, scrollDeltaY);
	}

	@Override
	public Optional<IUserInputHandler> handleMouseDragged(
		double mouseX,
		double mouseY,
		InputConstants.Key mouseKey,
		double dragX,
		double dragY
	) {
		InteractiveIngredientTooltip activeTooltip = this.activeTooltip;
		if (activeTooltip == null) {
			return Optional.empty();
		}
		return activeTooltip.handleMouseDragged(mouseX, mouseY, mouseKey, dragX, dragY);
	}

	@Override
	public void unfocus() {
		InteractiveIngredientTooltip activeTooltip = this.activeTooltip;
		if (activeTooltip != null) {
			activeTooltip.unfocus();
		}
	}
}
