package eakerzt.jiv.gui.input;

import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.runtime.IScreenHelper;
import eakerzt.jiv.common.util.ImmutableRect2i;
import eakerzt.jiv.gui.overlay.elements.IElement;
import eakerzt.jiv.gui.overlay.elements.IngredientElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.stream.Stream;

public class GuiContainerWrapper implements IRecipeFocusSource {
	private final IScreenHelper screenHelper;

	public GuiContainerWrapper(IScreenHelper screenHelper) {
		this.screenHelper = screenHelper;
	}

	@Override
	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		Screen guiScreen = Minecraft.getInstance().screen;
		if (guiScreen == null) {
			return Stream.empty();
		}
		return screenHelper.getClickableIngredientUnderMouse(guiScreen, mouseX, mouseY)
			.map(clickableSlot -> {
				ITypedIngredient<?> typedIngredient = clickableSlot.getTypedIngredient();
				ImmutableRect2i area = new ImmutableRect2i(clickableSlot.getArea());
				IElement<?> element = new IngredientElement<>(typedIngredient);
				return new ClickableIngredientInternal<>(element, area::contains, false, false);
			});
	}

	@Override
	public Stream<IDraggableIngredientInternal<?>> getDraggableIngredientUnderMouse(double mouseX, double mouseY) {
		return Stream.empty();
	}
}
