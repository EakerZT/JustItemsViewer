package eakerzt.jiv.gui.util;

import eakerzt.jiv.api.gui.placement.HorizontalAlignment;
import eakerzt.jiv.api.gui.placement.VerticalAlignment;
import eakerzt.jiv.common.util.ImmutableRect2i;
import eakerzt.jiv.common.util.ImmutableSize2i;

public class AlignmentUtil {
	public static ImmutableRect2i align(ImmutableSize2i size, ImmutableRect2i availableArea, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
		final int width = size.width();
		final int height = size.height();
		final int x = availableArea.getX() + horizontalAlignment.getXPos(availableArea.width(), width);
		final int y = availableArea.getY() + verticalAlignment.getYPos(availableArea.height(), height);
		return new ImmutableRect2i(x, y, width, height);
	}
}
