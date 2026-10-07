package eakerzt.jiv.config.gui.screenlist;

import java.net.URI;

import org.jspecify.annotations.Nullable;

/**
 * Icon metadata for the mod that owns a config screen.
 */
public record ConfigScreenOwnerMetadata(
	@Nullable URI iconPath
) {
	public ConfigScreenOwnerMetadata() {
		this(null);
	}
}
