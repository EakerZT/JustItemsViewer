package eakerzt.jiv.common;

import eakerzt.jiv.api.runtime.IJivFeatures;

public class JivFeatures implements IJivFeatures {
	private boolean jivGuiEnabled = true;
	private boolean inventoryEffectRendererGuiHandlerEnabled = true;

	@Override
	public void disableJivGui() {
		jivGuiEnabled = false;
	}

	@Override
	public boolean isJivGuiEnabled() {
		return jivGuiEnabled;
	}

	@Override
	public void disableInventoryEffectRendererGuiHandler() {
		inventoryEffectRendererGuiHandlerEnabled = false;
	}

	public boolean getInventoryEffectRendererGuiHandlerEnabled() {
		return inventoryEffectRendererGuiHandlerEnabled;
	}
}
