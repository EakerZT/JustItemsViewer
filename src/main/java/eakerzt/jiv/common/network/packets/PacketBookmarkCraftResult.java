package eakerzt.jiv.common.network.packets;

import eakerzt.jiv.api.constants.ModIds;
import eakerzt.jiv.common.network.ClientPacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class PacketBookmarkCraftResult extends PlayToClientPacket<PacketBookmarkCraftResult> {
	public static final Type<PacketBookmarkCraftResult> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ModIds.JIV_ID, "bookmark_craft_result"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PacketBookmarkCraftResult> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, p -> p.requestId, ByteBufCodecs.BOOL, p -> p.success,
			PacketBookmarkCraftResult::new);
	private static final Map<Integer, Consumer<Boolean>> pending = new HashMap<>();
	private static int nextId = 1;
	private final int requestId;
	private final boolean success;

	public PacketBookmarkCraftResult(int requestId, boolean success) {
		this.requestId = requestId;
		this.success = success;
	}

	public static int register(Consumer<Boolean> completion) {
		int id = nextId++;
		if (nextId <= 0) nextId = 1;
		pending.put(id, completion);
		return id;
	}

	public static void cancel(int requestId) { pending.remove(requestId); }
	@Override public Type<PacketBookmarkCraftResult> type() { return TYPE; }
	@Override public StreamCodec<RegistryFriendlyByteBuf, PacketBookmarkCraftResult> streamCodec() { return STREAM_CODEC; }
	@Override public void process(ClientPacketContext context) {
		var completion = pending.remove(requestId);
		if (completion != null) completion.accept(success);
	}
}
