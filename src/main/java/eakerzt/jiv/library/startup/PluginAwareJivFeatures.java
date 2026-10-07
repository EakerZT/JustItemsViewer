package eakerzt.jiv.library.startup;

import eakerzt.jiv.api.IModPlugin;
import eakerzt.jiv.api.runtime.IJivFeatures;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PluginAwareJivFeatures implements IJivFeatures {
	private static final Logger LOGGER = LogManager.getLogger();

	private final IJivFeatures jivFeatures;
	private final IModPlugin modPlugin;

	public PluginAwareJivFeatures(IJivFeatures jivFeatures, IModPlugin modPlugin) {
		this.jivFeatures = jivFeatures;
		this.modPlugin = modPlugin;
	}

	@Override
	public void disableJivGui() {
		LOGGER.info("JIV GUI is being disabled by {}", modPlugin.getPluginUid());
		jivFeatures.disableJivGui();
	}

	@Override
	public boolean isJivGuiEnabled() {
		return jivFeatures.isJivGuiEnabled();
	}

	@Override
	public void disableInventoryEffectRendererGuiHandler() {
		LOGGER.info("JIV inventory effect renderer GUI handler is being disabled by {}", modPlugin.getPluginUid());
		jivFeatures.disableInventoryEffectRendererGuiHandler();
	}
}
