package eakerzt.jiv.neoforge.plugins.neoforge;

import eakerzt.jiv.api.IModPlugin;
import eakerzt.jiv.api.JivPlugin;
import eakerzt.jiv.api.constants.ModIds;
import eakerzt.jiv.api.neoforge.NeoForgeTypes;
import eakerzt.jiv.api.registration.IRuntimeRegistration;
import eakerzt.jiv.api.registration.ISlotDisplayInterpreterRegistration;
import eakerzt.jiv.api.runtime.IJivFeatures;
import eakerzt.jiv.gui.config.JivGuiSortingConfigData;
import eakerzt.jiv.gui.config.JivGuiSortingConfigRegistration;
import eakerzt.jiv.gui.startup.JivEventHandlers;
import eakerzt.jiv.gui.startup.JivGuiStarter;
import eakerzt.jiv.gui.startup.ResourceReloadHandler;
import eakerzt.jiv.neoforge.events.RuntimeEventSubscriptions;
import eakerzt.jiv.neoforge.startup.EventRegistration;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

@JivPlugin
public class NeoForgeGuiPlugin implements IModPlugin {
	private static final Logger LOGGER = LogManager.getLogger();
	private static @Nullable ResourceReloadHandler resourceReloadHandler;

	private final JivGuiSortingConfigData sortingConfigData = JivGuiSortingConfigRegistration.get();
	private @Nullable IJivFeatures jivFeatures;
	private final RuntimeEventSubscriptions runtimeSubscriptions = new RuntimeEventSubscriptions(NeoForge.EVENT_BUS);

	@Override
	public Identifier getPluginUid() {
		return Identifier.fromNamespaceAndPath(ModIds.JIV_ID, "neoforge_gui");
	}

	@Override
	public void configureJiv(IJivFeatures jivFeatures) {
		this.jivFeatures = jivFeatures;
	}

	@Override
	public void registerSlotDisplayInterpreters(ISlotDisplayInterpreterRegistration registration) {
		registration.register(
			NeoForgeMod.FLUID_SLOT_DISPLAY.get(),
			NeoForgeTypes.FLUID_STACK,
			(ignoredSlotDisplay, ignoredContext, interpretationBuilder) -> {
				interpretationBuilder.setWildcardForSubtypes(true);
			}
		);
		registration.register(
			NeoForgeMod.FLUID_TAG_SLOT_DISPLAY.get(),
			NeoForgeTypes.FLUID_STACK,
			(slotDisplay, ignoredContext, interpretationBuilder) -> {
				interpretationBuilder
					.setTagKey(slotDisplay.tag())
					.setWildcardForSubtypes(true);
			}
		);
	}

	@Override
	public void registerRuntime(IRuntimeRegistration registration) {
		if (!isJivGuiEnabled()) {
			return;
		}

		if (!runtimeSubscriptions.isEmpty()) {
			LOGGER.error("JIV GUI is already running.");
			runtimeSubscriptions.clear();
		}

		JivEventHandlers eventHandlers = JivGuiStarter.start(registration, sortingConfigData);
		resourceReloadHandler = eventHandlers.resourceReloadHandler();

		EventRegistration.registerEvents(runtimeSubscriptions, eventHandlers);
	}

	@Override
	public void onRuntimeUnavailable() {
		LOGGER.info("Stopping JIV GUI");
		runtimeSubscriptions.clear();
		resourceReloadHandler = null;
	}

	public static Optional<ResourceReloadHandler> getResourceReloadHandler() {
		return Optional.ofNullable(resourceReloadHandler);
	}

	private boolean isJivGuiEnabled() {
		IJivFeatures jivFeatures = this.jivFeatures;
		return jivFeatures == null || jivFeatures.isJivGuiEnabled();
	}
}
