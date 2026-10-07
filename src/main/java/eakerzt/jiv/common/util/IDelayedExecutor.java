package eakerzt.jiv.common.util;

import java.time.Duration;
import java.util.concurrent.Future;

public interface IDelayedExecutor {
	Future<?> schedule(Runnable command, Duration delay);
}
