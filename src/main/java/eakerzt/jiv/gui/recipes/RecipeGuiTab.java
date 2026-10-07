package eakerzt.jiv.gui.recipes;

import eakerzt.jiv.api.gui.drawable.IDrawable;
import eakerzt.jiv.api.recipe.category.IRecipeCategory;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.gui.JivTooltip;
import eakerzt.jiv.common.gui.textures.Textures;
import eakerzt.jiv.common.input.IUserInputHandler;
import eakerzt.jiv.common.util.ImmutableRect2i;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public abstract class RecipeGuiTab implements IUserInputHandler {
	public static final int TAB_HEIGHT = 24;
	public static final int TAB_WIDTH = 24;

	protected final ImmutableRect2i area;

	public RecipeGuiTab(int x, int y) {
		this.area = new ImmutableRect2i(x, y, TAB_WIDTH, TAB_HEIGHT);
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return area.contains(mouseX, mouseY);
	}

	public abstract boolean isSelected(IRecipeCategory<?> selectedCategory);

	public void draw(boolean selected, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		Textures textures = Internal.getTextures();
		IDrawable tab = textures.getTabUnselected();
		if (selected) {
			tab = textures.getTabSelected();
		}

		tab.draw(guiGraphics, area.x(), area.y());
	}

	public abstract JivTooltip getTooltip();
}
