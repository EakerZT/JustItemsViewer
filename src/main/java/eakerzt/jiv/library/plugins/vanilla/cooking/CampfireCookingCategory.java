package eakerzt.jiv.library.plugins.vanilla.cooking;

import eakerzt.jiv.api.constants.RecipeTypes;
import eakerzt.jiv.api.gui.builder.IRecipeLayoutBuilder;
import eakerzt.jiv.api.gui.widgets.IRecipeExtrasBuilder;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.api.recipe.IFocusGroup;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.block.Blocks;

public class CampfireCookingCategory extends AbstractCookingCategory<CampfireCookingRecipe> {
	public CampfireCookingCategory(IGuiHelper guiHelper) {
		super(guiHelper, RecipeTypes.CAMPFIRE_COOKING, Blocks.CAMPFIRE, "gui.jiv.category.campfire", 400, 82, 44);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<CampfireCookingRecipe> recipeHolder, IFocusGroup focuses) {
		CampfireCookingRecipe recipe = recipeHolder.value();
		RecipeDisplay display = recipe.display().getFirst();
		if (display instanceof FurnaceRecipeDisplay furnaceRecipeDisplay) {
			builder.addInputSlot(1, 1)
				.setStandardSlotBackground()
				.add(furnaceRecipeDisplay.ingredient());

			builder.addOutputSlot(61, 9)
				.setOutputSlotBackground()
				.add(furnaceRecipeDisplay.result());
		}
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<CampfireCookingRecipe> recipeHolder, IFocusGroup focuses) {
		CampfireCookingRecipe recipe = recipeHolder.value();
		RecipeDisplay display = recipe.display().getFirst();
		if (display instanceof FurnaceRecipeDisplay furnaceRecipeDisplay) {
			int cookTime = furnaceRecipeDisplay.duration();
			if (cookTime <= 0) {
				cookTime = regularCookTime;
			}
			builder.addAnimatedRecipeArrowWidget(cookTime)
				.setPosition(26, 7);
			builder.addAnimatedRecipeFlameWidget(300)
				.setPosition(1, 20);

			addCookTime(builder, furnaceRecipeDisplay);
		}
	}
}
