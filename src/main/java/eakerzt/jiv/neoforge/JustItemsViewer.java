package eakerzt.jiv.neoforge;

import eakerzt.jiv.api.constants.ModIds;
import eakerzt.jiv.common.config.IServerConfig;
import eakerzt.jiv.common.util.MinecraftLocaleSupplier;
import eakerzt.jiv.common.util.Translator;
import eakerzt.jiv.config.neoforge.ConfigNeoForge;
import eakerzt.jiv.config.gui.neoforge.ConfigGuiNeoForge;
import eakerzt.jiv.neoforge.config.ServerConfig;
import eakerzt.jiv.neoforge.events.PermanentEventSubscriptions;
import eakerzt.jiv.neoforge.network.NetworkHandler;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

@Mod(ModIds.JIV_ID)
public class JustItemsViewer {

	public JustItemsViewer(IEventBus modEventBus, Dist dist) {
		ConfigNeoForge.register(modEventBus, dist);
		ConfigGuiNeoForge.register(modEventBus, dist);
		Translator.setLocaleSupplier(new MinecraftLocaleSupplier());
		IEventBus eventBus = NeoForge.EVENT_BUS;
		PermanentEventSubscriptions subscriptions = new PermanentEventSubscriptions(eventBus, modEventBus);

		ModLoadingContext modLoadingContext = ModLoadingContext.get();
		IServerConfig serverConfig = ServerConfig.register(modLoadingContext);

		NetworkHandler networkHandler = new NetworkHandler("3", serverConfig);
		networkHandler.registerPacketHandlers(subscriptions);

		eventBus.addListener(false, OnDatapackSyncEvent.class, e -> e.sendRecipes(
			RecipeType.CRAFTING,
			RecipeType.STONECUTTING,
			RecipeType.SMELTING,
			RecipeType.SMOKING,
			RecipeType.BLASTING,
			RecipeType.CAMPFIRE_COOKING,
			RecipeType.SMITHING
		));

		JustItemsViewerClientSafeRunner clientSafeRunner = new JustItemsViewerClientSafeRunner(networkHandler, subscriptions);
		if (dist.isClient()) {
			clientSafeRunner.registerClient();
		}
	}
}
