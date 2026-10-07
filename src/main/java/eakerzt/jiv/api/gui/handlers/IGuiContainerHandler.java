package eakerzt.jiv.api.gui.handlers;

import eakerzt.jiv.api.gui.builder.IClickableIngredientFactory;
import eakerzt.jiv.api.registration.IGuiHandlerRegistration;
import eakerzt.jiv.api.registration.IModIngredientRegistration;
import eakerzt.jiv.api.runtime.IClickableIngredient;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Allows plugins to change how JIV is displayed next to their mod's guis.
 * Register your implementation with {@link IGuiHandlerRegistration#addGuiContainerHandler(Class, IGuiContainerHandler)}.
 */
public interface IGuiContainerHandler<T extends AbstractContainerScreen<?>> {
	/**
	 * Give JIV information about extra space that the {@link AbstractContainerScreen} takes up.
	 * Used for moving JIV out of the way of extra things like gui tabs.
	 *
	 * @return the space that the gui takes up besides the normal rectangle defined by {@link AbstractContainerScreen}.
	 */
	default List<Rect2i> getGuiExtraAreas(T containerScreen) {
		return Collections.emptyList();
	}

	/**
	 * Return a clickable ingredient under the mouse that JIV could not normally detect, used for JIV recipe lookups.
	 *
	 * This is useful for guis that don't have normal slots (which is how JIV normally detects items under the mouse).
	 *
	 * This can also be used to let JIV look up liquids in tanks directly, by returning a FluidStack.
	 * Works with any ingredient type that has been registered with {@link IModIngredientRegistration}.
	 *
	 * @param mouseX the current X position of the mouse in screen coordinates.
	 * @param mouseY the current Y position of the mouse in screen coordinates.
	 *
	 * @since 21.2.0
	 */
	default Optional<? extends IClickableIngredient<?>> getClickableIngredientUnderMouse(
		IClickableIngredientFactory builder,
		T containerScreen,
		double mouseX,
		double mouseY
	) {
		return Optional.empty();
	}

	/**
	 * Return the JIV-controlled clickable areas for this GUI.
	 * This is useful when you want to add a spot on your GUI that opens JIV and shows recipes.
	 *
	 * Optionally, you can restrict what you return here based on the current mouse position.
	 * @param guiMouseX the current X position of the mouse in gui-relative coordinates.
	 * @param guiMouseY the current Y position of the mouse in gui-relative coordinates.
	 * @since 6.0.1
	 */
	default Collection<IGuiClickableArea> getGuiClickableAreas(T containerScreen, double guiMouseX, double guiMouseY) {
		return Collections.emptyList();
	}
}
