package eakerzt.jiv.common.network.packets;

import eakerzt.jiv.api.constants.ModIds;
import eakerzt.jiv.common.network.ServerPacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;

/** Take exactly one vanilla craft through its normal result-slot callbacks. */
public final class PacketBookmarkCraft extends PlayToServerPacket<PacketBookmarkCraft> {
	public static final Type<PacketBookmarkCraft> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ModIds.JIV_ID, "bookmark_craft"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PacketBookmarkCraft> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, p -> p.menuId, ByteBufCodecs.VAR_INT, p -> p.requestId,
			ItemStack.STREAM_CODEC, p -> p.expectedOutput, PacketBookmarkCraft::new);
	private final int menuId;
	private final int requestId;
	private final ItemStack expectedOutput;

	public PacketBookmarkCraft(int menuId, int requestId, ItemStack expectedOutput) {
		this.menuId = menuId;
		this.requestId = requestId;
		this.expectedOutput = expectedOutput.copy();
	}

	@Override public Type<PacketBookmarkCraft> type() { return TYPE; }
	@Override public StreamCodec<RegistryFriendlyByteBuf, PacketBookmarkCraft> streamCodec() { return STREAM_CODEC; }
	@Override public void process(ServerPacketContext context) {
		boolean success = craft(context);
		context.player().containerMenu.broadcastChanges();
		context.connection().sendPacketToClient(new PacketBookmarkCraftResult(requestId, success), context.player());
	}

	private boolean craft(ServerPacketContext context) {
		var player = context.player();
		var menu = player.containerMenu;
		if (menu.containerId != menuId || !(menu instanceof CraftingMenu || menu instanceof InventoryMenu)
				|| !menu.stillValid(player) || !menu.getCarried().isEmpty() || expectedOutput.isEmpty()) return false;
		var result = menu.slots.stream().filter(s -> s instanceof ResultSlot).findFirst().orElse(null);
		if (result == null || !result.mayPickup(player)) return false;
		ItemStack output = result.getItem();
		if (!ItemStack.isSameItemSameComponents(output, expectedOutput)
				|| output.getCount() != expectedOutput.getCount()) return false;
		// Only the 36 main inventory slots: armor and offhand are never destinations.
		var destinations = menu.slots.stream().filter(s -> s.container == player.getInventory()
				&& s.getContainerSlot() >= 0 && s.getContainerSlot() < 36 && s.mayPlace(output)).toList();
		long capacity = 0;
		for (var slot : destinations) {
			if (slot.getItem().isEmpty() || ItemStack.isSameItemSameComponents(slot.getItem(), output))
				capacity += Math.max(0, slot.getMaxStackSize(output) - slot.getItem().getCount());
		}
		if (capacity < output.getCount()) return false;
		menu.clicked(result.index, 0, ContainerInput.PICKUP, player);
		for (var slot : destinations) {
			var carried = menu.getCarried();
			if (carried.isEmpty()) break;
			if (slot.getItem().isEmpty() || ItemStack.isSameItemSameComponents(slot.getItem(), carried))
				menu.clicked(slot.index, 0, ContainerInput.PICKUP, player);
		}
		if (!menu.getCarried().isEmpty()) return false;
		// Return containers and reusable tools through the menu's normal transfer logic.
		int inputSlots = menu instanceof CraftingMenu ? 9 : 4;
		for (int i = 1; i <= inputSlots; i++) {
			if (menu.getSlot(i).hasItem()) menu.clicked(i, 0, ContainerInput.QUICK_MOVE, player);
		}
		return true;
	}
}
