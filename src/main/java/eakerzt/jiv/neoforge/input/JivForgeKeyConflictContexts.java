package eakerzt.jiv.neoforge.input;

import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

public enum JivForgeKeyConflictContexts implements IKeyConflictContext {
	JIV_GUI_HOVER,
	JIV_GUI_HOVER_BOOKMARK,
	JIV_GUI_HOVER_CHEAT_MODE,
	JIV_GUI_HOVER_CONFIG_BUTTON,
	JIV_GUI_HOVER_INGREDIENT,
	JIV_GUI_HOVER_SEARCH,
	JIV_GUI_FOCUSED_SEARCH;

	@Override
	public boolean isActive() {
		return KeyConflictContext.GUI.isActive();
	}

	@Override
	public boolean conflicts(IKeyConflictContext other) {
		return this == other;
	}
}
