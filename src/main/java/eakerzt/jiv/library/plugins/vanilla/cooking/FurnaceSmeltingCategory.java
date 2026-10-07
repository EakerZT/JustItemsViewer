package eakerzt.jiv.library.plugins.vanilla.cooking;

import eakerzt.jiv.api.constants.RecipeTypes;
import eakerzt.jiv.api.helpers.IGuiHelper;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.Blocks;

public class FurnaceSmeltingCategory extends AbstractCookingCategory<SmeltingRecipe> {
	public FurnaceSmeltingCategory(IGuiHelper guiHelper) {
		super(guiHelper, RecipeTypes.SMELTING, Blocks.FURNACE, "gui.jiv.category.smelting", 200, 116, 54);
	}
}
