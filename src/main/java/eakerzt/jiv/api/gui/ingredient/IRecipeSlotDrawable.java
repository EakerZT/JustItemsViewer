package eakerzt.jiv.api.gui.ingredient;

import eakerzt.jiv.api.gui.builder.IIngredientAcceptor;
import eakerzt.jiv.api.gui.builder.IRecipeLayoutBuilder;
import eakerzt.jiv.api.gui.builder.IRecipeSlotBuilder;
import eakerzt.jiv.api.gui.builder.ITooltipBuilder;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.ApiStatus;

/**
 * A drawable recipe slot, useful if you need to make JIV draw a slot somewhere.
 *
 * Created from a {@link IRecipeSlotBuilder}, usually from {@link IRecipeLayoutBuilder#addSlot},
 * using the {@link IRecipeLayoutBuilder} given to mod plugins in {@link IRecipeCategory#setRecipe}.
 *
 * You can also create one for other purposes with {@link IGuiHelper#createRecipeSlotDrawable}.
 *
 * @since 11.5.0
 */
@ApiStatus.NonExtendable
public interface IRecipeSlotDrawable extends IRecipeSlotView {

	/**
	 * Draws the recipe slot relative to the pose stack.
	 *
	 * @since 29.7.0
	 */
	void draw(GuiGraphicsExtractor guiGraphics, boolean hovered);

	/**
	 * Draw the tooltip for this recipe slot at the given mouse position.
	 *
	 * @since 21.1.0
	 */
	void drawTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY);

	/**
	 * Adds the current rich tooltip, including ingredient candidates and slot callbacks.
	 * Useful for composing an interactive tooltip before drawing it.
	 */
	void addTooltip(ITooltipBuilder tooltip);

	/**
	 * Return true if the mouse is over the slot.
	 *
	 * @param mouseX relative to its parent element.
	 * @param mouseY relative to its parent element.
	 *
	 * @since 19.6.0
	 */
	boolean isMouseOver(double mouseX, double mouseY);

	/**
	 * Move this slot to the given position.
	 * @param x the new x coordinate, relative to its parent element.
	 * @param y the new y coordinate, relative to its parent element.
	 *
	 * @since 19.6.0
	 */
	void setPosition(int x, int y);

	/**
	 * Overrides the currently displayed ingredients.
	 * Set this from {@link IRecipeCategory#onDisplayedIngredientsUpdate} when the currently displayed ingredients change.
	 *
	 * @since 19.8.3
	 */
	IIngredientAcceptor<?> createDisplayOverrides();

	/**
	 * Removes any display overrides that were set with {@link #createDisplayOverrides()}.
	 *
	 * @since 19.8.3
	 */
	void clearDisplayOverrides();

	/**
	 * Get the area that this recipe slot draws on, including the area covered by its background texture.
	 * Useful for laying out other recipe elements relative to the slot.
	 *
	 * @since 19.19.3
	 */
	Rect2i getAreaIncludingBackground();
}
