package eakerzt.jiv.neoforge;

import eakerzt.jiv.api.IModPlugin;
import eakerzt.jiv.api.constants.ModIds;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.gui.JivGuiColors;
import eakerzt.jiv.common.gui.RecipeSlotOptionsTooltipComponent;
import eakerzt.jiv.common.gui.IngredientTooltipComponent;
import eakerzt.jiv.common.gui.IngredientsTooltipComponent;
import eakerzt.jiv.common.network.IConnectionToServer;
import eakerzt.jiv.gui.config.InternalKeyMappings;
import eakerzt.jiv.gui.overlay.bookmarks.PreviewTooltipComponent;
import eakerzt.jiv.gui.recipes.InteractiveIngredientGridTooltipComponent;
import eakerzt.jiv.library.gui.ingredients.TagContentTooltipComponent;
import eakerzt.jiv.library.config.JivConfigData;
import eakerzt.jiv.library.config.JivConfigRegistration;
import eakerzt.jiv.library.plugins.vanilla.crafting.JivShapedRecipe;
import eakerzt.jiv.library.plugins.vanilla.cooking.JivSmeltingRecipe;
import eakerzt.jiv.library.recipes.RecipeSerializers;
import eakerzt.jiv.library.startup.JivStarter;
import eakerzt.jiv.library.startup.StartData;
import eakerzt.jiv.neoforge.chat.JivChatEventHandler;
import eakerzt.jiv.neoforge.chat.JivChatTooltipEventHandler;
import eakerzt.jiv.neoforge.chat.JivInternalShowCommand;
import eakerzt.jiv.neoforge.events.PermanentEventSubscriptions;
import eakerzt.jiv.neoforge.network.NetworkHandler;
import eakerzt.jiv.neoforge.plugins.neoforge.NeoForgeGuiPlugin;
import eakerzt.jiv.neoforge.startup.ForgePluginFinder;
import eakerzt.jiv.neoforge.startup.StartEventObserver;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppingEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class JustItemsViewerClient {
	private final PermanentEventSubscriptions subscriptions;
	private final JivStarter jivStarter;

	public JustItemsViewerClient(
		NetworkHandler networkHandler,
		PermanentEventSubscriptions subscriptions
	) {
		this.subscriptions = subscriptions;
		JivConfigData configData = JivConfigRegistration.register();
		IConnectionToServer serverConnection = networkHandler.getConnectionToServer();

		List<IModPlugin> plugins = ForgePluginFinder.getModPlugins();
		StartData startData = new StartData(
			plugins,
			serverConnection,
			configData
		);

		this.jivStarter = new JivStarter(startData);

		StartEventObserver startEventObserver = new StartEventObserver(serverConnection, this.jivStarter::start, this.jivStarter::stop);
		Internal.setRestartJivRunnable(startEventObserver::restart);
		startEventObserver.register(subscriptions);
	}

	public void register() {
		subscriptions.register(AddClientReloadListenersEvent.class, this::onRegisterReloadListenerEvent);
		subscriptions.register(RegisterClientTooltipComponentFactoriesEvent.class, this::onRegisterClientTooltipEvent);
		subscriptions.register(ClientStoppingEvent.class, e -> onClientStopping());
		subscriptions.register(RegisterKeyMappingsEvent.class, e -> {
			InternalKeyMappings keyMappings = new InternalKeyMappings(e::register, id -> {
				KeyMapping.Category category = new KeyMapping.Category(id);
				e.registerCategory(category);
				return category;
			});
			Internal.setKeyMappings(keyMappings);
		});

		JivChatEventHandler.register(subscriptions);
		JivChatTooltipEventHandler.register(subscriptions);
		JivInternalShowCommand.register(subscriptions);

		IEventBus modEventBus = subscriptions.getModEventBus();
		DeferredRegister<RecipeSerializer<?>> deferredRegister = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, ModIds.JIV_ID);
		deferredRegister.register(modEventBus);

		Supplier<RecipeSerializer<? extends CraftingRecipe>> jivShaped = deferredRegister.register("jiv_shaped", () -> JivShapedRecipe.SERIALIZER);
		Supplier<RecipeSerializer<? extends SmeltingRecipe>> jivSmelting = deferredRegister.register("jiv_smelting", () -> JivSmeltingRecipe.SERIALIZER);
		RecipeSerializers.register(jivShaped, jivSmelting);
	}

	private void onClientStopping() {
		jivStarter.stop();
		Internal.onClientStopping();
	}

	private void onRegisterReloadListenerEvent(AddClientReloadListenersEvent event) {
		event.addListener(Identifier.fromNamespaceAndPath(ModIds.JIV_ID, "jiv_client"), createReloadListener());
	}

	private void onRegisterClientTooltipEvent(RegisterClientTooltipComponentFactoriesEvent event) {
		event.register(IngredientTooltipComponent.class, Function.identity());
		event.register(IngredientsTooltipComponent.class, Function.identity());
		event.register(PreviewTooltipComponent.class, Function.identity());
		event.register(RecipeSlotOptionsTooltipComponent.class, Function.identity());
		event.register(TagContentTooltipComponent.class, Function.identity());
		event.register(InteractiveIngredientGridTooltipComponent.class, Function.identity());
	}

	private ResourceManagerReloadListener createReloadListener() {
		return (ResourceManager resourceManager) -> {
			JivGuiColors.onResourceManagerReload(resourceManager);
			NeoForgeGuiPlugin.getResourceReloadHandler()
				.ifPresent(r -> r.onResourceManagerReload(resourceManager));
		};
	}

}
