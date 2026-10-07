package eakerzt.jiv.gui.overlay;

import eakerzt.jiv.api.gui.handlers.IGuiProperties;

@SuppressWarnings({"rawtypes", "unchecked"})
record TestGuiProperties(
	int guiLeft,
	int guiTop,
	int guiXSize,
	int guiYSize,
	int screenWidth,
	int screenHeight
) implements IGuiProperties {
	@Override
	public Class screenClass() {
		return Object.class;
	}
}
