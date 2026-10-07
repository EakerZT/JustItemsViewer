package eakerzt.jiv.library.plugins.vanilla.grindstone;

import eakerzt.jiv.api.constants.RecipeTypes;
import eakerzt.jiv.api.constants.VanillaTypes;
import eakerzt.jiv.api.gui.builder.IRecipeLayoutBuilder;
import eakerzt.jiv.api.gui.builder.IRecipeSlotBuilder;
import eakerzt.jiv.api.gui.placement.HorizontalAlignment;
import eakerzt.jiv.api.gui.widgets.IRecipeExtrasBuilder;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.api.recipe.IFocusGroup;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.recipe.category.AbstractRecipeCategory;
import eakerzt.jiv.api.recipe.vanilla.IJivGrindstoneRecipe;
import eakerzt.jiv.common.gui.JivGuiColors;
import eakerzt.jiv.common.gui.JivGuiColors.GuiColor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GrindstoneRecipeCategory extends AbstractRecipeCategory<IJivGrindstoneRecipe> {
	private static final String topSlotName = "topSlot";
	private static final String bottomSlotName = "bottomSlot";

	public GrindstoneRecipeCategory(IGuiHelper guiHelper) {
		super(
			RecipeTypes.GRINDSTONE,
			Component.translatable("gui.jiv.category.grindstone"),
			guiHelper.createDrawableItemLike(Blocks.GRINDSTONE),
			73,
			52
		);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, IJivGrindstoneRecipe recipe, IFocusGroup focuses) {
		List<ItemStack> topInputs = recipe.getTopInputs();
		List<ItemStack> bottomInputs = recipe.getBottomInputs();
		List<ItemStack> outputs = recipe.getOutputs();

		IRecipeSlotBuilder topInputSlot = builder.addInputSlot(1, 1)
			.addIngredients(VanillaTypes.ITEM_STACK, getDisplayInputs(topInputs))
			.setStandardSlotBackground()
			.setSlotName(topSlotName);

		IRecipeSlotBuilder bottomInputSlot = builder.addInputSlot(1, 24)
			.addIngredients(VanillaTypes.ITEM_STACK, getDisplayInputs(bottomInputs))
			.setStandardSlotBackground()
			.setSlotName(bottomSlotName);

		int outputSlotXPosition = 52;
		int outputSlotYPosition = 13;
		IRecipeSlotBuilder outputSlot;
		if (recipe.isOutputRenderOnly()) {
			outputSlot = builder.addSlot(RecipeIngredientRole.RENDER_ONLY, outputSlotXPosition, outputSlotYPosition);
		} else {
			outputSlot = builder.addOutputSlot(outputSlotXPosition, outputSlotYPosition);
		}
		outputSlot.setOutputSlotBackground().addItemStacks(outputs);

		if (topInputs.size() == bottomInputs.size()) {
			if (topInputs.size() == outputs.size()) {
				builder.createFocusLink(topInputSlot, bottomInputSlot, outputSlot);
			}
		} else if (topInputs.size() == outputs.size() && bottomInputs.size() == 1) {
			builder.createFocusLink(topInputSlot, outputSlot);
		} else if (bottomInputs.size() == outputs.size() && topInputs.size() == 1) {
			builder.createFocusLink(bottomInputSlot, outputSlot);
		}
	}

	private static List<@Nullable ItemStack> getDisplayInputs(List<ItemStack> inputs) {
		List<@Nullable ItemStack> displayInputs = new ArrayList<>(inputs.size());
		for (ItemStack input : inputs) {
			if (input.isEmpty()) {
				displayInputs.add(null);
			} else {
				displayInputs.add(input);
			}
		}
		return displayInputs;
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, IJivGrindstoneRecipe recipe, IFocusGroup focuses) {
		builder.addRecipeArrowWidget().setPosition(20, 12);

		int maxXpReward = recipe.getMaxXpReward();
		if (maxXpReward > 0) {
			int minXpReward = recipe.getMinXpReward();
			Component text = Component.translatable("gui.jiv.category.grindstone.experience", minXpReward, maxXpReward);
			builder.addText(text, getWidth(), 10)
				.setPosition(0, 43)
				.setColor(JivGuiColors.getColor(GuiColor.GRINDSTONE_EXPERIENCE_REWARD_TEXT))
				.setShadow(true)
				.setTextAlignment(HorizontalAlignment.RIGHT);
		}
	}

	@Override
	public @Nullable Identifier getIdentifier(IJivGrindstoneRecipe recipe) {
		return recipe.getUid();
	}
}
