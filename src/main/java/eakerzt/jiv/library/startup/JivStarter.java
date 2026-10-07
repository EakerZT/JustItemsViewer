package eakerzt.jiv.library.startup;

import com.google.common.collect.ImmutableSetMultimap;
import eakerzt.jiv.api.IModPlugin;
import eakerzt.jiv.api.helpers.IColorHelper;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferManager;
import eakerzt.jiv.api.runtime.IScreenHelper;
import eakerzt.jiv.api.search.ISearchStorageBuilderFactory;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.config.ClientConfigs;
import eakerzt.jiv.common.network.ClientConnectionHelper;
import eakerzt.jiv.common.network.IConnectionToServer;
import eakerzt.jiv.common.network.packets.PacketRecipeTransferResult;
import eakerzt.jiv.common.platform.Services;
import eakerzt.jiv.common.recipes.VanillaClientRecipeLoader;
import eakerzt.jiv.common.util.ChatUtil;
import eakerzt.jiv.common.util.ErrorUtil;
import eakerzt.jiv.common.util.LoggedTimer;
import eakerzt.jiv.common.util.RegistryUtil;
import eakerzt.jiv.common.util.Translator;
import eakerzt.jiv.library.color.ColorHelper;
import eakerzt.jiv.library.config.ColorNameConfig;
import eakerzt.jiv.library.config.EditModeConfig;
import eakerzt.jiv.library.config.JivConfigData;
import eakerzt.jiv.library.config.ModIdFormatConfig;
import eakerzt.jiv.library.config.RecipeCategorySortingConfig;
import eakerzt.jiv.library.focus.FocusFactory;
import eakerzt.jiv.library.helpers.CodecHelper;
import eakerzt.jiv.library.ingredients.IngredientManager;
import eakerzt.jiv.library.ingredients.subtypes.SubtypeManager;
import eakerzt.jiv.library.load.PluginCaller;
import eakerzt.jiv.library.load.PluginHelper;
import eakerzt.jiv.library.load.PluginLoader;
import eakerzt.jiv.library.load.registration.RuntimeRegistration;
import eakerzt.jiv.library.plugins.jiv.JivInternalPlugin;
import eakerzt.jiv.library.plugins.vanilla.VanillaPlugin;
import eakerzt.jiv.library.recipes.RecipeManager;
import eakerzt.jiv.library.runtime.JivHelpers;
import eakerzt.jiv.library.runtime.JivRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class JivStarter {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String VANILLA_SERVER_BRAND = "vanilla";

	private final StartData data;
	private final List<IModPlugin> plugins;
	private final VanillaPlugin vanillaPlugin;
	private final ModIdFormatConfig modIdFormatConfig;
	private final ColorNameConfig colorNameConfig;
	private final RecipeCategorySortingConfig recipeCategorySortingConfig;
	private final ClientConfigs jivClientConfigs;
	private final List<IStopCallback> stopCallbacks = new ArrayList<>();
	private boolean running = false;

	public JivStarter(StartData data) {
		ErrorUtil.checkNotEmpty(data.plugins(), "plugins");
		this.data = data;
		this.plugins = data.plugins();
		PluginHelper.removePluginsWithCrashingUids(plugins);
		this.vanillaPlugin = PluginHelper.getPluginWithClass(VanillaPlugin.class, plugins)
			.orElseThrow(() -> new IllegalStateException("vanilla plugin not found"));
		JivInternalPlugin jivInternalPlugin = PluginHelper.getPluginWithClass(JivInternalPlugin.class, plugins)
			.orElse(null);
		PluginHelper.sortPlugins(plugins, vanillaPlugin, jivInternalPlugin);

		JivConfigData configData = data.configData();
		this.jivClientConfigs = configData.clientConfigs();
		this.modIdFormatConfig = configData.modIdFormatConfig();
		this.colorNameConfig = configData.colorNameConfig();
		this.recipeCategorySortingConfig = new RecipeCategorySortingConfig(jivClientConfigs);

	}

	public void start() {
		if (running) {
			LOGGER.error("Failed to start JIV, it is already running.");
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		ClientLevel level = minecraft.level;
		if (level == null) {
			LOGGER.error("Failed to start JIV, there is no Minecraft client level.");
			return;
		}
		RegistryAccess registryAccess = level.registryAccess();
		RegistryUtil.setRegistryAccess(registryAccess);
		ContextMap contextMap = SlotDisplayContext.fromLevel(level);

		if (!Internal.hasClientRecipes()) {
			RecipeMap vanillaRecipes = VanillaClientRecipeLoader.getVanillaRecipes(registryAccess);
			if (!vanillaRecipes.values().isEmpty()) {
				Internal.setClientFallbackRecipes(vanillaRecipes);
			}
		}

		LoggedTimer totalTime = new LoggedTimer();
		totalTime.start("Starting JIV");

		PluginCaller.callOnPlugins("Configuring JIV", plugins, p -> p.configureJiv(new PluginAwareJivFeatures(Internal.getJivFeatures(), p)));

		IColorHelper colorHelper = new ColorHelper(colorNameConfig);
		SubtypeManager subtypeManager = PluginLoader.registerSubtypes(data);
		IngredientManager ingredientManager = PluginLoader.registerIngredients(
			data,
			subtypeManager,
			colorHelper,
			contextMap
		);
		stopCallbacks.add(ingredientManager::onRuntimeStopped);

		FocusFactory focusFactory = new FocusFactory(ingredientManager);
		CodecHelper codecHelper = new CodecHelper(ingredientManager, focusFactory);

		Path configDir = Services.PLATFORM.getConfigHelper().createJivConfigDir();
		EditModeConfig.FileSerializer editModeSerializer = new EditModeConfig.FileSerializer(
			configDir.resolve("blacklist.json"),
			registryAccess,
			codecHelper
		);
		EditModeConfig editModeConfig = new EditModeConfig(editModeSerializer, ingredientManager);

		ImmutableSetMultimap<String, String> modAliases = PluginLoader.registerModAliases(data);

		JivHelpers jivHelpers = PluginLoader.createJivHelpers(
			modAliases,
			modIdFormatConfig,
			colorHelper,
			editModeConfig,
			focusFactory,
			codecHelper,
			ingredientManager,
			subtypeManager,
			contextMap
		);
		stopCallbacks.add(jivHelpers::onRuntimeStopped);

		ISearchStorageBuilderFactory searchStorageBuilderFactory = PluginLoader.createSearchStorageFactory(plugins);

		RecipeManager recipeManager = PluginLoader.createRecipeManager(
			plugins,
			vanillaPlugin,
			recipeCategorySortingConfig,
			jivHelpers,
			ingredientManager,
			contextMap
		);
		stopCallbacks.add(recipeManager::onRuntimeStopped);
		IRecipeTransferManager recipeTransferManager = PluginLoader.createRecipeTransferManager(
			vanillaPlugin,
			plugins,
			jivHelpers,
			data.serverConnection()
		);

		LoggedTimer timer = new LoggedTimer();
		timer.start("Building runtime");
		IScreenHelper screenHelper = PluginLoader.createGuiScreenHelper(plugins, jivHelpers, ingredientManager);

		RuntimeRegistration runtimeRegistration = new RuntimeRegistration(
			recipeManager,
			jivHelpers,
			editModeConfig,
			ingredientManager,
			recipeTransferManager,
			screenHelper,
			searchStorageBuilderFactory
		);
		PluginCaller.callOnPlugins("Registering Runtime", plugins, p -> p.registerRuntime(runtimeRegistration));

		JivRuntime jivRuntime = new JivRuntime(
			recipeManager,
			ingredientManager,
			Internal.getKeyMappings(),
			jivHelpers,
			screenHelper,
			recipeTransferManager,
			editModeConfig,
			runtimeRegistration.getIngredientListOverlay(),
			runtimeRegistration.getBookmarkOverlay(),
			runtimeRegistration.getBookmarkManager(),
			runtimeRegistration.getRecipesGui(),
			runtimeRegistration.getIngredientFilter()
		);
		timer.stop();

		PluginCaller.callOnPlugins("Sending Runtime", plugins, p -> p.onRuntimeAvailable(jivRuntime));
		Internal.setRuntime(jivRuntime);
		this.running = true;

		totalTime.stop();

		verifyClientRecipes(minecraft);
	}

	private void verifyClientRecipes(Minecraft minecraft) {
		IConnectionToServer serverConnection = data.serverConnection();
		RecipeMap clientRecipes = Internal.getClientSyncedRecipes();
		boolean showWarning = jivClientConfigs.getClientConfig().recipeSyncWarningEnabled().get();

		if (Internal.hasClientSyncedRecipes() && clientRecipes.values().isEmpty()) {
			String key = "jiv.message.server.recipe.sync.error";
			if (showWarning) {
				writeChatMessage(minecraft, Component.translatable(key).withStyle(ChatFormatting.RED));
			}
			LOGGER.error(Translator.translateToLocal(key));
		} else if (Internal.hasClientFallbackRecipes()) {
			if (!serverConnection.isJivOnServer() &&
				serverConnection.isSameModLoader()
			) {
				String key = "jiv.message.server.recipe.sync.jiv.missing";
				String serverBrand = ClientConnectionHelper.getServerBrand();
				if (showWarning) {
					writeChatMessage(minecraft, Component.translatable(key, serverBrand).withStyle(ChatFormatting.RED));
				}
				LOGGER.warn(Translator.translateToLocalFormatted(key, serverBrand));
			} else if (ClientConnectionHelper.hasServerBrand(VANILLA_SERVER_BRAND)) {
				String key = "jiv.message.server.recipe.sync.vanilla";
				if (showWarning) {
					writeChatMessage(minecraft, Component.translatable(key).withStyle(ChatFormatting.YELLOW));
				}
				LOGGER.warn(Translator.translateToLocal(key));
			} else {
				String key = "jiv.message.server.recipe.sync.unavailable";
				String serverBrand = ClientConnectionHelper.getServerBrand();
				if (showWarning) {
					writeChatMessage(minecraft, Component.translatable(key, serverBrand).withStyle(ChatFormatting.RED));
				}
				LOGGER.warn(Translator.translateToLocalFormatted(key, serverBrand));
			}
		}
	}

	private static void writeChatMessage(Minecraft minecraft, Component component) {
		LocalPlayer player = minecraft.player;
		if (player != null) {
			ChatUtil.writeChatMessage(player, component);
		}
	}

	public void stop() {
		if (!running) {
			return;
		}
		this.running = false;

		LOGGER.info("Stopping JIV");

		List<IModPlugin> plugins = data.plugins();
		PluginCaller.callOnPlugins("Sending Runtime Unavailable", plugins, IModPlugin::onRuntimeUnavailable);
		PacketRecipeTransferResult.clearPendingRecipeTransfers();

		Internal.onRuntimeStopped();

		for (IStopCallback stopCallback : stopCallbacks) {
			stopCallback.onRuntimeStopped();
		}
		stopCallbacks.clear();

		RegistryUtil.setRegistryAccess(null);
	}
}
