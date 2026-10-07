package eakerzt.jiv.api.gui.builder;

import eakerzt.jiv.api.gui.drawable.IDrawable;
import eakerzt.jiv.api.gui.drawable.TilingDirection;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotsView;
import eakerzt.jiv.api.gui.placement.HorizontalAlignment;
import eakerzt.jiv.api.gui.placement.IPlaceable;
import eakerzt.jiv.api.gui.placement.VerticalAlignment;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.api.ingredients.IIngredientRenderer;
import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.recipe.category.IRecipeCategory;
import eakerzt.jiv.api.registration.IModIngredientRegistration;
import org.jetbrains.annotations.ApiStatus;

/**
 * Allows setting properties of a slot on a {@link IRecipeLayoutBuilder}.
 * Implements {@link IIngredientAcceptor} to add ingredients to the slot.
 *
 * @see IIngredientAcceptor for methods to add ingredients to this builder.
 *
 * @since 9.3.0
 */
@ApiStatus.NonExtendable
public interface IRecipeSlotBuilder extends IIngredientAcceptor<IRecipeSlotBuilder>, IPlaceable<IRecipeSlotBuilder> {
	/**
	 * Add a callback to alter the rich tooltip for this slot.
	 * The callback is invoked while the slot is hovered even when it has no displayed ingredient,
	 * so backgrounds, overlays, and placeholders can provide tooltips.
	 *
	 * @see IRecipeSlotRichTooltipCallback
	 *
	 * @since 19.8.5
	 */
	IRecipeSlotBuilder addRichTooltipCallback(IRecipeSlotRichTooltipCallback tooltipCallback);

	/**
	 * Give the slot a unique name, for looking it up later by using
	 * {@link IRecipeSlotsView#findSlotByName(String)}
	 * in {@link IRecipeCategory#draw}
	 *
	 * @since 9.3.0
	 */
	IRecipeSlotBuilder setSlotName(String slotName);

	/**
	 * Set a normal slot background to draw behind the slot's ingredients.
	 * This background is 18x18 pixels and offset by (-1, -1) to match vanilla slots.
	 *
	 * @see IGuiHelper#getSlotDrawable() for the slot background drawable.
	 *
	 * @since 19.18.7
	 */
	IRecipeSlotBuilder setStandardSlotBackground();

	/**
	 * Set a normal slot background to draw behind the slot's ingredients.
	 * This background is 26x26 pixels and offset by (-5, -5) to match vanilla output slots.
	 *
	 * @see IGuiHelper#getOutputSlot() for the slot background drawable.
	 *
	 * @since 19.18.8
	 */
	IRecipeSlotBuilder setOutputSlotBackground();

	/**
	 * Set a custom background to draw behind the slot's ingredients.
	 *
	 * @param xOffset The amount to offset the background from the ingredient in the X direction.
	 *                May be negative, the background can be drawn larger than the ingredient.
	 * @param yOffset The amount to offset the background from the ingredient in the Y direction.
	 *                May be negative, the background can be drawn larger than the ingredient.
	 *
	 * @since 9.3.0
	 */
	IRecipeSlotBuilder setBackground(IDrawable background, int xOffset, int yOffset);

	/**
	 * Set an overlay to draw on top of the slot's ingredient.
	 *
	 * @param xOffset The amount to offset the overlay from the ingredient in the X direction.
	 *                May be negative, the overlay can be drawn larger than the ingredient.
	 * @param yOffset The amount to offset the overlay from the ingredient in the Y direction.
	 *                May be negative, the overlay can be drawn larger than the ingredient.
	 *
	 * @since 9.3.0
	 */
	IRecipeSlotBuilder setOverlay(IDrawable overlay, int xOffset, int yOffset);

	/**
	 * Set the properties of this slot's fluid renderer.
	 * This will be used to render any fluid ingredients in the slot.
	 *
	 * If no fluid renderer is set, the default 16x16 renderer is used.
	 *
	 * Uses {@link TilingDirection#UP_RIGHT} to keep the texture aligned to the bottom-left.
	 *
	 * @param capacity     maximum amount of fluid that this "tank" can hold
	 * @param showCapacity set {@code true} to show the capacity in the tooltip
	 * @param width        width of the fluid renderer
	 * @param height       height of the fluid renderer
	 *
	 * @since 10.1.0
	 */
	IRecipeSlotBuilder setFluidRenderer(long capacity, boolean showCapacity, int width, int height);

	/**
	 * Enables a compact fluid amount label at the bottom-right of this slot. Disabled by default.
	 * Reads the currently displayed fluid, including cycling candidates and display overrides.
	 * Non-fluid and non-positive amounts draw nothing. Uses mB without a suffix below 10,000,
	 * then integer K/M/G/T/P/E abbreviations. Text scales down to fit the slot.
	 * Independent of renderer configuration and tooltip capacity; call order does not matter.
	 * Drawn after a custom overlay without replacing it. Item count rendering is unchanged.
	 */
	IRecipeSlotBuilder setShowFluidAmount(boolean showFluidAmount);

	/** Marks this slot as non-consumed, with a native marker and tooltip. Disabled by default.
	 * Applies to any ingredient and role; this is display metadata, not gameplay logic.
	 * Draws a green infinity marker inside the top-left of non-empty slots and adds a tooltip.
	 * Independent of custom overlays, item counts and fluid amount labels.
	 */
	IRecipeSlotBuilder setNonConsumed(boolean nonConsumed);

	/** Sets a finite probability in [0,1] and enables its native label and tooltip.
	 * Supports item/fluid inputs and outputs. A probability of one hides the compact label,
	 * but keeps the tooltip. Off by default; each call enables it again.
	 * Reserve six pixels above the slot for the compact label; the tooltip keeps full precision.
	 * Does not replace custom overlays or item/fluid counts. Empty slots have no marker or metadata tooltip.
	 * This does not perform consumption, production or random rolls.
	 * @throws IllegalArgumentException if chance is non-finite or outside [0,1]
	 */
	IRecipeSlotBuilder setChance(double chance);

	/** Shows or hides the configured probability label and tooltip without changing its value. */
	IRecipeSlotBuilder setShowChance(boolean showChance);

	/**
	 * Set the properties of this slot's fluid renderer.
	 * This will be used to render any fluid ingredients in the slot.
	 *
	 * If no fluid renderer is set, the default 16x16 renderer is used.
	 *
	 * @param capacity        maximum amount of fluid that this "tank" can hold
	 * @param showCapacity    set {@code true} to show the capacity in the tooltip
	 * @param width           width of the fluid renderer
	 * @param height          height of the fluid renderer
	 * @param tilingDirection controls which direction the texture is tiled from
	 *
	 * @since 29.18.0
	 */
	IRecipeSlotBuilder setFluidRenderer(long capacity, boolean showCapacity, int width, int height, TilingDirection tilingDirection);

	/**
	 * Set a custom renderer for the given ingredient type for this slot.
	 *
	 * If no custom renderer is set, the default 16x16 renderer from
	 * {@link IModIngredientRegistration#register} is used.
	 *
	 * @implNote if multiple renderers are set, they must all have the same
	 * {@link IIngredientRenderer#getWidth()} and
	 * {@link IIngredientRenderer#getWidth()}
	 * so that they can render together in rotation in the same space.
	 *
	 * @param ingredientType     the type of ingredient to use the custom renderer on
	 * @param ingredientRenderer the custom ingredient renderer to use for this type
	 *
	 * @since 9.3.0
	 */
	<T> IRecipeSlotBuilder setCustomRenderer(
		IIngredientType<T> ingredientType,
		IIngredientRenderer<T> ingredientRenderer
	);

	@Override
	IRecipeSlotBuilder setPosition(int xPos, int yPos);

	@Override
	IRecipeSlotBuilder setPosition(
		int areaX,
		int areaY,
		int areaWidth,
		int areaHeight,
		HorizontalAlignment horizontalAlignment,
		VerticalAlignment verticalAlignment
	);

	@Override
	int getWidth();

	@Override
	int getHeight();
}
