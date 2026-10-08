package eakerzt.jiv.common.input;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;
import static org.junit.jupiter.api.Assertions.*;

class UserInputMouseGestureTest {
	@Test void macSimulatedRightClickRestoresPhysicalLeftForPressAndRelease() {
		var event = new MouseButtonEvent(10, 20, new MouseButtonInfo(1, GLFW.GLFW_MOD_CONTROL));
		for (InputType type : new InputType[]{InputType.SIMULATE, InputType.EXECUTE}) {
			var input = UserInput.fromVanilla(event, false, type).orElseThrow().withMouseButton(0);
			assertTrue(input.isMouseButton(0));
			assertEquals(GLFW.GLFW_MOD_CONTROL, input.getModifiers());
			assertEquals(type, input.getInputType());
			assertEquals(10, input.getMouseX());
			assertEquals(20, input.getMouseY());
		}
	}
	@Test void ctrlLeftPressAndReleaseRemainLeftMouseGestures() {
		var event = new MouseButtonEvent(10, 20, new MouseButtonInfo(0, GLFW.GLFW_MOD_CONTROL));
		for (InputType type : new InputType[]{InputType.SIMULATE, InputType.EXECUTE}) {
			var input = UserInput.fromVanilla(event, false, type).orElseThrow();
			assertTrue(input.isMouseButton(0));
			assertFalse(input.isMouseButton(1));
		}
	}
}
