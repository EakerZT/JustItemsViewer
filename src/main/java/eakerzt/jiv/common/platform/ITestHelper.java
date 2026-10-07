package eakerzt.jiv.common.platform;

import net.minecraft.gametest.framework.UnknownGameTestException;

public interface ITestHelper {
	Throwable getReason(UnknownGameTestException exception);
}
