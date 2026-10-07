package eakerzt.jiv.api.runtime;

import eakerzt.jiv.api.constants.VanillaTypes;
import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

/**
 * The {@link IBookmarkOverlay} is JIV's gui that displays all the bookmarked ingredients next to an open container gui.
 * Use this interface to get information from it.
 * Get the instance from {@link IJivRuntime#getBookmarkOverlay()}.
 */
@ApiStatus.NonExtendable
public interface IBookmarkOverlay {
	/**
	 * @return the ingredient that's currently under the mouse.
	 * @since 9.3.0
	 */
	Optional<ITypedIngredient<?>> getIngredientUnderMouse();

	/**
	 * @return the ingredient that's currently under the mouse, or null if there is none.
	 */
	@Nullable
	<T> T getIngredientUnderMouse(IIngredientType<T> ingredientType);

	/**
	 * @return the ingredient that's currently under the mouse, or null if there is none.
	 */
	@Nullable
	default ItemStack getItemStackUnderMouse() {
		return getIngredientUnderMouse(VanillaTypes.ITEM_STACK);
	}
}
