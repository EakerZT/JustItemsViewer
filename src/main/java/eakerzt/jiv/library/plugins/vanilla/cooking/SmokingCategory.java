package eakerzt.jiv.library.plugins.vanilla.cooking;

import eakerzt.jiv.api.constants.RecipeTypes;
import eakerzt.jiv.api.helpers.IGuiHelper;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.block.Blocks;

public class SmokingCategory extends AbstractCookingCategory<SmokingRecipe> {
	public SmokingCategory(IGuiHelper guiHelper) {
		super(guiHelper, RecipeTypes.SMOKING, Blocks.SMOKER, "gui.jiv.category.smoking", 100);
	}
}
