package eakerzt.jiv.common.input.keys;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public abstract class AbstractJivKeyMappingBuilder implements IJivKeyMappingBuilder {
	protected abstract IJivKeyMappingInternal buildMouse(int mouseButton);

	@Override
	public final IJivKeyMappingInternal buildMouseLeft() {
		return buildMouse(InputConstants.MOUSE_BUTTON_LEFT);
	}

	@Override
	public final IJivKeyMappingInternal buildMouseRight() {
		return buildMouse(InputConstants.MOUSE_BUTTON_RIGHT);
	}

	@Override
	public final IJivKeyMappingInternal buildMouseMiddle() {
		return buildMouse(InputConstants.MOUSE_BUTTON_MIDDLE);
	}

	@Override
	public final IJivKeyMappingInternal buildUnbound() {
		return buildKeyboardKey(GLFW.GLFW_KEY_UNKNOWN);
	}
}
