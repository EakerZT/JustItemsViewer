package eakerzt.jiv.common.network.packets;

import eakerzt.jiv.api.constants.ModIds;
import eakerzt.jiv.common.config.GiveMode;
import eakerzt.jiv.common.network.ServerPacketContext;
import eakerzt.jiv.common.network.codecs.EnumStreamCodec;
import eakerzt.jiv.common.util.ServerCommandUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class PacketGiveItemStack extends PlayToServerPacket<PacketGiveItemStack> {
	public static final CustomPacketPayload.Type<PacketGiveItemStack> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(ModIds.JIV_ID, "give_item_stack"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PacketGiveItemStack> STREAM_CODEC = StreamCodec.composite(
		ItemStack.STREAM_CODEC,
		p -> p.itemStack,
		new EnumStreamCodec<>(GiveMode.class),
		p -> p.giveMode,
		PacketGiveItemStack::new
	);

	private final ItemStack itemStack;
	private final GiveMode giveMode;

	public PacketGiveItemStack(ItemStack itemStack, GiveMode giveMode) {
		this.itemStack = itemStack;
		this.giveMode = giveMode;
	}

	@Override
	public Type<PacketGiveItemStack> type() {
		return TYPE;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, PacketGiveItemStack> streamCodec() {
		return STREAM_CODEC;
	}

	@Override
	public void process(ServerPacketContext context) {
		ServerCommandUtil.executeGive(context, itemStack, giveMode);
	}
}
