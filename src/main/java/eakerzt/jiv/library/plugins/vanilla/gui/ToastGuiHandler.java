package eakerzt.jiv.library.plugins.vanilla.gui;

import eakerzt.jiv.api.gui.handlers.IGlobalGuiHandler;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.config.IClientConfig;
import eakerzt.jiv.common.platform.IPlatformScreenHelper;
import eakerzt.jiv.common.platform.Services;
import eakerzt.jiv.common.util.ImmutableRect2i;
import net.minecraft.client.renderer.Rect2i;

import java.util.Collection;
import java.util.List;

public class ToastGuiHandler implements IGlobalGuiHandler {

	@Override
	public Collection<Rect2i> getGuiExtraAreas() {
		IClientConfig clientConfig = Internal.getClientConfigs().getClientConfig();

		if (!clientConfig.toastReflowEnabled().get()) {
			return List.of();
		}

		IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
		ImmutableRect2i toastsArea = screenHelper.getToastsArea();

		if (toastsArea.isEmpty()) {
			return List.of();
		}
		return List.of(toastsArea.toMutable());
	}
}
