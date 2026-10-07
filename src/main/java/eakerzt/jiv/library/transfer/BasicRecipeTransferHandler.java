package eakerzt.jiv.library.transfer;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotView;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotsView;
import eakerzt.jiv.api.helpers.IStackHelper;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferContext;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferError;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferHandler;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferHandlerHelper;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferInfo;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import eakerzt.jiv.common.network.IConnectionToServer;
import eakerzt.jiv.common.network.packets.PacketRecipeTransferCountedWithResult;
import eakerzt.jiv.common.network.packets.PacketRecipeTransferResult;
import eakerzt.jiv.common.network.packets.PacketRecipeTransferWithResult;
import eakerzt.jiv.common.network.packets.legacy.PacketRecipeTransfer;
import eakerzt.jiv.common.network.packets.legacy.PacketRecipeTransferCounted;
import eakerzt.jiv.common.transfer.RecipeTransferOperationsResult;
import eakerzt.jiv.common.transfer.RecipeTransferUtil;
import eakerzt.jiv.common.transfer.TransferOperation;
import eakerzt.jiv.common.util.StringUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BasicRecipeTransferHandler<C extends AbstractContainerMenu, R> implements IRecipeTransferHandler<C, R> {
	private static final Logger LOGGER = LogManager.getLogger();

	private final IConnectionToServer serverConnection;
	private final IStackHelper stackHelper;
	private final IRecipeTransferHandlerHelper handlerHelper;
	private final IRecipeTransferInfo<C, R> transferInfo;

	public BasicRecipeTransferHandler(
		IConnectionToServer serverConnection,
		IStackHelper stackHelper,
		IRecipeTransferHandlerHelper handlerHelper,
		IRecipeTransferInfo<C, R> transferInfo
	) {
		this.serverConnection = serverConnection;
		this.stackHelper = stackHelper;
		this.handlerHelper = handlerHelper;
		this.transferInfo = transferInfo;
	}

	@Override
	public Class<? extends C> getContainerClass() {
		return transferInfo.getContainerClass();
	}

	@Override
	public Optional<MenuType<C>> getMenuType() {
		return transferInfo.getMenuType();
	}

	@Override
	public IRecipeType<R> getRecipeType() {
		return transferInfo.getRecipeType();
	}

	@Nullable
	@Override
	public IRecipeTransferError transferRecipe(IRecipeTransferContext<R, C> context, boolean doTransfer) {
		return transferRecipeInternal(
			context.getContainer(),
			context.getRecipe(),
			context.getRecipeSlots(),
			context.getPlayer(),
			context.isMaxTransfer(),
			doTransfer,
			context
		);
	}

	@Nullable
	private IRecipeTransferError transferRecipeInternal(
		C container,
		R recipe,
		IRecipeSlotsView recipeSlotsView,
		Player player,
		boolean maxTransfer,
		boolean doTransfer,
		IRecipeTransferContext<R, C> context
	) {
		if (!serverConnection.isJivOnServer()) {
			Component tooltipMessage = Component.translatable("jiv.tooltip.error.recipe.transfer.no.server");
			return handlerHelper.createUserErrorWithTooltip(tooltipMessage);
		}

		if (!transferInfo.canHandle(container, recipe)) {
			IRecipeTransferError handlingError = transferInfo.getHandlingError(container, recipe);
			if (handlingError != null) {
				return handlingError;
			}
			return handlerHelper.createInternalError();
		}

		List<Slot> craftingSlots = Collections.unmodifiableList(transferInfo.getRecipeSlots(container, recipe));
		List<Slot> inventorySlots = Collections.unmodifiableList(transferInfo.getInventorySlots(container, recipe));
		if (!validateTransferInfo(transferInfo, container, craftingSlots, inventorySlots)) {
			return handlerHelper.createInternalError();
		}

		List<IRecipeSlotView> inputItemSlotViews = recipeSlotsView.getSlotViews(RecipeIngredientRole.INPUT);
		if (!validateRecipeView(transferInfo, container, craftingSlots, inputItemSlotViews)) {
			return handlerHelper.createInternalError();
		}

		InventoryState inventoryState = getInventoryState(craftingSlots, inventorySlots, player, container, transferInfo);
		if (inventoryState == null) {
			return handlerHelper.createInternalError();
		}

		// check if we have enough inventory space to shuffle items around to their final locations
		int inputCount = (int) inputItemSlotViews.stream()
			.filter(slot -> !slot.isEmpty())
			.count();
		if (!inventoryState.hasRoom(inputCount)) {
			Component message = Component.translatable("jiv.tooltip.error.recipe.transfer.inventory.full");
			return handlerHelper.createUserErrorWithTooltip(message);
		}

		RecipeTransferOperationsResult transferOperations = RecipeTransferUtil.getRecipeTransferOperations(
			stackHelper,
			inventoryState.availableItemStacks,
			inputItemSlotViews,
			craftingSlots
		);

		if (!transferOperations.missingItems.isEmpty()) {
			Component message = Component.translatable("jiv.tooltip.error.recipe.transfer.missing");
			return handlerHelper.createUserErrorForMissingSlots(message, transferOperations.missingItems);
		}

		if (!RecipeTransferUtil.validateSlots(player, transferOperations.results, craftingSlots, inventorySlots)) {
			return handlerHelper.createInternalError();
		}

		boolean requiresCountedTransferPacket = requiresCountedTransferPacket(transferOperations.results);
		boolean useCountedTransferPacket = requiresCountedTransferPacket && serverConnection.canSendPacket(PacketRecipeTransferCounted.TYPE);

		if (doTransfer) {
			boolean requireCompleteSets = transferInfo.requireCompleteSets(container, recipe);
			boolean supportsTransferResults = supportsServerRecipeTransferResults(useCountedTransferPacket);
			if (supportsTransferResults) {
				sendTransferWithResult(
					transferOperations.results,
					craftingSlots,
					inventorySlots,
					maxTransfer,
					requireCompleteSets,
					useCountedTransferPacket,
					context
				);
			} else if (useCountedTransferPacket) {
				PacketRecipeTransferCounted packet = PacketRecipeTransferCounted.fromSlots(
					transferOperations.results,
					craftingSlots,
					inventorySlots,
					maxTransfer,
					requireCompleteSets
				);
				serverConnection.sendPacketToServer(packet);
			} else {
				PacketRecipeTransfer packet = PacketRecipeTransfer.fromSlots(
					transferOperations.results,
					craftingSlots,
					inventorySlots,
					maxTransfer,
					requireCompleteSets
				);
				serverConnection.sendPacketToServer(packet);
			}
		}

		return null;
	}

	private boolean supportsServerRecipeTransferResults(boolean useCountedTransferPacket) {
		if (useCountedTransferPacket) {
			return serverConnection.canSendPacket(PacketRecipeTransferCountedWithResult.TYPE);
		}
		return serverConnection.canSendPacket(PacketRecipeTransferWithResult.TYPE);
	}

	private void sendTransferWithResult(
		List<TransferOperation> transferOperations,
		List<Slot> craftingSlots,
		List<Slot> inventorySlots,
		boolean maxTransfer,
		boolean requireCompleteSets,
		boolean useCountedTransferPacket,
		IRecipeTransferContext<R, C> context
	) {
		PacketRecipeTransferResult.registerPendingRecipeTransfer(context);
		int transferId = context.getTransferId();
		if (useCountedTransferPacket) {
			PacketRecipeTransferCountedWithResult packet = PacketRecipeTransferCountedWithResult.fromSlots(
				transferOperations,
				craftingSlots,
				inventorySlots,
				maxTransfer,
				requireCompleteSets,
				transferId
			);
			serverConnection.sendPacketToServer(packet);
		} else {
			PacketRecipeTransferWithResult packet = PacketRecipeTransferWithResult.fromSlots(
				transferOperations,
				craftingSlots,
				inventorySlots,
				maxTransfer,
				requireCompleteSets,
				transferId
			);
			serverConnection.sendPacketToServer(packet);
		}
	}

	private static boolean requiresCountedTransferPacket(List<TransferOperation> transferOperations) {
		Set<Integer> craftingSlotIds = new IntOpenHashSet();
		for (TransferOperation transferOperation : transferOperations) {
			if (transferOperation.count() > 1 || !craftingSlotIds.add(transferOperation.craftingSlotId())) {
				return true;
			}
		}
		return false;
	}

	public static <C extends AbstractContainerMenu, R> boolean validateTransferInfo(
		IRecipeTransferInfo<C, R> transferInfo,
		C container,
		List<Slot> craftingSlots,
		List<Slot> inventorySlots
	) {
		for (Slot slot : craftingSlots) {
			if (slot.isFake()) {
				LOGGER.error("Recipe Transfer helper {} does not work for container {}. " +
					"The Recipe Transfer Helper references crafting slot index [{}] but it is a fake (output) slot, which is not allowed.",
					transferInfo.getClass(), container.getClass(), slot.index
				);
				return false;
			}
		}
		for (Slot slot : inventorySlots) {
			if (slot.isFake()) {
				LOGGER.error("Recipe Transfer helper {} does not work for container {}. " +
					"The Recipe Transfer Helper references inventory slot index [{}] but it is a fake (output) slot, which is not allowed.",
					transferInfo.getClass(), container.getClass(), slot.index
				);
				return false;
			}
		}
		Collection<Integer> craftingSlotIndexes = slotIndexes(craftingSlots);
		Collection<Integer> inventorySlotIndexes = slotIndexes(inventorySlots);
		Collection<Integer> containerSlotIndexes = slotIndexes(container.slots);

		if (!containerSlotIndexes.containsAll(craftingSlotIndexes)) {
			LOGGER.error("Recipe Transfer helper {} does not work for container {}. " +
				"The Recipes Transfer Helper references crafting slot indexes [{}] that are not found in the inventory container slots [{}]",
				transferInfo.getClass(), container.getClass(), StringUtil.intsToString(craftingSlotIndexes), StringUtil.intsToString(containerSlotIndexes)
			);
			return false;
		}

		if (!containerSlotIndexes.containsAll(inventorySlotIndexes)) {
			LOGGER.error("Recipe Transfer helper {} does not work for container {}. " +
				"The Recipes Transfer Helper references inventory slot indexes [{}] that are not found in the inventory container slots [{}]",
				transferInfo.getClass(), container.getClass(), StringUtil.intsToString(inventorySlotIndexes), StringUtil.intsToString(containerSlotIndexes)
			);
			return false;
		}

		return true;
	}

	public static <C extends AbstractContainerMenu, R> boolean validateRecipeView(
		IRecipeTransferInfo<C, R> transferInfo,
		C container,
		List<Slot> craftingSlots,
		List<IRecipeSlotView> inputSlots
	) {
		if (inputSlots.size() > craftingSlots.size()) {
			LOGGER.error("Recipe View {} does not work for container {}. " +
				"The Recipe View has more input slots ({}) than the number of inventory crafting slots ({})",
				transferInfo.getClass(), container.getClass(), inputSlots.size(), craftingSlots.size()
			);
			return false;
		}

		return true;
	}

	public static Set<Integer> slotIndexes(Collection<Slot> slots) {
		Set<Integer> set = new IntOpenHashSet(slots.size());
		for (Slot s : slots) {
			set.add(s.index);
		}
		return set;
	}

	@Nullable
	public static <C extends AbstractContainerMenu, R> InventoryState getInventoryState(
		Collection<Slot> craftingSlots,
		Collection<Slot> inventorySlots,
		Player player,
		C container,
		IRecipeTransferInfo<C, R> transferInfo
	) {
		Map<Slot, ItemStack> availableItemStacks = new HashMap<>();
		int filledCraftSlotCount = 0;
		int emptySlotCount = 0;

		for (Slot slot : craftingSlots) {
			final ItemStack stack = slot.getItem();
			if (!stack.isEmpty()) {
				if (!slot.allowModification(player)) {
					LOGGER.error(
						"Recipe Transfer helper {} does not work for container {}. The Player is not able to move items out of Crafting Slot number {}",
						transferInfo.getClass(), container.getClass(), slot.index
					);
					return null;
				}
				filledCraftSlotCount++;
				availableItemStacks.put(slot, stack.copy());
			}
		}

		for (Slot slot : inventorySlots) {
			final ItemStack stack = slot.getItem();
			if (!stack.isEmpty()) {
				if (slot.allowModification(player)) {
					availableItemStacks.put(slot, stack.copy());
				}
			} else if (slot.allowModification(player)) {
				emptySlotCount++;
			}
		}

		return new InventoryState(availableItemStacks, filledCraftSlotCount, emptySlotCount);
	}

	public record InventoryState(
		Map<Slot, ItemStack> availableItemStacks,
		int filledCraftSlotCount,
		int emptySlotCount
	) {
		/**
		 * check if we have enough inventory space to shuffle items around to their final locations
		 */
		public boolean hasRoom(int inputCount) {
			return filledCraftSlotCount - inputCount <= emptySlotCount;
		}
	}
}
