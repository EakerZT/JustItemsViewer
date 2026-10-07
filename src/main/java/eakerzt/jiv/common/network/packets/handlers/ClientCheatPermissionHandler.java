package eakerzt.jiv.common.network.packets.handlers;

import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.config.IClientToggleState;
import eakerzt.jiv.common.network.ClientPacketContext;
import eakerzt.jiv.common.util.ChatUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;

import java.util.List;

/**
 * Client-side-only functions related to cheat permissions
 */
public class ClientCheatPermissionHandler {
	public static void handleHasCheatPermission(ClientPacketContext context, boolean hasPermission, List<String> allowedCheatingMethods) {
		if (!hasPermission) {
			LocalPlayer player = context.player();
			ChatUtil.writeChatMessage(player, "jiv.chat.error.no.cheat.permission.1", ChatFormatting.RED);

			if (allowedCheatingMethods.isEmpty()) {
				ChatUtil.writeChatMessage(player, "jiv.chat.error.no.cheat.permission.disabled", ChatFormatting.RED);
			} else {
				ChatUtil.writeChatMessage(player, "jiv.chat.error.no.cheat.permission.enabled", ChatFormatting.RED);
				for (String allowedCheatingMethod : allowedCheatingMethods) {
					ChatUtil.writeChatMessage(player, allowedCheatingMethod, ChatFormatting.RED);
				}
			}

			IClientToggleState toggleState = Internal.getClientToggleState();
			toggleState.setCheatItemsEnabled(false);
			player.closeContainer();
		}
	}
}
