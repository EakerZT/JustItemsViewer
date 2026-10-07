package eakerzt.jiv.neoforge.chat;

import com.mojang.brigadier.arguments.StringArgumentType;
import eakerzt.jiv.common.chat.JivChatItemLinkRecipeLookup;
import eakerzt.jiv.common.chat.JivChatItemLinks;
import eakerzt.jiv.neoforge.events.PermanentEventSubscriptions;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

public final class JivInternalShowCommand {
	private JivInternalShowCommand() {
	}

	public static void register(PermanentEventSubscriptions subscriptions) {
		subscriptions.register(RegisterClientCommandsEvent.class, JivInternalShowCommand::onRegisterClientCommands);
	}

	private static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
		event.getDispatcher().register(
			Commands.literal(JivChatItemLinks.SHOW_RECIPE_COMMAND)
				.then(Commands.argument(JivChatItemLinks.LINK_ARGUMENT, StringArgumentType.greedyString())
					.executes(context -> {
						String link = StringArgumentType.getString(context, JivChatItemLinks.LINK_ARGUMENT);
						return JivChatItemLinkRecipeLookup.executeShowRecipeCommand(link);
					})
				)
		);
	}
}
