package eakerzt.jiv.library.plugins.vanilla.compostable;

import eakerzt.jiv.api.constants.RecipeTypes;
import eakerzt.jiv.api.gui.builder.IRecipeLayoutBuilder;
import eakerzt.jiv.api.gui.placement.HorizontalAlignment;
import eakerzt.jiv.api.gui.placement.VerticalAlignment;
import eakerzt.jiv.api.gui.widgets.IRecipeExtrasBuilder;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.api.recipe.IFocusGroup;
import eakerzt.jiv.api.recipe.category.AbstractRecipeCategory;
import eakerzt.jiv.api.recipe.vanilla.IJivCompostingRecipe;
import eakerzt.jiv.common.gui.JivGuiColors;
import eakerzt.jiv.common.gui.JivGuiColors.GuiColor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;

public class CompostableRecipeCategory extends AbstractRecipeCategory<IJivCompostingRecipe> {
	public CompostableRecipeCategory(IGuiHelper guiHelper) {
		super(
			RecipeTypes.COMPOSTING,
			Component.translatable("gui.jiv.category.compostable"),
			guiHelper.createDrawableItemLike(Blocks.COMPOSTER),
			120,
			18
		);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, IJivCompostingRecipe recipe, IFocusGroup focuses) {
		builder.addInputSlot(1, 1)
			.setStandardSlotBackground()
			.addItemStacks(recipe.getInputs());
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, IJivCompostingRecipe recipe, IFocusGroup focuses) {
		float chance = recipe.getChance();
		int chancePercent = (int) Math.floor(chance * 100);
		Component text = Component.translatable("gui.jiv.category.compostable.chance", chancePercent);
		builder.addText(text, getWidth() - 24, getHeight())
			.setPosition(24, 0)
			.setTextAlignment(HorizontalAlignment.CENTER)
			.setTextAlignment(VerticalAlignment.CENTER)
			.setColor(JivGuiColors.getColor(GuiColor.RECIPE_COMPOSTING_CHANCE_TEXT));
	}

	@Override
	public Identifier getIdentifier(IJivCompostingRecipe recipe) {
		return recipe.getUid();
	}
}
