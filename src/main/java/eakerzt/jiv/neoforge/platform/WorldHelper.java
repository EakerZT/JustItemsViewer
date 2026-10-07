package eakerzt.jiv.neoforge.platform;

import eakerzt.jiv.common.platform.IPlatformWorldHelper;
import net.minecraft.server.MinecraftServer;

import java.util.Optional;

public class WorldHelper implements IPlatformWorldHelper {
	@Override
	public Optional<String> getLevelId(MinecraftServer server) {
		return Optional.of(server.storageSource.getLevelId());
	}
}
