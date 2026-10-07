package eakerzt.jiv.library.plugins.vanilla.cooking.fuel;

import eakerzt.jiv.api.gui.builder.IRecipeLayoutBuilder;
import eakerzt.jiv.api.gui.drawable.IDrawable;
import eakerzt.jiv.api.gui.placement.HorizontalAlignment;
import eakerzt.jiv.api.gui.placement.VerticalAlignment;
import eakerzt.jiv.api.gui.widgets.IRecipeExtrasBuilder;
import eakerzt.jiv.api.recipe.IFocusGroup;
import eakerzt.jiv.api.recipe.category.AbstractRecipeCategory;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import eakerzt.jiv.api.recipe.vanilla.IJivFuelingRecipe;
import eakerzt.jiv.common.gui.JivGuiColors;
import eakerzt.jiv.common.gui.JivGuiColors.GuiColor;
import eakerzt.jiv.common.gui.textures.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.text.NumberFormat;

public abstract class AbstractFuelCategory extends AbstractRecipeCategory<IJivFuelingRecipe> {
	private final int burnDivisor;

	protected AbstractFuelCategory(
		Textures textures,
		IRecipeType<IJivFuelingRecipe> recipeType,
		Component title,
		IDrawable icon,
		int burnDivisor
	) {
		super(
			recipeType,
			title,
			new IconWithFlameOverlay(textures, icon),
			getMaxWidth(),
			34
		);
		if (burnDivisor <= 0) {
			throw new IllegalArgumentException("burnDivisor must be greater than 0");
		}
		this.burnDivisor = burnDivisor;
	}

	private static int getMaxWidth() {
		// width of the recipe depends on the text, which is different in each language
		Minecraft minecraft = Minecraft.getInstance();
		Font fontRenderer = minecraft.font;
		Component maxSmeltCountText = createSmeltCountText(10000000 * 200);
		int maxStringWidth = fontRenderer.width(maxSmeltCountText.getString());
		int textPadding = 20;
		return 18 + textPadding + maxStringWidth;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, IJivFuelingRecipe recipe, IFocusGroup focuses) {
		builder.addInputSlot(1, 17)
			.setStandardSlotBackground()
			.addItemStacks(recipe.getInputs());
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, IJivFuelingRecipe recipe, IFocusGroup focuses) {
		int burnTime = recipe.getBurnTime() / burnDivisor;
		builder.addAnimatedRecipeFlameWidget(burnTime)
			.setPosition(1, 0);

		Component smeltCountText = createSmeltCountText(recipe.getBurnTime());
		builder.addText(smeltCountText, getWidth() - 20, getHeight())
			.setPosition(20, 0)
			.setTextAlignment(HorizontalAlignment.CENTER)
			.setTextAlignment(VerticalAlignment.CENTER)
			.setColor(JivGuiColors.getColor(GuiColor.RECIPE_FUEL_SMELT_COUNT_TEXT));
	}

	public static Component createSmeltCountText(int burnTime) {
		if (burnTime == 200) {
			return Component.translatable("gui.jiv.category.fuel.smeltCount.single");
		} else {
			NumberFormat numberInstance = NumberFormat.getNumberInstance();
			numberInstance.setMaximumFractionDigits(2);
			String smeltCount = numberInstance.format(burnTime / 200f);
			return Component.translatable("gui.jiv.category.fuel.smeltCount", smeltCount);
		}
	}

	@Override
	public @Nullable Identifier getIdentifier(IJivFuelingRecipe recipe) {
		return null;
	}

	private static class IconWithFlameOverlay implements IDrawable {
		private final IDrawable icon;
		private final IDrawable flameIcon;

		public IconWithFlameOverlay(Textures textures, IDrawable icon) {
			this.icon = icon;
			this.flameIcon = textures.getFlameIcon();
		}

		@Override
		public int getWidth() {
			return 16;
		}

		@Override
		public int getHeight() {
			return 16;
		}

		@Override
		public void draw(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset) {
			icon.draw(guiGraphics, xOffset, yOffset);

			var poseStack = guiGraphics.pose();
			poseStack.pushMatrix();
			{
				poseStack.translate(8 + xOffset, 8 + yOffset);
				poseStack.scale(0.5f, 0.5f);
				flameIcon.draw(guiGraphics);
			}
			poseStack.popMatrix();
		}
	}
}
