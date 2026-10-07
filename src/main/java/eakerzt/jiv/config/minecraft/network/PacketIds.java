package eakerzt.jiv.config.minecraft.network;

import net.minecraft.resources.Identifier;

final class PacketIds {
	private PacketIds() {}

	static Identifier create(String path) {
		return Identifier.fromNamespaceAndPath("jiv", "config/" + path);
	}
}
