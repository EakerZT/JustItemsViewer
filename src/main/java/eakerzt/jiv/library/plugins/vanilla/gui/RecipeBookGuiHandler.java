package eakerzt.jiv.library.plugins.vanilla.gui;

import eakerzt.jiv.api.gui.handlers.IGuiContainerHandler;
import eakerzt.jiv.common.platform.IPlatformScreenHelper;
import eakerzt.jiv.common.platform.Services;
import eakerzt.jiv.common.util.ImmutableRect2i;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.RecipeBookMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecipeBookGuiHandler<C extends RecipeBookMenu, T extends AbstractRecipeBookScreen<C>> implements IGuiContainerHandler<T> {
	/**
	 * Modeled after {@link RecipeBookComponent#render(GuiGraphicsExtractor, int, int, float)}
	 */
	@Override
	public List<Rect2i> getGuiExtraAreas(T containerScreen) {
		IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();

		RecipeBookComponent<?> guiRecipeBook = screenHelper.getRecipeBookComponent(containerScreen);
		if (guiRecipeBook.isVisible()) {
			List<Rect2i> extraAreas = new ArrayList<>();
			ImmutableRect2i bookArea = screenHelper.getBookArea(guiRecipeBook);
			extraAreas.add(bookArea.toMutable());
			for (RecipeBookTabButton tab : screenHelper.getTabButtons(guiRecipeBook)) {
				if (tab.visible) {
					extraAreas.add(new Rect2i(tab.getX(), tab.getY(), tab.getWidth(), tab.getHeight()));
				}
			}
			return extraAreas;
		}
		return Collections.emptyList();
	}
}
