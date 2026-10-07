package eakerzt.jiv.gui.recipes;

import eakerzt.jiv.api.gui.IRecipeLayoutDrawable;
import eakerzt.jiv.common.input.IUserInputHandler;
import eakerzt.jiv.gui.bookmarks.RecipeBookmark;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jspecify.annotations.Nullable;

public interface IRecipeLayoutWithButtons<R> {
	void draw(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks);

	void updateBounds(int recipeXOffset, int recipeYOffset);

	int totalWidth();

	IUserInputHandler createUserInputHandler();

	void tick();

	IRecipeLayoutDrawable<R> getRecipeLayout();

	@Nullable
	RecipeBookmark<?, ?> getRecipeBookmark();

	void drawTooltips(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY);

	int getMissingCountHint();
}
