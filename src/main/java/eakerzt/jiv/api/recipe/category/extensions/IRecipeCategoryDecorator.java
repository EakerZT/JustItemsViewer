package eakerzt.jiv.api.recipe.category.extensions;

import eakerzt.jiv.api.gui.builder.ITooltipBuilder;
import eakerzt.jiv.api.gui.drawable.IDrawable;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotsView;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.api.recipe.category.IRecipeCategory;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import eakerzt.jiv.api.registration.IAdvancedRegistration;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * The {@link IRecipeCategoryDecorator} allows further customization of recipe categories.
 * It can be used to draw additional elements or tooltips on recipes, even of other mods.
 * <p>
 * Register it with {@link IAdvancedRegistration#addRecipeCategoryDecorator(IRecipeType, IRecipeCategoryDecorator)}.
 *
 * @since 15.1.0
 */
public interface IRecipeCategoryDecorator<T> {
	/**
	 * Draw extras or additional info about the recipe after the {@link IRecipeCategory} that
	 * this decorator is registered to has drawn.
	 * Tooltips are handled by {@link #decorateTooltips(ITooltipBuilder, Object, IRecipeCategory, IRecipeSlotsView, double, double)}.
	 *
	 * @param recipe          the current recipe being drawn.
	 * @param recipeCategory  the recipe category of the recipe.
	 * @param recipeSlotsView a view of the current recipe slots being drawn.
	 * @param guiGraphics     the current {@link GuiGraphicsExtractor} for rendering.
	 * @param mouseX          the X position of the mouse, relative to the recipe.
	 * @param mouseY          the Y position of the mouse, relative to the recipe.
	 *
	 * @see IDrawable for a simple class for drawing things.
	 * @see IGuiHelper for useful functions.
	 * @see IRecipeSlotsView for information about the ingredients that are currently being drawn.
	 *
	 * @since 15.1.0
	 */
	default void draw(T recipe, IRecipeCategory<T> recipeCategory, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {

	}

	/**
	 * Allows adding to existing tooltips added by the {@link IRecipeCategory} that
	 * this decorator is registered to.
	 *
	 * @param tooltip         the existing tooltip strings.
	 * @param recipe          the current recipe being drawn.
	 * @param recipeCategory  the recipe category of the recipe.
	 * @param recipeSlotsView a view of the current recipe slots being drawn.
	 * @param mouseX          the X position of the mouse, relative to the recipe.
	 * @param mouseY          the Y position of the mouse, relative to the recipe.
	 *
	 * @since 19.5.4
	 */
	default void decorateTooltips(ITooltipBuilder tooltip, T recipe, IRecipeCategory<T> recipeCategory, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {

	}
}
