package eakerzt.jiv.library.plugins.vanilla.stonecutting;

import com.mojang.serialization.Codec;
import eakerzt.jiv.api.constants.RecipeTypes;
import eakerzt.jiv.api.gui.builder.IRecipeLayoutBuilder;
import eakerzt.jiv.api.gui.widgets.IRecipeExtrasBuilder;
import eakerzt.jiv.api.helpers.ICodecHelper;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.api.recipe.IFocusGroup;
import eakerzt.jiv.api.recipe.IRecipeManager;
import eakerzt.jiv.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.StonecutterRecipeDisplay;
import net.minecraft.world.level.block.Blocks;

public class StoneCuttingRecipeCategory extends AbstractRecipeCategory<RecipeHolder<StonecutterRecipe>> {
	public static final int width = 82;
	public static final int height = 34;

	public StoneCuttingRecipeCategory(IGuiHelper guiHelper) {
		super(
			RecipeTypes.STONECUTTING,
			Component.translatable("gui.jiv.category.stoneCutter"),
			guiHelper.createDrawableItemLike(Blocks.STONECUTTER),
			width,
			height
		);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<StonecutterRecipe> recipeHolder, IFocusGroup focuses) {
		StonecutterRecipe recipe = recipeHolder.value();
		RecipeDisplay display = recipe.display().getFirst();
		if (display instanceof StonecutterRecipeDisplay stonecutterRecipeDisplay) {
			builder.addInputSlot(1, 9)
				.setStandardSlotBackground()
				.add(stonecutterRecipeDisplay.input());

			builder.addOutputSlot(61, 9)
				.setOutputSlotBackground()
				.add(stonecutterRecipeDisplay.result());
		}
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<StonecutterRecipe> recipe, IFocusGroup focuses) {
		builder.addRecipeArrowWidget().setPosition(26, 9);
	}

	@Override
	public boolean isHandled(RecipeHolder<StonecutterRecipe> recipeHolder) {
		StonecutterRecipe recipe = recipeHolder.value();
		return !recipe.isSpecial();
	}

	@Override
	public Identifier getIdentifier(RecipeHolder<StonecutterRecipe> recipe) {
		return recipe.id().identifier();
	}

	@Override
	public Codec<RecipeHolder<StonecutterRecipe>> getCodec(ICodecHelper codecHelper, IRecipeManager recipeManager) {
		return codecHelper.getRecipeHolderCodec();
	}
}
