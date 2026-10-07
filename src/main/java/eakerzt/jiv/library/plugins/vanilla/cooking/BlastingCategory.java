package eakerzt.jiv.library.plugins.vanilla.cooking;

import eakerzt.jiv.api.constants.RecipeTypes;
import eakerzt.jiv.api.helpers.IGuiHelper;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.level.block.Blocks;

public class BlastingCategory extends AbstractCookingCategory<BlastingRecipe> {
	public BlastingCategory(IGuiHelper guiHelper) {
		super(guiHelper, RecipeTypes.BLASTING, Blocks.BLAST_FURNACE, "gui.jiv.category.blasting", 100);
	}
}
