package eakerzt.jiv.config.gui.neoforge;

import eakerzt.jiv.config.gui.remote.RemoteConfigEditor;
import eakerzt.jiv.config.gui.remote.RemoteConfigEditorServer;
import eakerzt.jiv.config.gui.remote.RemoteConfigNetworking;
import eakerzt.jiv.config.gui.remote.RemoteConfigRequestChunkPayload;
import eakerzt.jiv.config.gui.remote.RemoteConfigResponseChunkPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;

public final class ConfigGuiNeoForgeNetwork {
	private static final String PROTOCOL_VERSION = "1";

	private ConfigGuiNeoForgeNetwork() {

	}

	public static void register(IEventBus modEventBus) {
		modEventBus.addListener(ConfigGuiNeoForgeNetwork::registerPayloads);
		RemoteConfigNetworking.setServerSender((player, payload) -> {
			if (!player.connection.hasChannel(payload.type())) {
				return false;
			}
			PacketDistributor.sendToPlayer(player, payload);
			return true;
		});
	}

	private static void registerPayloads(RegisterPayloadHandlersEvent event) {
		event.registrar(PROTOCOL_VERSION)
			.executesOn(HandlerThread.MAIN)
			.optional()
			.playToServer(
				RemoteConfigRequestChunkPayload.TYPE,
				RemoteConfigRequestChunkPayload.STREAM_CODEC,
				(payload, context) -> RemoteConfigEditorServer.handleRequestChunk((ServerPlayer) context.player(), payload)
			)
			.playToClient(
				RemoteConfigResponseChunkPayload.TYPE,
				RemoteConfigResponseChunkPayload.STREAM_CODEC,
				(payload, context) -> RemoteConfigEditor.handleResponseChunk(payload)
			);
	}
}
