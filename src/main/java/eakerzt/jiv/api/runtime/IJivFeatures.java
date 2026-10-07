package eakerzt.jiv.api.runtime;

import eakerzt.jiv.api.IModPlugin;
import eakerzt.jiv.api.gui.handlers.IGuiContainerHandler;
import org.jetbrains.annotations.ApiStatus;

/**
 * Provides access for mod plugins to disable various JIV features.
 * This may be needed by mods that substantially change hard-coded vanilla behaviors.
 *
 * Get an instance from {@link IModPlugin#configureJiv(IJivFeatures)}.
 *
 * @since 17.3.0
 */
@ApiStatus.NonExtendable
public interface IJivFeatures {
	/**
	 * Disable JIV's built-in GUI.
	 *
	 * <p>
	 * This prevents JIV from registering its own GUI handlers, overlays, recipe GUI,
	 * input handlers, render handlers, and GUI resource reload handlers. JIV's API,
	 * ingredient and recipe registrations, recipe transfer handlers, and runtime remain available.
	 * </p>
	 *
	 * <p>
	 * This should be called from {@link IModPlugin#configureJiv(IJivFeatures)}
	 * so that JIV can disable the GUI before its own GUI handlers are registered.
	 * </p>
	 *
	 * @since 29.20.0
	 */
	default void disableJivGui() {

	}

	/**
	 * Returns true if JIV's built-in GUI is enabled.
	 *
	 * @since 29.20.0
	 */
	boolean isJivGuiEnabled();

	/**
	 * Disable JIV's Inventory Effect Renderer {@link IGuiContainerHandler}.
	 * This is used by JIV in order to move out of the way of potion effects shown next to the inventory.
	 * It can be disabled by mods that remove this behavior or substitute their own.
	 *
	 * @since 17.3.0
	 */
	void disableInventoryEffectRendererGuiHandler();
}
