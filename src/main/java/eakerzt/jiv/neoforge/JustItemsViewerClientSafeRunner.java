package eakerzt.jiv.neoforge;

import eakerzt.jiv.neoforge.events.PermanentEventSubscriptions;
import eakerzt.jiv.neoforge.network.NetworkHandler;

public class JustItemsViewerClientSafeRunner {
	private final NetworkHandler networkHandler;
	private final PermanentEventSubscriptions subscriptions;

	public JustItemsViewerClientSafeRunner(
		NetworkHandler networkHandler,
		PermanentEventSubscriptions subscriptions
	) {
		this.networkHandler = networkHandler;
		this.subscriptions = subscriptions;
	}

	public void registerClient() {
		JustItemsViewerClient jivClient = new JustItemsViewerClient(networkHandler, subscriptions);
		jivClient.register();
	}
}
