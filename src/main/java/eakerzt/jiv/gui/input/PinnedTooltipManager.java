package eakerzt.jiv.gui.input;

import com.mojang.blaze3d.platform.InputConstants;
import eakerzt.jiv.common.input.keys.IJivKeyMappingInternal;
import eakerzt.jiv.common.input.keys.IJivKeyMappingWithExtraModifiers;
import org.jspecify.annotations.Nullable;

public final class PinnedTooltipManager {
	private static @Nullable IPinnedTooltipHolder active;
	private static int activeTooltipRenderDepth;

	public static void opened(IPinnedTooltipHolder holder) {
		IPinnedTooltipHolder active = PinnedTooltipManager.active;
		if (active != null && active != holder) {
			active.hide();
		}
		PinnedTooltipManager.active = holder;
	}

	public static void closed(IPinnedTooltipHolder holder) {
		if (PinnedTooltipManager.active == holder) {
			PinnedTooltipManager.active = null;
		}
	}

	public static void draw(IPinnedTooltipHolder holder, Runnable draw) {
		if (active != holder) {
			draw.run();
			return;
		}
		activeTooltipRenderDepth++;
		try {
			draw.run();
		} finally {
			activeTooltipRenderDepth--;
		}
	}

	public static boolean shouldSuppressExternalTooltip() {
		return active != null && activeTooltipRenderDepth == 0;
	}

	public static boolean matchesInput(
		InputConstants.Key inputKey,
		IJivKeyMappingWithExtraModifiers keyMapping,
		IJivKeyMappingInternal pinKeyMapping
	) {
		if (keyMapping.isActiveAndMatches(inputKey)) {
			return true;
		}
		return active != null &&
			pinKeyMapping.isDown() &&
			inputKey.getType() != InputConstants.Type.MOUSE &&
			keyMapping.isActiveAndMatchesAllowingExtraModifiers(inputKey);
	}

	private PinnedTooltipManager() {
	}
}
