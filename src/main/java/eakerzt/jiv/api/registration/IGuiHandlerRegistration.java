package eakerzt.jiv.api.registration;

import eakerzt.jiv.api.gui.handlers.IGhostIngredientHandler;
import eakerzt.jiv.api.gui.handlers.IGlobalGuiHandler;
import eakerzt.jiv.api.gui.handlers.IGuiClickableArea;
import eakerzt.jiv.api.gui.handlers.IGuiContainerHandler;
import eakerzt.jiv.api.gui.handlers.IScreenHandler;
import eakerzt.jiv.api.helpers.IJivHelpers;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.List;

@ApiStatus.NonExtendable
public interface IGuiHandlerRegistration {
	/**
	 * {@link IJivHelpers} provides helpers and tools for addon mods.
	 * @since 11.5.0
	 */
	IJivHelpers getJivHelpers();

	/**
	 * Add a handler to give JIV extra information about how to layout the item list next to a specific type of {@link AbstractContainerScreen}.
	 * Multiple handlers can be registered for one {@link AbstractContainerScreen}.
	 *
	 * @see #addGenericGuiContainerHandler(Class, IGuiContainerHandler) for handlers that use Java Generics
	 */
	<T extends AbstractContainerScreen<?>> void addGuiContainerHandler(Class<? extends T> guiClass, IGuiContainerHandler<T> guiHandler);

	/**
	 * Same as {@link #addGuiContainerHandler(Class, IGuiContainerHandler)} but for handlers that use Java Generics to
	 * support multiple types of containers. This type of handler runs into type issues with the regular method.
	 * @since 7.1.1
	 */
	<T extends AbstractContainerScreen<?>> void addGenericGuiContainerHandler(Class<? extends T> guiClass, IGuiContainerHandler<?> guiHandler);

	/**
	 * Add a handler to let JIV draw next to a specific class (or subclass) of {@link Screen}.
	 * By default, JIV can only draw next to {@link AbstractContainerScreen}.
	 */
	<T extends Screen> void addGuiScreenHandler(Class<T> guiClass, IScreenHandler<T> handler);

	/**
	 * Same as {@link #addGuiScreenHandler(Class, IScreenHandler)} but for handlers that use Java Generics to
	 * support multiple types of containers. This type of handler runs into type issues with the regular method.
	 * @since 22.0.0
	 */
	<T extends Screen> void addGenericGuiScreenHandler(Class<T> guiClass, IScreenHandler<?> handler);

	/**
	 * Add a handler to give JIV extra information about how to layout the ingredient list.
	 * Used for guis that display next to GUIs and would normally intersect with JIV.
	 */
	void addGlobalGuiHandler(IGlobalGuiHandler globalGuiHandler);

	/**
	 * Add a clickable area on a gui to jump to specific categories of recipes in JIV.
	 *
	 * @param containerScreenClass  the gui class for JIV to detect.
	 * @param xPos                  left x position of the clickable area, relative to the left edge of the gui.
	 * @param yPos                  top y position of the clickable area, relative to the top edge of the gui.
	 * @param width                 the width of the clickable area.
	 * @param height                the height of the clickable area.
	 * @param recipeTypes           the recipe types that JIV should display.
	 *
	 * @since 9.5.0
	 */
	default <T extends AbstractContainerScreen<?>> void addRecipeClickArea(Class<? extends T> containerScreenClass, int xPos, int yPos, int width, int height, IRecipeType<?>... recipeTypes) {
		this.addGuiContainerHandler(containerScreenClass, new IGuiContainerHandler<T>() {
			@Override
			public Collection<IGuiClickableArea> getGuiClickableAreas(T containerScreen, double mouseX, double mouseY) {
				IGuiClickableArea clickableArea = IGuiClickableArea.createBasic(xPos, yPos, width, height, recipeTypes);
				return List.of(clickableArea);
			}
		});
	}

	/**
	 * Lets mods accept ghost ingredients from JIV.
	 * These ingredients are dragged from the ingredient list on to your gui, and are useful
	 * for setting recipes or anything else that does not need the real ingredient to exist.
	 */
	<T extends Screen> void addGhostIngredientHandler(Class<T> guiClass, IGhostIngredientHandler<T> handler);

}
