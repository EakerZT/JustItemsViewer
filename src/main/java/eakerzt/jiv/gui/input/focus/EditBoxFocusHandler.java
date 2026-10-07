package eakerzt.jiv.gui.input.focus;

import eakerzt.jiv.common.platform.IPlatformScreenHelper;
import eakerzt.jiv.common.platform.Services;
import net.minecraft.client.gui.components.EditBox;

public class EditBoxFocusHandler implements IFocusHandler {
	private final EditBox editBox;
	private final boolean canLoseFocus;
	private boolean wasFocused;

	public EditBoxFocusHandler(EditBox editBox) {
		this.editBox = editBox;
		IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
		this.canLoseFocus = screenHelper.canLoseFocus(this.editBox);
	}

	@Override
	public void unFocus() {
		boolean focused = editBox.isFocused();
		if (focused) {
			if (!canLoseFocus) {
				this.editBox.setCanLoseFocus(true);
			}
			this.editBox.setFocused(false);
		}
		this.wasFocused = focused;
	}

	@Override
	public void focus() {
		if (this.wasFocused) {
			if (!editBox.isFocused()) {
				this.editBox.setFocused(true);
			}
			if (!canLoseFocus) {
				this.editBox.setCanLoseFocus(false);
			}
			this.wasFocused = false;
		}
	}
}
