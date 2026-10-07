package eakerzt.jiv.api.registration;

import eakerzt.jiv.api.IModPlugin;
import eakerzt.jiv.api.helpers.IJivHelpers;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferHandler;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferHandlerHelper;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferInfo;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferListener;
import eakerzt.jiv.api.recipe.transfer.IUniversalRecipeTransferHandler;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jspecify.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus;

/**
 * Register recipe transfer handlers here to give JIV the information it needs to transfer recipes into the crafting area.
 * Get the instance passed in to your plugin's {@link IModPlugin#registerRecipeTransferHandlers}.
 */
@ApiStatus.NonExtendable
public interface IRecipeTransferRegistration {
	/**
	 * {@link IJivHelpers} provides helpers and tools for addon mods.
	 */
	IJivHelpers getJivHelpers();

	IRecipeTransferHandlerHelper getTransferHelper();

	/**
	 * Basic method for adding a recipe transfer handler.
	 *
	 * @param containerClass     the class of the container that this recipe transfer handler is for
	 * @param menuType           the optional menu type of the container that this recipe transfer handler is for
	 * @param recipeType         the recipe type that this container can use
	 * @param recipeSlotStart    the first slot for recipe inputs
	 * @param recipeSlotCount    the number of slots for recipe inputs
	 * @param inventorySlotStart the first slot of the available inventory (usually player inventory)
	 * @param inventorySlotCount the number of slots of the available inventory
	 *
	 * @since 11.0.0
	 */
	<C extends AbstractContainerMenu, R> void addRecipeTransferHandler(Class<? extends C> containerClass, @Nullable MenuType<C> menuType, IRecipeType<R> recipeType, int recipeSlotStart, int recipeSlotCount, int inventorySlotStart, int inventorySlotCount);

	/**
	 * Advanced method for adding a recipe transfer handler.
	 *
	 * Use this when recipe slots or inventory slots are spread out in different number ranges.
	 */
	<C extends AbstractContainerMenu, R> void addRecipeTransferHandler(IRecipeTransferInfo<C, R> recipeTransferInfo);

	/**
	 * Complete control over recipe transfer.
	 * Use this when the container has a non-standard inventory or crafting area.
	 *
	 * @since 9.5.0
	 */
	<C extends AbstractContainerMenu, R> void addRecipeTransferHandler(IRecipeTransferHandler<C, R> recipeTransferHandler, IRecipeType<R> recipeType);

	/**
	 * Add a universal handler that can handle any category of recipe.
	 * Useful for mods with recipe pattern encoding, for automated recipe systems.
	 *
	 * @since 19.8.1
	 */
	<C extends AbstractContainerMenu> void addUniversalRecipeTransferHandler(IUniversalRecipeTransferHandler<C> universalRecipeTransferHandler);

	/**
	 * Add a listener that observes attempts to transfer recipes through JIV.
	 *
	 * @since 29.35.0
	 */
	void addRecipeTransferListener(IRecipeTransferListener recipeTransferListener);
}
