package eakerzt.jiv.config.neoforge;

import eakerzt.jiv.config.minecraft.network.MinecraftPayloads;
import eakerzt.jiv.config.server.ServerConfigProtocol;
import eakerzt.jiv.config.server.ServerConfigRuntime;
import eakerzt.jiv.config.minecraft.network.SyncPacket;
import eakerzt.jiv.config.minecraft.network.IdentityPacket;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;

public final class ConfigNeoForgeNetwork {
	private ConfigNeoForgeNetwork() {

	}

	public static void register(IEventBus modEventBus) {
		modEventBus.addListener(ConfigNeoForgeNetwork::registerPayloads);
		MinecraftPayloads.registerSender((player, payload) -> {
			if (player.connection.hasChannel(payload.type())) {
				PacketDistributor.sendToPlayer(player, payload);
				return true;
			}
			return false;
		});
	}

	private static void registerPayloads(RegisterPayloadHandlersEvent event) {
		event.registrar(Integer.toString(ServerConfigProtocol.CHANNEL_VERSION))
			.executesOn(HandlerThread.MAIN)
			.optional()
			.playToClient(
				IdentityPacket.TYPE,
				IdentityPacket.STREAM_CODEC,
				(payload, context) -> ServerConfigRuntime.handleServerIdentity(payload.toPayload())
			)
			.playToClient(
				SyncPacket.TYPE,
				SyncPacket.STREAM_CODEC,
				(payload, context) -> ServerConfigRuntime.handleSyncChunk(payload.toPayload())
			);
	}
}
