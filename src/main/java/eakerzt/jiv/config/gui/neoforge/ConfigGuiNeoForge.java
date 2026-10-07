package eakerzt.jiv.config.gui.neoforge;

import eakerzt.jiv.config.gui.remote.RemoteConfigEditorServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * NeoForge entry point for the config GUI mod.
 */
public final class ConfigGuiNeoForge {
	public static final String MOD_ID = "jiv";

	private ConfigGuiNeoForge() {}

	public static void register(IEventBus modEventBus, Dist dist) {
		ConfigGuiNeoForgeNetwork.register(modEventBus);
		NeoForge.EVENT_BUS.addListener(
			(ServerStartedEvent event) -> RemoteConfigEditorServer.onServerStarted(event.getServer())
		);
		NeoForge.EVENT_BUS.addListener(
			(ServerStoppedEvent event) -> RemoteConfigEditorServer.onServerStopped(event.getServer())
		);
		NeoForge.EVENT_BUS.addListener(
			(ServerTickEvent.Post event) -> RemoteConfigEditorServer.onServerTick(event.getServer())
		);
		NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
			if (event.getEntity() instanceof ServerPlayer player) {
				RemoteConfigEditorServer.onPlayerDisconnect(player);
			}
		});
		if (dist.isClient()) {
			ConfigGuiNeoForgeClient.register(modEventBus);
		}
	}
}
