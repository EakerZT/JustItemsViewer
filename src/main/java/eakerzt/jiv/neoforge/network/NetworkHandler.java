package eakerzt.jiv.neoforge.network;

import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.config.IServerConfig;
import eakerzt.jiv.common.network.ClientPacketContext;
import eakerzt.jiv.common.network.IConnectionToClient;
import eakerzt.jiv.common.network.IConnectionToServer;
import eakerzt.jiv.common.network.ServerPacketContext;
import eakerzt.jiv.common.network.packets.PacketCheatPermission;
import eakerzt.jiv.common.network.packets.PacketDeletePlayerItem;
import eakerzt.jiv.common.network.packets.PacketGiveItemStack;
import eakerzt.jiv.common.network.packets.PacketRecipeTransferCountedWithResult;
import eakerzt.jiv.common.network.packets.PacketRecipeTransferResult;
import eakerzt.jiv.common.network.packets.PacketRecipeTransferWithResult;
import eakerzt.jiv.common.network.packets.PacketRequestCheatPermission;
import eakerzt.jiv.common.network.packets.PacketSetHotbarItemStack;
import eakerzt.jiv.common.network.packets.PlayToClientPacket;
import eakerzt.jiv.common.network.packets.PlayToServerPacket;
import eakerzt.jiv.common.network.packets.legacy.PacketRecipeTransfer;
import eakerzt.jiv.common.network.packets.legacy.PacketRecipeTransferCounted;
import eakerzt.jiv.neoforge.events.PermanentEventSubscriptions;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.HandlerThread;

import java.util.function.BiConsumer;

public class NetworkHandler {
	private final String protocolVersion;
	private final IServerConfig serverConfig;
	private final IConnectionToServer connectionToServer;
	private final IConnectionToClient connectionToClient;

	public NetworkHandler(String protocolVersion, IServerConfig serverConfig) {
		this.protocolVersion = protocolVersion;
		this.serverConfig = serverConfig;

		this.connectionToServer = new ConnectionToServer();
		Internal.setServerConnection(this.connectionToServer);
		this.connectionToClient = new ConnectionToClient();
	}

	public void registerPacketHandlers(PermanentEventSubscriptions subscriptions) {
		subscriptions.register(RegisterPayloadHandlersEvent.class, ev -> {
			ev.registrar(this.protocolVersion)
				.executesOn(HandlerThread.MAIN)
				.optional()
				.playToServer(PacketDeletePlayerItem.TYPE, PacketDeletePlayerItem.STREAM_CODEC, wrapServerHandler(PacketDeletePlayerItem::process))
				.playToServer(PacketGiveItemStack.TYPE, PacketGiveItemStack.STREAM_CODEC, wrapServerHandler(PacketGiveItemStack::process))
				.playToServer(PacketRecipeTransfer.TYPE, PacketRecipeTransfer.STREAM_CODEC, wrapServerHandler(PacketRecipeTransfer::process))
				.playToServer(PacketRecipeTransferCounted.TYPE, PacketRecipeTransferCounted.STREAM_CODEC, wrapServerHandler(PacketRecipeTransferCounted::process))
				.playToServer(PacketRecipeTransferWithResult.TYPE, PacketRecipeTransferWithResult.STREAM_CODEC, wrapServerHandler(PacketRecipeTransferWithResult::process))
				.playToServer(PacketRecipeTransferCountedWithResult.TYPE, PacketRecipeTransferCountedWithResult.STREAM_CODEC, wrapServerHandler(PacketRecipeTransferCountedWithResult::process))
				.playToServer(PacketSetHotbarItemStack.TYPE, PacketSetHotbarItemStack.STREAM_CODEC, wrapServerHandler(PacketSetHotbarItemStack::process))
				.playToServer(PacketRequestCheatPermission.TYPE, PacketRequestCheatPermission.STREAM_CODEC, wrapServerHandler(PacketRequestCheatPermission::process))
				.playToClient(PacketCheatPermission.TYPE, PacketCheatPermission.STREAM_CODEC, wrapClientHandler(PacketCheatPermission::process))
				.playToClient(PacketRecipeTransferResult.TYPE, PacketRecipeTransferResult.STREAM_CODEC, wrapClientHandler(PacketRecipeTransferResult::process));
		});
	}

	private <T extends PlayToClientPacket<T>> IPayloadHandler<T> wrapClientHandler(BiConsumer<T, ClientPacketContext> consumer) {
		return (t, payloadContext) -> {
			LocalPlayer player = (LocalPlayer) payloadContext.player();
			var clientPacketContext = new ClientPacketContext(player, connectionToServer);
			payloadContext.enqueueWork(() -> {
				consumer.accept(t, clientPacketContext);
			});
		};
	}

	private <T extends PlayToServerPacket<T>> IPayloadHandler<T> wrapServerHandler(BiConsumer<T, ServerPacketContext> consumer) {
		return (t, payloadContext) -> {
			ServerPlayer player = (ServerPlayer) payloadContext.player();
			var serverPacketContext = new ServerPacketContext(player, serverConfig, connectionToClient);
			payloadContext.enqueueWork(() -> {
				consumer.accept(t, serverPacketContext);
			});
		};
	}

	public IConnectionToServer getConnectionToServer() {
		return connectionToServer;
	}
}
