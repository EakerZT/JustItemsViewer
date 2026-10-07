package eakerzt.jiv.common.network;

import eakerzt.jiv.common.config.IServerConfig;
import net.minecraft.server.level.ServerPlayer;

public record ServerPacketContext(ServerPlayer player,
	IServerConfig serverConfig,
	IConnectionToClient connection
) {
}
