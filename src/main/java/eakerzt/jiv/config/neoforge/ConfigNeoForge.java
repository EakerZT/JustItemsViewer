package eakerzt.jiv.config.neoforge;

import net.neoforged.api.distmarker.Dist;
import eakerzt.jiv.config.server.ServerConfigRuntime;
import eakerzt.jiv.config.minecraft.MinecraftConfigRuntime;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * NeoForge entry point for the config mod.
 */
public final class ConfigNeoForge {
	public static final String MOD_ID = "jiv";

	private ConfigNeoForge() {}

	public static void register(IEventBus modEventBus, Dist dist) {
		ConfigNeoForgeNetwork.register(modEventBus);
		NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> MinecraftConfigRuntime.onServerStarted(event.getServer()));
		NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> ServerConfigRuntime.onServerStopped());
		NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
			if (event.getEntity() instanceof ServerPlayer player) {
				MinecraftConfigRuntime.onPlayerJoin(player);
			}
		});
		if (dist.isClient()) {
			ConfigNeoForgeClient.register();
		}
	}
}
