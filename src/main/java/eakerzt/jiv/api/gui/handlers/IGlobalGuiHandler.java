package eakerzt.jiv.api.gui.handlers;

import eakerzt.jiv.api.gui.builder.IClickableIngredientFactory;
import eakerzt.jiv.api.registration.IGuiHandlerRegistration;
import eakerzt.jiv.api.registration.IModIngredientRegistration;
import eakerzt.jiv.api.runtime.IClickableIngredient;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Allows plugins to change how JIV is displayed next to guis.
 * This is for mods that display next to all GUIs, like JIV does, so they can draw together correctly.
 * For handling modded GUIs, you should use {@link IGuiContainerHandler} instead.
 *
 * Register your implementation with {@link IGuiHandlerRegistration#addGlobalGuiHandler(IGlobalGuiHandler)}.
 *
 * @see IGuiContainerHandler
 */
public interface IGlobalGuiHandler {
	/**
	 * Give JIV information about extra space that your mod takes up.
	 * Used for moving JIV out of the way of extra things like gui buttons.
	 *
	 * @return the space that the gui takes up besides the normal rectangle defined by {@link AbstractContainerScreen}.
	 */
	default Collection<Rect2i> getGuiExtraAreas() {
		return Collections.emptyList();
	}

	/**
	 * Return a clickable ingredient under the mouse that JIV could not normally detect, used for JIV recipe lookups.
	 * <p>
	 * This is useful for guis that don't have normal slots (which is how JIV normally detects items under the mouse).
	 * <p>
	 * This can also be used to let JIV look up liquids in tanks directly, by returning a FluidStack.
	 * Works with any ingredient type that has been registered with {@link IModIngredientRegistration}.
	 *
	 * @param builder a builder to help with the creation of clickable ingredients.
	 * @param mouseX the current X position of the mouse in screen coordinates.
	 * @param mouseY the current Y position of the mouse in screen coordinates.
	 *
	 * @since 21.2.0
	 */
	default Optional<? extends IClickableIngredient<?>> getClickableIngredientUnderMouse(IClickableIngredientFactory builder, double mouseX, double mouseY) {
		return Optional.empty();
	}

}
