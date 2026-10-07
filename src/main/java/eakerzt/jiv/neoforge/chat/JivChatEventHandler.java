package eakerzt.jiv.neoforge.chat;

import eakerzt.jiv.common.chat.JivChatItemLinks;
import eakerzt.jiv.neoforge.events.PermanentEventSubscriptions;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;

import java.util.Optional;

public final class JivChatEventHandler {
	private JivChatEventHandler() {
	}

	public static void register(PermanentEventSubscriptions subscriptions) {
		subscriptions.register(ClientChatReceivedEvent.class, JivChatEventHandler::onChatMessageReceived);
	}

	private static void onChatMessageReceived(ClientChatReceivedEvent event) {
		Optional<Component> parsedMessage = JivChatItemLinks.parseChatMessage(event.getMessage());
		if (parsedMessage.isEmpty()) {
			return;
		}

		Component parsed = parsedMessage.get();
		event.setMessage(parsed);
	}
}
