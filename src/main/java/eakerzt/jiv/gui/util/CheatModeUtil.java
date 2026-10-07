package eakerzt.jiv.gui.util;

import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.config.IClientToggleState;
import eakerzt.jiv.common.network.IConnectionToServer;
import eakerzt.jiv.common.network.packets.PacketRequestCheatPermission;

public final class CheatModeUtil {
	private CheatModeUtil() {}

	public static void toggleCheatMode(IClientToggleState toggleState) {
		setCheatModeEnabled(toggleState, !toggleState.isCheatItemsEnabled());
	}

	public static void setCheatModeEnabled(IClientToggleState toggleState, boolean enabled) {
		toggleState.setCheatItemsEnabled(enabled);
		if (enabled) {
			IConnectionToServer serverConnection = Internal.getServerConnection();
			serverConnection.sendPacketToServer(PacketRequestCheatPermission.INSTANCE);
		}
	}
}
