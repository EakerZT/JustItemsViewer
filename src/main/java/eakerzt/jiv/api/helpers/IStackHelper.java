package eakerzt.jiv.api.helpers;

import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.ingredients.subtypes.ISubtypeManager;
import eakerzt.jiv.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus;

/**
 * Helps get ItemStacks from common formats used in recipes.
 * Get the instance from {@link IJivHelpers#getStackHelper()}.
 */
@ApiStatus.NonExtendable
public interface IStackHelper {
	/**
	 * Gets the unique identifier for a stack, ignoring NBT on items without subtypes, and uses the {@link ISubtypeManager}.
	 * If two unique identifiers are equal, then the items can be considered equivalent.
	 *
	 * @since 19.9.0
	 */
	Object getUidForStack(ItemStack stack, UidContext context);

	/**
	 * Gets the unique identifier for a stack, ignoring NBT on items without subtypes, and uses the {@link ISubtypeManager}.
	 * If two unique identifiers are equal, then the items can be considered equivalent.
	 *
	 * @since 19.19.4
	 */
	Object getUidForStack(ITypedIngredient<ItemStack> stack, UidContext context);

	/**
	 * Similar to ItemStack.areItemStacksEqual but ignores NBT on items without subtypes, and uses the {@link ISubtypeManager}
	 * @since 7.3.0
	 */
	boolean isEquivalent(@Nullable ItemStack lhs, @Nullable ItemStack rhs, UidContext context);
}
