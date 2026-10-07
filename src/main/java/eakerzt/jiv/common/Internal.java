package eakerzt.jiv.common;

import com.google.common.base.Preconditions;
import eakerzt.jiv.api.runtime.IJivRuntime;
import eakerzt.jiv.api.runtime.IRecipesGui;
import eakerzt.jiv.common.config.ClientToggleState;
import eakerzt.jiv.common.config.IClientToggleState;
import eakerzt.jiv.common.config.IClientConfigs;
import eakerzt.jiv.common.gui.textures.Textures;
import eakerzt.jiv.common.input.IInternalKeyMappings;
import eakerzt.jiv.common.network.IConnectionToServer;
import eakerzt.jiv.common.util.DelayedExecutor;
import eakerzt.jiv.common.util.IDelayedExecutor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.Connection;
import net.minecraft.world.item.crafting.RecipeMap;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.Optional;

/**
 * For JIV internal use only, these are normally accessed from the API.
 */
public final class Internal {
	@Nullable
	private static Textures textures;
	@Nullable
	private static IConnectionToServer serverConnection;
	@Nullable
	private static IInternalKeyMappings keyMappings;
	@Nullable
	private static IClientToggleState toggleState;
	@Nullable
	private static IClientConfigs jivClientConfigs;
	@Nullable
	private static IJivRuntime jivRuntime;
	/**
	 * Null means that no recipe source has been selected for the current connection yet.
	 * A present value may have an empty recipe map when the server explicitly synchronizes zero recipes.
	 */
	@Nullable
	private static Runnable restartJivRunnable;
	@Nullable
	private static ClientRecipes clientRecipes = null;
	private static final JivFeatures jivFeatures = new JivFeatures();
	private static final DelayedExecutor delayedExecutor = new DelayedExecutor(Duration.ofSeconds(10));

	private Internal() {

	}

	public static Textures getTextures() {
		if (textures == null) {
			Minecraft minecraft = Minecraft.getInstance();
			TextureAtlas guiAtlas = minecraft.getAtlasManager().getAtlasOrThrow(AtlasIds.GUI);
			textures = new Textures(guiAtlas);
		}
		return textures;
	}

	public static IConnectionToServer getServerConnection() {
		Preconditions.checkState(serverConnection != null, "Server Connection has not been created yet.");
		return serverConnection;
	}

	public static void setServerConnection(IConnectionToServer serverConnection) {
		Internal.serverConnection = serverConnection;
	}

	public static IInternalKeyMappings getKeyMappings() {
		Preconditions.checkState(keyMappings != null, "Key Mappings have not been created yet.");
		return keyMappings;
	}

	public static void setKeyMappings(IInternalKeyMappings keyMappings) {
		Internal.keyMappings = keyMappings;
	}

	public static IClientToggleState getClientToggleState() {
		if (toggleState == null) {
			toggleState = new ClientToggleState();
		}
		return toggleState;
	}

	public static IClientConfigs getClientConfigs() {
		Preconditions.checkState(jivClientConfigs != null, "Jiv Client Configs have not been created yet.");
		return jivClientConfigs;
	}

	public static Optional<IClientConfigs> getOptionalClientConfigs() {
		return Optional.ofNullable(jivClientConfigs);
	}

	public static void setClientConfigs(IClientConfigs jivClientConfigs) {
		Internal.jivClientConfigs = jivClientConfigs;
	}

	public static void registerRuntimeListenerRemoval(Runnable listenerRemoval) {
		getClientConfigs().registerRuntimeListenerRemoval(listenerRemoval);
	}

	public static JivFeatures getJivFeatures() {
		return jivFeatures;
	}

	public static IDelayedExecutor getDelayedExecutor() {
		return delayedExecutor;
	}

	public static void setRuntime(IJivRuntime jivRuntime) {
		Internal.jivRuntime = jivRuntime;
	}

	public static IJivRuntime getJivRuntime() {
		Preconditions.checkState(jivRuntime != null, "Jiv Runtime has not been created yet.");

		return jivRuntime;
	}

	public static Optional<IJivRuntime> getOptionalJivRuntime() {
		return Optional.ofNullable(jivRuntime);
	}

	public static void setRestartJivRunnable(Runnable restartJivRunnable) {
		Internal.restartJivRunnable = restartJivRunnable;
	}

	public static void restartJiv() {
		Preconditions.checkState(restartJivRunnable != null, "JIV restart handler has not been created yet.");
		restartJivRunnable.run();
	}

	@Nullable
	private static String getRemoteConnectionId() {
		ClientPacketListener clientPacketListener = Minecraft.getInstance().getConnection();
		if (clientPacketListener != null) {
			Connection connection = clientPacketListener.getConnection();
			if (connection.isConnected()) {
				return connection.getLoggableAddress(true);
			}
		}
		return null;
	}

	public static void setClientSyncedRecipes(RecipeMap clientSyncedRecipes) {
		setClientRecipes(clientSyncedRecipes, true);
	}

	public static void setClientFallbackRecipes(RecipeMap clientRecipes) {
		setClientRecipes(clientRecipes, false);
	}

	public static void clearClientRecipes() {
		clientRecipes = null;
	}

	private static void setClientRecipes(RecipeMap recipes, boolean syncedWithServer) {
		var connectionId = getRemoteConnectionId();
		if (connectionId != null) {
			Internal.clientRecipes = new ClientRecipes(recipes, connectionId, syncedWithServer);
		}
	}

	public static RecipeMap getClientSyncedRecipes() {
		ClientRecipes clientRecipes = getClientRecipes();
		if (clientRecipes != null) {
			return clientRecipes.recipes();
		}
		return RecipeMap.EMPTY;
	}

	public static boolean hasClientSyncedRecipes() {
		ClientRecipes clientRecipes = getClientRecipes();
		return clientRecipes != null && clientRecipes.syncedWithServer();
	}

	public static boolean hasClientFallbackRecipes() {
		ClientRecipes clientRecipes = getClientRecipes();
		return clientRecipes != null && !clientRecipes.syncedWithServer();
	}

	public static boolean hasClientRecipes() {
		return getClientRecipes() != null;
	}

	@Nullable
	private static ClientRecipes getClientRecipes() {
		if (clientRecipes != null) {
			var connectionId = getRemoteConnectionId();
			if (clientRecipes.connectionId().equals(connectionId)) {
				return clientRecipes;
			}
		}
		return null;
	}

	public static void onRuntimeStopped() {
		closeRecipeGuiIfOpen();

		if (clientRecipes != null) {
			var connectionId = getRemoteConnectionId();
			if (!clientRecipes.connectionId().equals(connectionId)) {
				clientRecipes = null;
			}
		}
		if (jivClientConfigs != null) {
			jivClientConfigs.onRuntimeStopped();
		}
		if (toggleState != null) {
			toggleState.clearListeners();
		}
		if (serverConnection != null) {
			serverConnection.onRuntimeStopped();
		}
		if (jivRuntime != null) {
			jivRuntime = null;
		}
	}

	private static void closeRecipeGuiIfOpen() {
		IJivRuntime jivRuntime = Internal.jivRuntime;
		if (jivRuntime == null) {
			return;
		}

		IRecipesGui recipesGui = jivRuntime.getRecipesGui();
		if (recipesGui instanceof Screen recipesScreen) {
			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft.screen == recipesScreen) {
				recipesScreen.onClose();
			}
		}
	}

	public static void onClientStopping() {
		onRuntimeStopped();
		restartJivRunnable = null;
		delayedExecutor.shutdown();
	}

	private record ClientRecipes(RecipeMap recipes, String connectionId, boolean syncedWithServer) {

	}
}
