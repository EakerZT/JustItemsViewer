package eakerzt.jiv.api.gui.drawable;

import eakerzt.jiv.api.gui.ITickTimer;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotsView;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * An animated {@link IDrawable}, useful for showing a gui animation like furnace flames or progress arrows.
 *
 * Useful for drawing miscellaneous things in
 * {@link IRecipeCategory#draw(Object, IRecipeSlotsView, GuiGraphicsExtractor, double, double)}.
 *
 * To create an instance, call
 * {@link IGuiHelper#createAnimatedDrawable(IDrawableStatic, int, StartDirection, boolean)}
 *
 * Internally, these use an {@link ITickTimer} to simulate tick-driven animations.
 */
public interface IDrawableAnimated extends IDrawable {
	/**
	 * The direction that the animation starts from.
	 *
	 * @see IGuiHelper#createAnimatedDrawable(IDrawableStatic, int, StartDirection, boolean)
	 */
	enum StartDirection {
		TOP, BOTTOM, LEFT, RIGHT
	}
}
