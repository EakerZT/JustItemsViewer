package eakerzt.jiv.gui.overlay.bookmarks;

import eakerzt.jiv.api.gui.builder.ITooltipBuilder;
import eakerzt.jiv.api.gui.buttons.IButtonState;
import eakerzt.jiv.api.gui.buttons.IIconButtonController;
import eakerzt.jiv.api.gui.drawable.IDrawable;
import eakerzt.jiv.api.gui.inputs.IJivUserInput;
import eakerzt.jiv.gui.bookmarks.BookmarkList;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** The top control affects the default subgroup only; other groups retain their own layouts. */
public final class BookmarkModeButtonController implements IIconButtonController {
	private final BookmarkList bookmarks;
	private static final IDrawable ITEMS = new ModeIcon(false), RECIPES = new ModeIcon(true);

	public BookmarkModeButtonController(BookmarkList bookmarks) {
		this.bookmarks = bookmarks;
	}

	public boolean onPress(IJivUserInput input) {
		if (!input.isSimulate()) {
			var group = bookmarks.page().groups.get(0);
			group.todo = !group.todo;
			bookmarks.changed();
		}
		return true;
	}

	public void updateState(IButtonState state) {
		state.setIcon(bookmarks.page().groups.get(0).todo ? RECIPES : ITEMS);
		state.setActive(true);
	}

	public void getTooltips(ITooltipBuilder tooltip) {
		tooltip.add(
				Component.translatable(
						bookmarks.page().groups.get(0).todo
								? "jiv.bookmarks.mode.recipes"
								: "jiv.bookmarks.mode.items"));
		tooltip.add(Component.translatable("jiv.bookmarks.mode.switch"));
	}

	private record ModeIcon(boolean recipe) implements IDrawable {
		public int getWidth() {
			return 12;
		}

		public int getHeight() {
			return 12;
		}

		public void draw(GuiGraphicsExtractor graphics, int x, int y) {
			if (recipe) {
				graphics.fill(x, y + 3, x + 4, y + 7, 0xff66ccff);
				graphics.fill(x + 4, y + 4, x + 7, y + 5, 0xffdddddd);
				graphics.fill(x + 6, y + 1, x + 7, y + 10, 0xffdddddd);
				for (int row = 0; row < 3; row++)
					graphics.fill(x + 8, y + row * 4, x + 12, y + row * 4 + 3, 0xff45da75);
			} else {
				for (int row = 0; row < 2; row++)
					for (int column = 0; column < 2; column++) {
						int left = x + column * 7, top = y + row * 7;
						graphics.fill(left, top, left + 5, top + 5, 0xffdddddd);
						graphics.fill(left + 1, top + 1, left + 4, top + 4, 0xff777777);
					}
			}
		}
	}
}
