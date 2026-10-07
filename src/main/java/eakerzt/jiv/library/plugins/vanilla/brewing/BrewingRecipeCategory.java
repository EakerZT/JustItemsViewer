package eakerzt.jiv.library.plugins.vanilla.brewing;

import eakerzt.jiv.api.constants.RecipeTypes;
import eakerzt.jiv.api.gui.ITickTimer;
import eakerzt.jiv.api.gui.builder.IRecipeLayoutBuilder;
import eakerzt.jiv.api.gui.drawable.IDrawable;
import eakerzt.jiv.api.gui.drawable.IDrawableAnimated;
import eakerzt.jiv.api.gui.drawable.IDrawableStatic;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotsView;
import eakerzt.jiv.api.gui.widgets.IRecipeExtrasBuilder;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.api.recipe.IFocusGroup;
import eakerzt.jiv.api.recipe.category.AbstractRecipeCategory;
import eakerzt.jiv.api.recipe.vanilla.IJivBrewingRecipe;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.gui.JivGuiColors;
import eakerzt.jiv.common.gui.JivGuiColors.GuiColor;
import eakerzt.jiv.common.gui.textures.Textures;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.BrewingStandScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class BrewingRecipeCategory extends AbstractRecipeCategory<IJivBrewingRecipe> {
	private final IDrawable background;
	private final IDrawableAnimated arrow;
	private final IDrawableAnimated bubbles;
	private final IDrawableStatic blazeHeat;

	public BrewingRecipeCategory(IGuiHelper guiHelper) {
		super(
			RecipeTypes.BREWING,
			Component.translatable("gui.jiv.category.brewing"),
			guiHelper.createDrawableItemLike(Blocks.BREWING_STAND),
			114,
			61
		);
		Textures textures = Internal.getTextures();
		background = textures.getBrewingStandBackground();

		arrow = guiHelper.createAnimatedDrawable(textures.getBrewingStandArrow(), 400, IDrawableAnimated.StartDirection.TOP, false);

		ITickTimer bubblesTickTimer = new BrewingBubblesTickTimer(guiHelper);
		bubbles = guiHelper.createAnimatedDrawable(textures.getBrewingStandBubbles(), bubblesTickTimer, IDrawableAnimated.StartDirection.BOTTOM);

		blazeHeat = textures.getBrewingStandBlazeHeat();
	}

	@Override
	public void draw(IJivBrewingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
		background.draw(guiGraphics, 0, 1);
		blazeHeat.draw(guiGraphics, 5, 30);
		bubbles.draw(guiGraphics, 9, 1);
		arrow.draw(guiGraphics, 43, 3);
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, IJivBrewingRecipe recipe, IFocusGroup focuses) {
		int brewingSteps = recipe.getBrewingSteps();
		String brewingStepsString = "?";
		if (brewingSteps < Integer.MAX_VALUE) {
			brewingStepsString = Integer.toString(brewingSteps);
		}
		Component steps = Component.translatable("gui.jiv.category.brewing.steps", brewingStepsString);

		builder.addText(steps, 42, 12)
			.setPosition(70, 28)
			.setColor(JivGuiColors.getColor(GuiColor.RECIPE_BREWING_STEPS_TEXT));
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, IJivBrewingRecipe recipe, IFocusGroup focuses) {
		List<ItemStack> potionInputs = recipe.getPotionInputs();

		builder.addInputSlot(1, 37)
			.addItemStacks(potionInputs);

		builder.addInputSlot(24, 44)
			.addItemStacks(potionInputs);

		builder.addInputSlot(47, 37)
			.addItemStacks(potionInputs);

		builder.addInputSlot(24, 3)
			.addItemStacks(recipe.getIngredients());

		builder.addOutputSlot(81, 3)
			.add(recipe.getPotionOutput())
			.setStandardSlotBackground();
	}

	@Override
	public Identifier getIdentifier(IJivBrewingRecipe recipe) {
		return recipe.getUid();
	}

	private static class BrewingBubblesTickTimer implements ITickTimer {
		/**
		 * Similar to {@link BrewingStandScreen#BUBBLELENGTHS}
		 */
		@SuppressWarnings("JavadocReference")
		private static final int[] BUBBLE_LENGTHS = new int[]{29, 23, 18, 13, 9, 5, 0};
		private final ITickTimer internalTimer;

		public BrewingBubblesTickTimer(IGuiHelper guiHelper) {
			this.internalTimer = guiHelper.createTickTimer(14, BUBBLE_LENGTHS.length - 1, false);
		}

		@Override
		public int getValue() {
			int timerValue = this.internalTimer.getValue();
			return BUBBLE_LENGTHS[timerValue];
		}

		@Override
		public int getMaxValue() {
			return BUBBLE_LENGTHS[0];
		}
	}
}
