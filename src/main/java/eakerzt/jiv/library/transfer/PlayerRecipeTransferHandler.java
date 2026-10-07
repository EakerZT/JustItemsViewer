package eakerzt.jiv.library.transfer;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import eakerzt.jiv.api.constants.RecipeTypes;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotView;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotsView;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferContext;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferError;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferHandler;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferHandlerHelper;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import eakerzt.jiv.common.transfer.RecipeTransferContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class PlayerRecipeTransferHandler implements IRecipeTransferHandler<InventoryMenu, RecipeHolder<CraftingRecipe>> {
	/**
	 * Indexes from the crafting recipe inputs that fit into the player crafting grid
	 * when we trim the right and bottom edges.
	 */
	private static final IntSet PLAYER_INV_INDEXES = IntArraySet.of(0, 1, 3, 4);

	private final IRecipeTransferHandlerHelper handlerHelper;
	private final IRecipeTransferHandler<InventoryMenu, RecipeHolder<CraftingRecipe>> handler;

	public PlayerRecipeTransferHandler(IRecipeTransferHandlerHelper handlerHelper) {
		this.handlerHelper = handlerHelper;
		var basicRecipeTransferInfo = handlerHelper.createBasicRecipeTransferInfo(InventoryMenu.class, null, RecipeTypes.CRAFTING, 1, 4, 9, 36);
		this.handler = handlerHelper.createUnregisteredRecipeTransferHandler(basicRecipeTransferInfo);
	}

	@Override
	public Class<? extends InventoryMenu> getContainerClass() {
		return handler.getContainerClass();
	}

	@Override
	public Optional<MenuType<InventoryMenu>> getMenuType() {
		return handler.getMenuType();
	}

	@Override
	public IRecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
		return RecipeTypes.CRAFTING;
	}

	@Nullable
	@Override
	public IRecipeTransferError transferRecipe(IRecipeTransferContext<RecipeHolder<CraftingRecipe>, InventoryMenu> context, boolean doTransfer) {
		if (!handlerHelper.recipeTransferHasServerSupport()) {
			Component tooltipMessage = Component.translatable("jiv.tooltip.error.recipe.transfer.no.server");
			return this.handlerHelper.createUserErrorWithTooltip(tooltipMessage);
		}

		List<IRecipeSlotView> slotViews = context.getRecipeSlots().getSlotViews(RecipeIngredientRole.INPUT);
		if (!validateIngredientsOutsidePlayerGridAreEmpty(slotViews)) {
			Component tooltipMessage = Component.translatable(
				"jiv.tooltip.error.recipe.transfer.too.large.player.inventory"
			);
			return this.handlerHelper.createUserErrorWithTooltip(tooltipMessage);
		}

		List<IRecipeSlotView> filteredSlotViews = filterSlots(slotViews);
		IRecipeSlotsView filteredRecipeSlots = this.handlerHelper.createRecipeSlotsView(filteredSlotViews);
		var filteredContext = RecipeTransferContext.copyWithRecipeSlots(context, filteredRecipeSlots);
		return this.handler.transferRecipe(filteredContext, doTransfer);
	}

	private static boolean validateIngredientsOutsidePlayerGridAreEmpty(List<IRecipeSlotView> slotViews) {
		int bound = slotViews.size();
		for (int i = 0; i < bound; i++) {
			if (!PLAYER_INV_INDEXES.contains(i)) {
				IRecipeSlotView slotView = slotViews.get(i);
				if (!slotView.isEmpty()) {
					return false;
				}
			}
		}
		return true;
	}

	private static List<IRecipeSlotView> filterSlots(List<IRecipeSlotView> slotViews) {
		return PLAYER_INV_INDEXES.intStream()
			.mapToObj(slotViews::get)
			.toList();
	}
}
