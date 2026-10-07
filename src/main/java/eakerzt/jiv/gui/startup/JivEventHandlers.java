package eakerzt.jiv.gui.startup;

import eakerzt.jiv.gui.events.GuiEventHandler;
import eakerzt.jiv.gui.input.ClientInputHandler;

public record JivEventHandlers(
	GuiEventHandler guiEventHandler,
	ClientInputHandler clientInputHandler,
	ResourceReloadHandler resourceReloadHandler
) {
}
