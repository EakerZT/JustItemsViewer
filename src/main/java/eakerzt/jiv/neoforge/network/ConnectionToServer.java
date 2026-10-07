package eakerzt.jiv.neoforge.network;

import eakerzt.jiv.common.network.ClientConnectionHelper;
import eakerzt.jiv.common.network.IConnectionToServer;
import eakerzt.jiv.common.network.packets.PacketDeletePlayerItem;
import eakerzt.jiv.common.network.packets.PlayToServerPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public final class ConnectionToServer implements IConnectionToServer {
	private static final String NEOFORGE_SERVER_BRAND = "neoforge";

	private static JivServerState jivOnServerCacheValue = JivServerState.UNKNOWN;

	private enum JivServerState {
		UNKNOWN, ON_SERVER, NOT_ON_SERVER
	}

	@Override
	public boolean isJivOnServer() {
		return canSendPacket(PacketDeletePlayerItem.TYPE);
	}

	@Override
	public boolean isSameModLoader() {
		return ClientConnectionHelper.hasServerBrand(NEOFORGE_SERVER_BRAND);
	}

	@Override
	public boolean canSendPacket(CustomPacketPayload.Type<?> packetType) {
		Minecraft minecraft = Minecraft.getInstance();
		ClientPacketListener clientPacketListener = minecraft.getConnection();
		if (clientPacketListener == null || !clientPacketListener.getConnection().isConnected()) {
			jivOnServerCacheValue = JivServerState.UNKNOWN;
			return false;
		}
		if (jivOnServerCacheValue == JivServerState.UNKNOWN) {
			boolean onServer = clientPacketListener.hasChannel(PacketDeletePlayerItem.TYPE);
			jivOnServerCacheValue = JivServerState.NOT_ON_SERVER;
			if (onServer) {
				jivOnServerCacheValue = JivServerState.ON_SERVER;
			}
		}
		return jivOnServerCacheValue == JivServerState.ON_SERVER &&
			clientPacketListener.hasChannel(packetType);
	}

	@Override
	public <T extends PlayToServerPacket<T>> void sendPacketToServer(T packet) {
		Minecraft minecraft = Minecraft.getInstance();
		ClientPacketListener netHandler = minecraft.getConnection();
		if (netHandler != null && canSendPacket(packet.type())) {
			ClientPacketDistributor.sendToServer(packet);
		}
	}

	@Override
	public void onRuntimeStopped() {
		jivOnServerCacheValue = JivServerState.UNKNOWN;
	}
}
