package eakerzt.jiv.api.gui;

import eakerzt.jiv.api.gui.drawable.IDrawableAnimated;
import eakerzt.jiv.api.helpers.IGuiHelper;
import org.jetbrains.annotations.ApiStatus;

/**
 * A timer to help render things that normally depend on ticks.
 * Get an instance from {@link IGuiHelper#createTickTimer(int, int, boolean)}.
 * These are used in the internal implementation of {@link IDrawableAnimated}.
 */
@ApiStatus.NonExtendable
public interface ITickTimer {
	int getValue();

	int getMaxValue();
}
