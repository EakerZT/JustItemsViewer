package eakerzt.jiv.common.network;

import eakerzt.jiv.common.network.packets.PlayToClientPacket;
import net.minecraft.server.level.ServerPlayer;

public interface IConnectionToClient {
	<T extends PlayToClientPacket<T>> void sendPacketToClient(T packet, ServerPlayer player);
}
