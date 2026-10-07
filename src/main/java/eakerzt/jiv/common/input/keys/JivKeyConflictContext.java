package eakerzt.jiv.common.input.keys;

import net.minecraft.client.Minecraft;

public enum JivKeyConflictContext {
	UNIVERSAL {
		@Override
		public boolean isActive() {
			return true;
		}

		@Override
		public boolean conflicts(JivKeyConflictContext other) {
			return true;
		}
	},
	GUI {
		@Override
		public boolean isActive() {
			return Minecraft.getInstance().screen != null;
		}
	},
	IN_GAME {
		@Override
		public boolean isActive() {
			return !GUI.isActive();
		}
	},
	JIV_GUI_HOVER,
	JIV_GUI_HOVER_BOOKMARK,
	JIV_GUI_HOVER_CHEAT_MODE,
	JIV_GUI_HOVER_CONFIG_BUTTON,
	JIV_GUI_HOVER_INGREDIENT,
	JIV_GUI_HOVER_SEARCH,
	JIV_GUI_FOCUSED_SEARCH;

	public boolean isActive() {
		return GUI.isActive();
	}

	public boolean conflicts(JivKeyConflictContext other) {
		return this == other;
	}
}
