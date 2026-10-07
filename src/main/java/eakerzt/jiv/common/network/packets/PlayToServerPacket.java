package eakerzt.jiv.common.network.packets;

import eakerzt.jiv.common.network.ServerPacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public abstract class PlayToServerPacket<T extends PlayToServerPacket<T>> implements CustomPacketPayload {
	@Override
	public abstract Type<T> type();
	public abstract StreamCodec<RegistryFriendlyByteBuf, T> streamCodec();
	public abstract void process(ServerPacketContext context);
}
