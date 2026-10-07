package eakerzt.jiv.library.startup;

public interface IStopCallback {
	/**
	 * Called when JIV is stopping (the player logs out).
	 * Used for cleaning up caches and resources so they can be released.
	 */
	void onRuntimeStopped();
}
