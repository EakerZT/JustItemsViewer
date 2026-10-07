package eakerzt.jiv.neoforge.startup;

import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.network.IConnectionToServer;
import eakerzt.jiv.neoforge.events.PermanentEventSubscriptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.lang.ref.WeakReference;

/**
 * This class observes events and determines when it's the right time to start JIV.
 *
 * JIV needs to see {@link ClientPlayerNetworkEvent.LoggingIn} before it is ready to start. When
 * the connection can provide server recipe content, JIV also waits for {@link RecipesReceivedEvent}
 * so it does not briefly start with fallback client recipes.
 *
 * Connections to vanilla servers get an empty recipe event, which lets JIV continue with fallback recipes.
 * Datapack reloads can fire another recipe event after JIV has started. JIV restarts when that
 * event changes the recipe source between synced recipes and fallback client recipes.
 */
public class StartEventObserver implements ResourceManagerReloadListener {
	private static final Logger LOGGER = LogManager.getLogger();

	private enum State {
		LISTENING, JIV_STARTED
	}

	private final IConnectionToServer serverConnection;
	private final Runnable startRunnable;
	private final Runnable stopRunnable;
	private WeakReference<Connection> currentConnection = new WeakReference<>(null);
	private State state = State.LISTENING;
	private boolean observedLogin;
	private boolean observedRecipeSync;

	public StartEventObserver(IConnectionToServer serverConnection, Runnable startRunnable, Runnable stopRunnable) {
		this.serverConnection = serverConnection;
		this.startRunnable = startRunnable;
		this.stopRunnable = stopRunnable;
	}

	public void register(PermanentEventSubscriptions subscriptions) {
		subscriptions.register(EventPriority.LOWEST, ClientPlayerNetworkEvent.LoggingIn.class, this::onLoggingIn);
		subscriptions.register(EventPriority.LOWEST, RecipesReceivedEvent.class, this::onRecipesReceivedEvent);

		subscriptions.register(ClientPlayerNetworkEvent.LoggingOut.class, event -> {
			if (event.getPlayer() != null) {
				logReceivedEvent(event);
				transitionState(State.LISTENING);
			}
		});

		subscriptions.register(ScreenEvent.Init.Pre.class, event -> {
			if (this.state != State.JIV_STARTED) {
				Screen screen = event.getScreen();
				Minecraft minecraft = screen.getMinecraft();
				if (screen instanceof AbstractContainerScreen && minecraft != null && minecraft.player != null) {
					LOGGER.error("""
							A Screen is opening but JIV hasn't started yet.
							Normally, JIV is started after these events have fired: {}.
							Something has caused one or more of these events to fail, so JIV is starting very late.
							Missing events: {}""",
						getRequiredStartEventsString(),
						getMissingStartEventsString()
					);
					transitionState(State.LISTENING);
					transitionState(State.JIV_STARTED);
				}
			}
		});
	}

	private void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
		if (!observeConnectionEvent(event)) {
			return;
		}
		this.observedLogin = true;
		startIfReady();
	}

	private void onRecipesReceivedEvent(RecipesReceivedEvent event) {
		if (!observeConnectionEvent(event)) {
			return;
		}
		boolean hadSyncedRecipes = Internal.hasClientSyncedRecipes();
		RecipeMap recipeMap = event.getRecipeMap();
		boolean receivedSyncedRecipes = !event.getRecipeTypes().isEmpty();
		if (receivedSyncedRecipes) {
			Internal.setClientSyncedRecipes(recipeMap);
		} else if (hadSyncedRecipes) {
			Internal.clearClientRecipes();
		}

		this.observedRecipeSync = true;
		if (this.state == State.JIV_STARTED && (hadSyncedRecipes || receivedSyncedRecipes)) {
			restart();
		} else {
			startIfReady();
		}
	}

	private void startIfReady() {
		if (this.state != State.LISTENING || !this.observedLogin) {
			return;
		}
		if (shouldWaitForRecipes() && !this.observedRecipeSync) {
			return;
		}
		transitionState(State.JIV_STARTED);
	}

	private <T extends Event> boolean observeConnectionEvent(T event) {
		Connection observingConnection = this.currentConnection.get();
		Connection currentConnection = getCurrentConnection();
		if (currentConnection != observingConnection) {
			// Connection changed => any information we previously got is useless now
			clearObservedStartEvents();
			this.currentConnection = new WeakReference<>(currentConnection);
		}
		if (currentConnection == null) {
			// No connection => Disregard, this probably an event being fired on the integrated server thread
			LOGGER.debug("JIV StartEventObserver received {} too early, ignoring", event.getClass());
			return false;
		}
		logReceivedEvent(event);
		return true;
	}

	private boolean shouldWaitForRecipes() {
		return serverConnection.isJivOnServer() ||
			serverConnection.isSameModLoader();
	}

	private String getRequiredStartEventsString() {
		if (shouldWaitForRecipes()) {
			return "[%s, %s]".formatted(ClientPlayerNetworkEvent.LoggingIn.class.getName(), RecipesReceivedEvent.class.getName());
		}
		return "[%s]".formatted(ClientPlayerNetworkEvent.LoggingIn.class.getName());
	}

	private String getMissingStartEventsString() {
		StringBuilder missingEvents = new StringBuilder("[");
		if (!observedLogin) {
			missingEvents.append(ClientPlayerNetworkEvent.LoggingIn.class.getName());
		}
		if (shouldWaitForRecipes() && !observedRecipeSync) {
			if (missingEvents.length() > 1) {
				missingEvents.append(", ");
			}
			missingEvents.append(RecipesReceivedEvent.class.getName());
		}
		return missingEvents.append("]").toString();
	}

	private static <T extends Event> void logReceivedEvent(T event) {
		LOGGER.debug("JIV StartEventObserver received event: {}", event.getClass());
	}

	@Nullable
	private static Connection getCurrentConnection() {
		Minecraft minecraft = Minecraft.getInstance();
		ClientPacketListener packetListener = minecraft.getConnection();
		if (packetListener != null) {
			return packetListener.getConnection();
		} else if (minecraft.pendingConnection != null) {
			// Some events are fired very early in the connection process,
			// so packetListener may not be initialized.
			// Instead, we grab it from pendingConnection (singleplayer) or...
			return minecraft.pendingConnection;
		} else if (minecraft.screen instanceof ConnectScreen connectScreen) {
			//...the connect screen (multiplayer)
			return connectScreen.connection;
		} else {
			return null;
		}
	}

	@Override
	public void onResourceManagerReload(ResourceManager pResourceManager) {
		LOGGER.debug("JIV StartEventObserver detected resource manager reload.");
		restart();
	}

	public void restart() {
		if (this.state != State.JIV_STARTED) {
			return;
		}
		transitionState(State.LISTENING);
		transitionState(State.JIV_STARTED);
	}

	private void transitionState(State newState) {
		LOGGER.debug("JIV StartEventObserver transitioning state from {} to {}", this.state, newState);

		switch (newState) {
			case LISTENING -> {
				if (this.state == State.JIV_STARTED) {
					this.stopRunnable.run();
				}
			}
			case JIV_STARTED -> {
				if (this.state != State.LISTENING) {
					throw new IllegalStateException("Attempted Illegal state transition from " + this.state + " to " + newState);
				}
				this.startRunnable.run();
			}
		}

		this.state = newState;
		clearObservedStartEvents();
	}

	private void clearObservedStartEvents() {
		this.observedLogin = false;
		this.observedRecipeSync = false;
	}
}
