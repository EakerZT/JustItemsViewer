package eakerzt.jiv.config.gui;

import eakerzt.jiv.config.gui.api.IConfigScreenValue;
import eakerzt.jiv.config.gui.info.ServerConfigAccess;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Internal access policy for native values, retained through identity-preserving GUI wrappers.
 */
public interface ConfigValueAccess {
	boolean isEditable();

	/**
	 * Shared live status provider for values with the same server access policy.
	 */
	default Optional<Supplier<ServerConfigAccess>> getServerAccess() {
		return Optional.empty();
	}

	static boolean isEditable(IConfigScreenValue<?> value) {
		if (value.getIdentityKey() instanceof ConfigValueAccess access) {
			return access.isEditable();
		}
		return true;
	}
}
