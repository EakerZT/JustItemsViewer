package eakerzt.jiv.common.config.legacy;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

public final class LegacyConfigPaths {
	private LegacyConfigPaths() {}

	public static List<Path> get(Path jivConfigDirectory, @Nullable UUID profileId, String fileName) {
		jivConfigDirectory = Objects.requireNonNull(jivConfigDirectory).toAbsolutePath().normalize();
		Objects.requireNonNull(fileName);

		Path legacyFile = jivConfigDirectory.resolve(fileName);
		if (profileId == null) {
			return List.of(legacyFile);
		}

		Path legacyProfileFile = jivConfigDirectory.resolve("players").resolve(profileId.toString()).resolve(fileName);
		return List.of(legacyProfileFile, legacyFile);
	}
}
