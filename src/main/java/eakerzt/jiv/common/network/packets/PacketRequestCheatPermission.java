package eakerzt.jiv.common.network.packets;

import eakerzt.jiv.api.constants.ModIds;
import eakerzt.jiv.common.config.IServerConfig;
import eakerzt.jiv.common.network.IConnectionToClient;
import eakerzt.jiv.common.network.ServerPacketContext;
import eakerzt.jiv.common.util.ServerCommandUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class PacketRequestCheatPermission extends PlayToServerPacket<PacketRequestCheatPermission> {
	public static final PacketRequestCheatPermission INSTANCE = new PacketRequestCheatPermission();
	public static final CustomPacketPayload.Type<PacketRequestCheatPermission> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(ModIds.JIV_ID, "request_cheat_permission"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PacketRequestCheatPermission> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	private PacketRequestCheatPermission() {

	}

	@Override
	public Type<PacketRequestCheatPermission> type() {
		return TYPE;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, PacketRequestCheatPermission> streamCodec() {
		return STREAM_CODEC;
	}

	@Override
	public void process(ServerPacketContext context) {
		ServerPlayer player = context.player();
		IServerConfig serverConfig = context.serverConfig();
		boolean hasPermission = ServerCommandUtil.hasPermissionForCheatMode(player, serverConfig);
		PacketCheatPermission packetCheatPermission = new PacketCheatPermission(hasPermission, serverConfig);

		IConnectionToClient connection = context.connection();
		connection.sendPacketToClient(packetCheatPermission, player);
	}
}
