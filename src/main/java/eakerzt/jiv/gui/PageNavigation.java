package eakerzt.jiv.gui;

import eakerzt.jiv.api.gui.inputs.IJivUserInput;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.gui.JivGuiColors;
import eakerzt.jiv.common.gui.JivGuiColors.GuiColor;
import eakerzt.jiv.common.util.ImmutableRect2i;
import eakerzt.jiv.common.util.MathUtil;
import eakerzt.jiv.api.gui.buttons.IButtonState;
import eakerzt.jiv.api.gui.buttons.IIconButtonController;
import eakerzt.jiv.gui.elements.IconButton;
import eakerzt.jiv.gui.input.IPaged;
import eakerzt.jiv.common.input.IUserInputHandler;
import eakerzt.jiv.common.input.handlers.CombinedInputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class PageNavigation {
	private final IPaged paged;
	private final IconButton nextButton;
	private final IconButton backButton;
	private final boolean hideOnSinglePage;
	private String pageNumDisplayString = "1/1";
	private ImmutableRect2i area = ImmutableRect2i.EMPTY;

	public PageNavigation(IPaged paged, boolean hideOnSinglePage) {
		this.paged = paged;
		this.nextButton = new IconButton(new IIconButtonController() {
			@Override
			public boolean onPress(IJivUserInput b) {
				return b.isSimulate() || paged.nextPage();
			}

			@Override
			public void initState(IButtonState state) {
				state.setIcon(Internal.getTextures().getArrowNext());
				updateState(state);
			}

			@Override
			public void updateState(IButtonState state) {
				state.setActive(paged.getPageCount() > 1);
			}
		});
		this.backButton = new IconButton(new IIconButtonController() {
			@Override
			public boolean onPress(IJivUserInput b) {
				return b.isSimulate() || paged.previousPage();
			}

			@Override
			public void initState(IButtonState state) {
				state.setIcon(Internal.getTextures().getArrowPrevious());
				updateState(state);
			}

			@Override
			public void updateState(IButtonState state) {
				state.setActive(paged.getPageCount() > 1);
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

	public void updateBounds(ImmutableRect2i area) {
		this.area = area;
		int buttonSize = Math.min(area.getHeight(), area.width() / 2);

		ImmutableRect2i backArea = area.keepLeft(buttonSize);
		this.backButton.updateBounds(backArea);

		ImmutableRect2i nextArea = area.keepRight(buttonSize);
		this.nextButton.updateBounds(nextArea);
	}

	public void updatePageNumber() {
		int pageNum = this.paged.getPageNumber();
		int pageCount = this.paged.getPageCount();
		this.pageNumDisplayString = String.format("%d/%d", pageNum + 1, pageCount);

		this.nextButton.tick();
		this.backButton.tick();
	}

	public void draw(Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (isVisible()) {
			guiGraphics.fill(
				backButton.getX() + backButton.getWidth(),
				backButton.getY(),
				nextButton.getX(),
				nextButton.getY() + nextButton.getHeight(),
				JivGuiColors.getColor(GuiColor.PAGE_NAVIGATION_BACKGROUND)
			);

			int availableWidth = this.area.width() - backButton.getWidth() - nextButton.getWidth();
			Font font = minecraft.font;
			ImmutableRect2i centerArea = MathUtil.centerTextArea(this.area, font, this.pageNumDisplayString);
			if (centerArea.width() <= availableWidth) {
				guiGraphics.text(font, pageNumDisplayString, centerArea.getX(), centerArea.getY(), JivGuiColors.getColor(GuiColor.PAGE_NAVIGATION_TEXT));
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
			this.nextButton.createInputHandler(),
			this.backButton.createInputHandler()
		);
	}

}
