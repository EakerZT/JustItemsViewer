package eakerzt.jiv.library.load;

import eakerzt.jiv.api.IModPlugin;
import eakerzt.jiv.library.plugins.jiv.JivInternalPlugin;
import eakerzt.jiv.library.plugins.vanilla.VanillaPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class PluginHelper {
	private static final Logger LOGGER = LogManager.getLogger();

	public static void removePluginsWithCrashingUids(List<IModPlugin> plugins) {
		plugins.removeIf(plugin -> {
			try {
				plugin.getPluginUid();
				return false;
			} catch (RuntimeException | LinkageError e) {
				LOGGER.error("Failed to get plugin UID, removing plugin from JIV: {}", plugin.getClass(), e);
				return true;
			}
		});
	}

	public static void sortPlugins(List<IModPlugin> plugins, VanillaPlugin vanillaPlugin, @Nullable JivInternalPlugin jivInternalPlugin) {
		plugins.remove(vanillaPlugin);
		plugins.addFirst(vanillaPlugin);

		if (jivInternalPlugin != null) {
			plugins.remove(jivInternalPlugin);
			plugins.add(jivInternalPlugin);
		}
	}

	public static <T extends IModPlugin> Optional<T> getPluginWithClass(Class<? extends T> pluginClass, List<IModPlugin> modPlugins) {
		for (IModPlugin modPlugin : modPlugins) {
			if (pluginClass.isInstance(modPlugin)) {
				T cast = pluginClass.cast(modPlugin);
				return Optional.of(cast);
			}
		}
		return Optional.empty();
	}
}
