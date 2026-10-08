package eakerzt.jiv.gui;

import eakerzt.jiv.api.gui.buttons.IButtonState;
import eakerzt.jiv.api.gui.buttons.IIconButtonController;
import eakerzt.jiv.api.gui.inputs.IJivUserInput;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.gui.JivGuiColors;
import eakerzt.jiv.common.gui.JivGuiColors.GuiColor;
import eakerzt.jiv.common.input.IUserInputHandler;
import eakerzt.jiv.common.input.handlers.CombinedInputHandler;
import eakerzt.jiv.common.util.ImmutableRect2i;
import eakerzt.jiv.common.util.MathUtil;
import eakerzt.jiv.gui.elements.IconButton;
import eakerzt.jiv.gui.input.IPaged;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class PageNavigation {
	private IPaged paged;
	private java.util.function.Supplier<String> displayText;
	private final IconButton nextButton;
	private final IconButton backButton;
	private final boolean hideOnSinglePage;
	private @org.jspecify.annotations.Nullable IconButton leadingButton;
	private String pageNumDisplayString = "1/1";
	private ImmutableRect2i area = ImmutableRect2i.EMPTY;

	public PageNavigation(IPaged paged, boolean hideOnSinglePage) {
		this.paged = paged;
		this.nextButton =
				new IconButton(
						new IIconButtonController() {
							@Override
							public boolean onPress(IJivUserInput b) {
								return b.isSimulate() || PageNavigation.this.paged.nextPage();
							}

							@Override
							public void initState(IButtonState state) {
								state.setIcon(Internal.getTextures().getArrowNext());
								updateState(state);
							}

							@Override
							public void updateState(IButtonState state) {
								state.setActive(PageNavigation.this.paged.hasNext());
							}
						});
		this.backButton =
				new IconButton(
						new IIconButtonController() {
							@Override
							public boolean onPress(IJivUserInput b) {
								return b.isSimulate() || PageNavigation.this.paged.previousPage();
							}

							@Override
							public void initState(IButtonState state) {
								state.setIcon(Internal.getTextures().getArrowPrevious());
								updateState(state);
							}

							@Override
							public void updateState(IButtonState state) {
								state.setActive(PageNavigation.this.paged.hasPrevious());
							}
						});
		this.hideOnSinglePage = hideOnSinglePage;
	}

	private boolean isVisible() {
		if (area.isEmpty()) {
			return false;
		}
		return !hideOnSinglePage || this.paged.hasNext() || this.paged.hasPrevious();
	}

	public void configure(IPaged paged, java.util.function.Supplier<String> displayText) {
		this.paged = paged;
		this.displayText = displayText;
		updatePageNumber();
	}

	public void setLeadingButton(IconButton button) {
		this.leadingButton = button;
		updateBounds(area);
	}

	public ImmutableRect2i getLeadingButtonArea() {
		return leadingButton == null ? ImmutableRect2i.EMPTY : leadingButton.getArea();
	}

	private ImmutableRect2i centerArea() {
		int leadingWidth = leadingButton == null ? 0 : leadingButton.getWidth() + 2;
		return area.cropLeft(backButton.getWidth() + leadingWidth).cropRight(nextButton.getWidth());
	}

	public void drawTooltips(GuiGraphicsExtractor graphics, int x, int y) {
		if (leadingButton != null && isVisible()) leadingButton.drawTooltips(graphics, x, y);
	}

	public void updateBounds(ImmutableRect2i area) {
		this.area = area;
		int buttonSize = Math.min(area.getHeight(), area.width() / 2);
        // All three bookmark buttons use the standard navigation size.
        // Only exceptionally narrow areas constrain their physical fit.
        if (leadingButton != null) buttonSize = Math.min(buttonSize, Math.max(0, (area.width() - 2) / 3));

		ImmutableRect2i backArea = area.keepLeft(buttonSize);
		this.backButton.updateBounds(backArea);

		ImmutableRect2i nextArea = area.keepRight(buttonSize);
		this.nextButton.updateBounds(nextArea);
		if (leadingButton != null) {
			int size = buttonSize;
			leadingButton.updateBounds(
					new ImmutableRect2i(
							backArea.x() + backArea.width() + 1, area.y(), size, area.height()));
		}
	}

	public void updatePageNumber() {
		int pageNum = this.paged.getPageNumber();
		int pageCount = this.paged.getPageCount();
		this.pageNumDisplayString =
				displayText == null
						? String.format("%d/%d", pageNum + 1, pageCount)
						: displayText.get();

		this.nextButton.tick();
		this.backButton.tick();
	}

	public void draw(
			Minecraft minecraft,
			GuiGraphicsExtractor guiGraphics,
			int mouseX,
			int mouseY,
			float partialTicks) {
		if (displayText != null) updatePageNumber();
		if (isVisible()) {
			guiGraphics.fill(
					backButton.getX() + backButton.getWidth(),
					backButton.getY(),
					nextButton.getX(),
					nextButton.getY() + nextButton.getHeight(),
					JivGuiColors.getColor(GuiColor.PAGE_NAVIGATION_BACKGROUND));

			int availableWidth = centerArea().width();
			Font font = minecraft.font;
			ImmutableRect2i centerArea =
					MathUtil.centerTextArea(centerArea(), font, this.pageNumDisplayString);
			if (centerArea.width() <= availableWidth) {
				guiGraphics.text(
						font,
						pageNumDisplayString,
						centerArea.getX(),
						centerArea.getY(),
						JivGuiColors.getColor(GuiColor.PAGE_NAVIGATION_TEXT));
            } else if (leadingButton != null && availableWidth > 0 && font.width(pageNumDisplayString) > 0) {
                var labelArea = centerArea();
                float scale = Math.min(1f, (float) availableWidth / font.width(pageNumDisplayString));
                var pose = guiGraphics.pose();
                pose.pushMatrix();
                try {
                    pose.translate(labelArea.x(), labelArea.y() + (labelArea.height() - font.lineHeight * scale) / 2f);
                    pose.scale(scale, scale);
                    guiGraphics.text(font, pageNumDisplayString, 0, 0, JivGuiColors.getColor(GuiColor.PAGE_NAVIGATION_TEXT));
                } finally {
                    pose.popMatrix();
                }
            }
			if (leadingButton != null) {
				leadingButton.tick();
				leadingButton.draw(guiGraphics, mouseX, mouseY, partialTicks);
			}
			nextButton.draw(guiGraphics, mouseX, mouseY, partialTicks);
			backButton.draw(guiGraphics, mouseX, mouseY, partialTicks);
		}
	}

	public void setForcePressed(boolean nextButton, boolean backButton) {
		this.nextButton.setForcePressed(nextButton);
		this.backButton.setForcePressed(backButton);
	}

	public ImmutableRect2i getNextButtonArea() {
		return nextButton.getArea();
	}

	public ImmutableRect2i getBackButtonArea() {
		return backButton.getArea();
	}

	public IUserInputHandler createInputHandler() {
		return new CombinedInputHandler(
				"PageNavigation",
				new eakerzt.jiv.gui.input.handlers.ProxyInputHandler(
						() ->
								leadingButton == null
										? eakerzt.jiv.gui.input.handlers.NullInputHandler.INSTANCE
										: leadingButton.createInputHandler()),
				this.nextButton.createInputHandler(),
				this.backButton.createInputHandler());
	}
}
