package eakerzt.jiv.api.recipe.transfer;

import eakerzt.jiv.api.recipe.types.IRecipeType;
import eakerzt.jiv.api.registration.IRecipeTransferRegistration;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * A recipe transfer handler moves items into a crafting area, based on the items in a recipe.
 *
 * Implementing this interface gives full control over the recipe transfer process.
 * Mods that use a regular slotted inventory can use {@link IRecipeTransferInfo} instead, which is much simpler.
 *
 * Useful functions for implementing a recipe transfer handler can be found in {@link IRecipeTransferHandlerHelper}.
 *
 * To register your recipe transfer handler, use {@link IRecipeTransferRegistration#addRecipeTransferHandler(IRecipeTransferHandler, IRecipeType)}
 */
public interface IRecipeTransferHandler<C extends AbstractContainerMenu, R> {

	/**
	 * The container that this recipe transfer handler can use.
	 */
	Class<? extends C> getContainerClass();

	/**
	 * Return the optional menu type that this recipe transfer helper supports.
	 * This is used to optionally narrow down the type of container handled by this recipe transfer handler.
	 */
	Optional<MenuType<C>> getMenuType();

	/**
	 * The recipe that this recipe transfer handler can use.
	 */
	IRecipeType<R> getRecipeType();

	/**
	 * Handles a recipe transfer or checks whether it can be transferred.
	 *
	 * When {@code doTransfer} is true, call
	 * {@link IRecipeTransferContext#completeRecipeTransfer(RecipeTransferResult)}
	 * when the transfer has finished.
	 *
	 * @param context information about the recipe transfer
	 * @param doTransfer if true, do the transfer. if false, check for errors but do not transfer any items
	 * @return a recipe transfer error if the recipe can't be transferred. Return null on success.
	 *
	 * @since 29.35.0
	 */
	@Nullable
	IRecipeTransferError transferRecipe(IRecipeTransferContext<R, C> context, boolean doTransfer);
}
