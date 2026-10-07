package eakerzt.jiv.api.gui.builder;

import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * A chainable interface that accepts typed ingredients.
 * Has convenience functions to make adding ingredients easier.
 *
 * @see IRecipeLayoutBuilder
 * @see IRecipeSlotBuilder
 *
 * @since 9.3.0
 */
@ApiStatus.NonExtendable
public interface IIngredientAcceptor<THIS extends IIngredientAcceptor<THIS>> {
	/**
	 * Add a slot display that contains {@link ItemStack}s.
	 *
	 * @since 20.0.0
	 */
	THIS add(SlotDisplay slotDisplay);

	/**
	 * Add a slot display that contains the specified type of ingredients.
	 *
	 * @since 28.2.0
	 */
	<I> THIS add(IIngredientType<I> ingredientType, SlotDisplay slotDisplay);

	/**
	 * Add one {@link ItemStack}.
	 *
	 * @since 20.0.0
	 */
	THIS add(ItemStack itemStack);

	/**
	 * Add one {@link ItemLike}.
	 *
	 * @since 20.0.0
	 */
	THIS add(ItemLike itemLike);

	/**
	 * Add one {@link ItemStackTemplate}.
	 *
	 * @since 29.4.0
	 */
	THIS add(ItemStackTemplate itemStackTemplate);

	/**
	 * Convenience helper to add one Fluid ingredient with the default amount (one bucket).
	 *
	 * To add multiple Fluid ingredients, you can call this multiple times.
	 *
	 * @see #add(Fluid, long) to add a Fluid with an amount.
	 * @see #add(Fluid, long, DataComponentPatch) to add a Fluid with a {@link DataComponentPatch}.
	 * @since 20.0.0
	 */
	THIS add(Fluid fluid);

	/**
	 * Convenience helper to add one Fluid ingredient.
	 *
	 * To add multiple Fluid ingredients, you can call this multiple times.
	 *
	 * @see #add(Fluid, long) to add a Fluid with the default amount.
	 * @see #add(Fluid, long, DataComponentPatch) to add a Fluid with a {@link DataComponentPatch}.
	 * @since 20.0.0
	 */
	THIS add(Fluid fluid, long amount);

	/**
	 * Convenience helper to add one Fluid ingredient with a {@link DataComponentPatch}.
	 *
	 * To add multiple Fluid ingredients, you can call this multiple times.
	 *
	 * @see #add(Fluid, long) to add a Fluid with the default amount.
	 * @see #add(Fluid, long) to add a Fluid without a {@link DataComponentPatch}.
	 * @since 20.0.0
	 */
	THIS add(Fluid fluid, long amount, DataComponentPatch component);

	/**
	 * Convenience function to add an ordered list of ingredients from an {@link Ingredient}.
	 *
	 * @since 20.0.0
	 */
	THIS add(Ingredient ingredient);

	/**
	 * Add an Ingredient that contains the specified type of ingredients.
	 *
	 * @since 28.2.0
	 */
	<I> THIS add(IIngredientType<I> ingredientType, Ingredient ingredient);

	/**
	 * Add one typed ingredient.
	 *
	 * @since 20.0.0
	 */
	<I> THIS add(ITypedIngredient<I> typedIngredient);

	/**
	 * Add one ingredient with a custom {@link IIngredientType}.
	 *
	 * @since 20.0.0
	 */
	<I> THIS add(IIngredientType<I> ingredientType, I ingredient);

	/**
	 * Add an ordered list of ingredients.
	 *
	 * @since 9.3.0
	 */
	<I> THIS addIngredients(IIngredientType<I> ingredientType, List<@Nullable I> ingredients);

	/**
	 * Add an ordered list of ingredients.
	 * The type of ingredients can be mixed, as long as they are all valid ingredient types.
	 * Prefer using {@link #addIngredients(IIngredientType, List)} for type safety.
	 *
	 * @since 9.3.0
	 */
	THIS addIngredientsUnsafe(List<?> ingredients);

	/**
	 * Convenience function to add an ordered non-null list of typed ingredients.
	 *
	 * @param ingredients a non-null list of ingredients for the slot
	 *
	 * @since 19.6.0
	 */
	THIS addTypedIngredients(List<ITypedIngredient<?>> ingredients);

	/**
	 * Convenience function to add an ordered non-null list of typed ingredients.
	 * {@link Optional#empty()} ingredients will be shown as blank in the rotation.
	 *
	 * @param ingredients a non-null list of optional ingredients for the slot
	 *
	 * @since 19.6.0
	 */
	THIS addOptionalTypedIngredients(List<Optional<ITypedIngredient<?>>> ingredients);

	/**
	 * Convenience function to add an order list of {@link ItemStack}.
	 *
	 * @since 9.3.0
	 */
	THIS addItemStacks(List<ItemStack> itemStacks);

	/**
	 * @return the current context for resolving recipe displays.
	 *
	 * @since 29.12.0
	 * @apiNote Use this when resolving {@link SlotDisplay}s directly.
	 * If you add {@link SlotDisplay}s to this acceptor, JIV will resolve them with this context.
	 */
	ContextMap getContextMap();

}
