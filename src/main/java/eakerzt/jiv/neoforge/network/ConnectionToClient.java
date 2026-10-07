package eakerzt.jiv.neoforge.network;

import eakerzt.jiv.common.network.IConnectionToClient;
import eakerzt.jiv.common.network.packets.PlayToClientPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class ConnectionToClient implements IConnectionToClient {
	@Override
	public <T extends PlayToClientPacket<T>> void sendPacketToClient(T packet, ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, packet);
	}
}
