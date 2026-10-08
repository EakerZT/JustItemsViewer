package eakerzt.jiv.gui.recipes;

import eakerzt.jiv.api.gui.builder.ITooltipBuilder;
import eakerzt.jiv.api.gui.inputs.IJivUserInput;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.gui.JivGuiColors;
import eakerzt.jiv.common.gui.JivGuiColors.GuiColor;
import eakerzt.jiv.common.gui.textures.Textures;
import eakerzt.jiv.gui.bookmarks.BookmarkList;
import eakerzt.jiv.gui.bookmarks.IBookmark;
import eakerzt.jiv.api.gui.buttons.IButtonState;
import eakerzt.jiv.api.gui.buttons.IIconButtonController;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class RecipeBookmarkButtonController implements IIconButtonController {
	private final BookmarkList bookmarks;
	private final @Nullable IBookmark recipeBookmark;
	private boolean bookmarked;

	public RecipeBookmarkButtonController(BookmarkList bookmarks, @Nullable IBookmark recipeBookmark) {
		this.bookmarks = bookmarks;
		this.recipeBookmark = recipeBookmark;
	}

	@Override
	public void getTooltips(ITooltipBuilder tooltip) {
		if (recipeBookmark != null) {
			if (bookmarks.containsUngrouped(recipeBookmark)) {
				tooltip.add(Component.translatable("jiv.tooltip.bookmarks.recipe.remove"));
			} else {
				tooltip.add(Component.translatable("jiv.tooltip.bookmarks.recipe.add"));
			}
		}
	}

	@Override
	public void initState(IButtonState state) {
		Textures textures = Internal.getTextures();
		state.setIcon(textures.getRecipeBookmark());
		if (recipeBookmark == null) {
			state.setActive(false);
			state.setVisible(false);
		}
		updateState(state);
	}

	@Override
	public void updateState(IButtonState state) {
		bookmarked = recipeBookmark != null && bookmarks.containsUngrouped(recipeBookmark);
		state.setForcePressed(bookmarked);
	}

	@Override
	public boolean onPress(IJivUserInput input) {
		if (recipeBookmark != null) {
			if (!input.isSimulate()) {
				bookmarks.toggleBookmark(recipeBookmark);
			}
			return true;
		}
		return false;
	}

	@Override
	public void drawExtras(GuiGraphicsExtractor guiGraphics, Rect2i buttonArea, int mouseX, int mouseY, float partialTicks) {
		if (bookmarked) {
			guiGraphics.fill(
				buttonArea.getX(),
				buttonArea.getY(),
				buttonArea.getX() + buttonArea.getWidth(),
				buttonArea.getY() + buttonArea.getHeight(),
				JivGuiColors.getColor(GuiColor.BOOKMARKED_RECIPE_OVERLAY)
			);
		}
	}
}
