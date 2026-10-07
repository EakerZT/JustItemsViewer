package eakerzt.jiv.common.network.packets;

import eakerzt.jiv.common.network.ClientPacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public abstract class PlayToClientPacket<T extends PlayToClientPacket<T>> implements CustomPacketPayload {
	@Override
	public abstract Type<T> type();
	public abstract StreamCodec<RegistryFriendlyByteBuf, T> streamCodec();
	public abstract void process(ClientPacketContext context);
}
