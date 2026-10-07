package eakerzt.jiv.library.gui.widgets;

import eakerzt.jiv.api.gui.drawable.IDrawable;
import eakerzt.jiv.api.gui.inputs.IJivInputHandler;
import eakerzt.jiv.api.gui.widgets.IScrollBoxWidget;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.config.IClientConfig;
import eakerzt.jiv.common.config.IClientConfigs;
import eakerzt.jiv.common.gui.elements.DrawableBlank;
import eakerzt.jiv.common.gui.elements.DrawableWrappedText;
import eakerzt.jiv.common.util.ImmutableRect2i;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.FormattedText;

import java.util.List;

public class ScrollBoxRecipeWidget extends AbstractScrollWidget implements IScrollBoxWidget, IJivInputHandler {
	private IDrawable contents = DrawableBlank.EMPTY;

	public ScrollBoxRecipeWidget(int width, int height, int xPos, int yPos) {
		super(new ImmutableRect2i(xPos, yPos, width, height));
	}

	@Override
	public int getContentAreaWidth() {
		return contentsArea.width();
	}

	@Override
	public int getContentAreaHeight() {
		return contentsArea.height();
	}

	@Override
	public IScrollBoxWidget setContents(IDrawable contents) {
		this.contents = contents;
		return this;
	}

	@Override
	public IScrollBoxWidget setContents(List<FormattedText> text) {
		this.contents = new DrawableWrappedText(text, getContentAreaWidth());
		return this;
	}

	@Override
	protected int getVisibleAmount() {
		return contentsArea.height();
	}

	@Override
	protected int getHiddenAmount() {
		return Math.max(contents.getHeight() - contentsArea.height(), 0);
	}

	@Override
	protected void drawContents(GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY, float scrollOffsetY) {
		var poseStack = guiGraphics.pose();

		guiGraphics.enableScissor(
			contentsArea.x(),
			contentsArea.y(),
			contentsArea.width(),
			contentsArea.height()
		);
		poseStack.pushMatrix();
		float scrollAmount = getHiddenAmount() * scrollOffsetY;
		poseStack.translate(0f, -scrollAmount);
		try {
			contents.draw(guiGraphics);
		} finally {
			poseStack.popMatrix();
			guiGraphics.disableScissor();
		}
	}

	@Override
	protected float calculateScrollAmount(double scrollDeltaY) {
		IClientConfigs jivClientConfigs = Internal.getClientConfigs();
		IClientConfig clientConfig = jivClientConfigs.getClientConfig();
		int smoothScrollRate = clientConfig.smoothScrollRate().get();

		int totalHeight = contents.getHeight();
		double scrollAmount = scrollDeltaY * smoothScrollRate;
		return (float) (scrollAmount / (double) totalHeight);
	}
}
