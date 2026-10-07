package eakerzt.jiv.gui.input;

import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.gui.JivGuiColors;
import eakerzt.jiv.common.gui.JivGuiColors.GuiColor;
import eakerzt.jiv.common.gui.elements.ScalableDrawable;
import eakerzt.jiv.common.gui.textures.Textures;
import eakerzt.jiv.common.input.IUserInputHandler;
import eakerzt.jiv.common.util.ImmutableRect2i;
import eakerzt.jiv.common.util.TextHistory;
import eakerzt.jiv.gui.input.focus.ScreenFocusHandler;
import eakerzt.jiv.gui.input.handlers.TextFieldInputHandler;
import eakerzt.jiv.gui.overlay.ISearchField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.BooleanSupplier;

public class GuiTextFieldFilter extends EditBox implements ISearchField {
	private static final int maxSearchLength = 128;
	private static final TextHistory history = new TextHistory();
	private final BooleanSupplier filterEmpty;

	private ImmutableRect2i area;
	private final ScalableDrawable background;
	private ImmutableRect2i backgroundBounds;

	private @Nullable ScreenFocusHandler screenUnfocusHandler;

	public GuiTextFieldFilter(BooleanSupplier filterEmpty) {
		super(Minecraft.getInstance().font, 0, 0, 0, 0, Component.translatable("gui.jiv.search"));
		this.filterEmpty = filterEmpty;

		setMaxLength(maxSearchLength);
		this.area = ImmutableRect2i.EMPTY;
		Textures textures = Internal.getTextures();
		this.background = textures.getSearchBackground();
		this.backgroundBounds = ImmutableRect2i.EMPTY;
		setBordered(false);
	}

	@Override
	public void updateBounds(ImmutableRect2i area) {
		this.backgroundBounds = area;
		setX(area.getX() + 4);
		setY(area.getY() + (area.getHeight() - 8) / 2);
		this.width = Math.max(0, area.getWidth() - 12);
		this.height = area.getHeight();
		this.area = area;
	}

	@Override
	public void setValue(String filterText) {
		if (!filterText.equals(getValue())) {
			super.setValue(filterText);
		}
		int color = JivGuiColors.getColor(GuiColor.SEARCH_FIELD_TEXT);
		if (filterEmpty.getAsBoolean()) {
			color = JivGuiColors.getColor(GuiColor.SEARCH_FIELD_ERROR_TEXT);
		}
		setTextColor(color);
	}

	public Optional<String> getHistory(TextHistory.Direction direction) {
		String currentText = getValue();
		return history.get(direction, currentText);
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return area.contains(mouseX, mouseY);
	}

	public IUserInputHandler createInputHandler() {
		return new TextFieldInputHandler(this);
	}

	@Override
	public void setFocused(boolean keyboardFocus) {
		if (isFocused() == keyboardFocus) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		Screen screen = minecraft.screen;
		if (keyboardFocus) {
			if (screen != null) {
				screenUnfocusHandler = ScreenFocusHandler.create(screen);
				if (screenUnfocusHandler != null) {
					screenUnfocusHandler.unFocus();
				}
			}
			super.setFocused(true);
			if (screen != null) {
				screen.setFocused(this);
			}
		} else {
			super.setFocused(false);
			if (screen != null && screen.getFocused() == this) {
				screen.setFocused(null);
			}
			if (screenUnfocusHandler != null) {
				screenUnfocusHandler.focus();
				screenUnfocusHandler = null;
			}
		}

		String text = getValue();
		history.add(text);
	}

	@Override
	public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		extractBackgroundRenderState(guiGraphics);
		extractForegroundRenderState(guiGraphics, mouseX, mouseY, partialTicks);
	}

	public void extractBackgroundRenderState(GuiGraphicsExtractor guiGraphics) {
		if (this.isVisible()) {
			background.draw(guiGraphics, this.backgroundBounds);
		}
	}

	public void extractForegroundRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.extractWidgetRenderState(guiGraphics, mouseX, mouseY, partialTicks);
	}
}
