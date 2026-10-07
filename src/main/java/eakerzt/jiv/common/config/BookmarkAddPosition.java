package eakerzt.jiv.common.config;

public enum BookmarkAddPosition {
	END,
	FRONT;

	public boolean isFront() {
		return this == FRONT;
	}
}
