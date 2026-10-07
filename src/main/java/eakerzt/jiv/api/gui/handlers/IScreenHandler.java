package eakerzt.jiv.api.gui.handlers;

import eakerzt.jiv.api.gui.builder.IClickableIngredientFactory;
import eakerzt.jiv.api.registration.IModIngredientRegistration;
import eakerzt.jiv.api.registration.IGuiHandlerRegistration;
import eakerzt.jiv.api.runtime.IClickableIngredient;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

/**
 * Creates {@link IGuiProperties} from a {@link Screen} so JIV can draw next to it.
 * By default, JIV already handles this for all {@link AbstractContainerScreen}.
 * Register a {@link IScreenHandler} with JIV by using {@link IGuiHandlerRegistration#addGuiScreenHandler(Class, IScreenHandler)}
 */
@FunctionalInterface
public interface IScreenHandler<T extends Screen> extends Function<T, IGuiProperties> {
	@Override
	@Nullable
	IGuiProperties apply(T guiScreen);

	/**
	 * Return a clickable ingredient under the mouse that JIV could not normally detect, used for JIV recipe lookups.
	 *
	 * This is useful for screens that don't have normal slots (which is how JIV normally detects items under the mouse).
	 *
	 * This can also be used to let JIV look up liquids in tanks directly, by returning a FluidStack.
	 * Works with any ingredient type that has been registered with {@link IModIngredientRegistration}.
	 *
	 * @param mouseX the current X position of the mouse in screen coordinates.
	 * @param mouseY the current Y position of the mouse in screen coordinates.
	 *
	 * @since 29.15.0
	 */
	default Optional<? extends IClickableIngredient<?>> getClickableIngredientUnderMouse(
		IClickableIngredientFactory factory,
		T screen,
		double mouseX,
		double mouseY
	) {
		return Optional.empty();
	}
}
