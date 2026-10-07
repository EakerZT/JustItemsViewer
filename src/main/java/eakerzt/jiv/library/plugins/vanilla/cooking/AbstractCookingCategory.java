package eakerzt.jiv.library.plugins.vanilla.cooking;

import com.mojang.serialization.Codec;
import eakerzt.jiv.api.gui.builder.IRecipeLayoutBuilder;
import eakerzt.jiv.api.gui.placement.HorizontalAlignment;
import eakerzt.jiv.api.gui.placement.VerticalAlignment;
import eakerzt.jiv.api.gui.widgets.IRecipeExtrasBuilder;
import eakerzt.jiv.api.helpers.ICodecHelper;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.api.recipe.IFocusGroup;
import eakerzt.jiv.api.recipe.IRecipeManager;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.recipe.category.AbstractRecipeCategory;
import eakerzt.jiv.api.recipe.types.IRecipeHolderType;
import eakerzt.jiv.common.gui.JivGuiColors;
import eakerzt.jiv.common.gui.JivGuiColors.GuiColor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

public abstract class AbstractCookingCategory<T extends AbstractCookingRecipe> extends AbstractRecipeCategory<RecipeHolder<T>> {
	protected final int regularCookTime;

	public AbstractCookingCategory(IGuiHelper guiHelper, IRecipeHolderType<T> recipeType, Block icon, String translationKey, int regularCookTime) {
		this(guiHelper, recipeType, icon, translationKey, regularCookTime, 82, 54);
	}

	public AbstractCookingCategory(IGuiHelper guiHelper, IRecipeHolderType<T> recipeType, Block icon, String translationKey, int regularCookTime, int width, int height) {
		super(
			recipeType,
			Component.translatable(translationKey),
			guiHelper.createDrawableItemLike(icon),
			width,
			height
		);
		this.regularCookTime = regularCookTime;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<T> recipeHolder, IFocusGroup focuses) {
		T recipe = recipeHolder.value();
		RecipeDisplay display = recipe.display().getFirst();
		if (display instanceof FurnaceRecipeDisplay furnaceRecipeDisplay) {
			FuelDisplay fuelDisplay = FuelDisplay.create(furnaceRecipeDisplay.fuel());
			int outputX = getWidth() - 21;
			int outputY = 19;
			if (fuelDisplay.output() != null) {
				outputY = 5;
			}

			builder.addInputSlot(1, 1)
				.setStandardSlotBackground()
				.add(furnaceRecipeDisplay.ingredient());

			RecipeIngredientRole fuelRole = RecipeIngredientRole.RENDER_ONLY;
			if (fuelDisplay.isSpecific()) {
				fuelRole = RecipeIngredientRole.INPUT;
			}
			builder.addSlot(fuelRole, 1, 37)
				.setStandardSlotBackground()
				.add(fuelDisplay.input());

			builder.addOutputSlot(outputX, outputY)
				.setOutputSlotBackground()
				.add(furnaceRecipeDisplay.result());

			if (fuelDisplay.output() != null) {
				builder.addOutputSlot(outputX, 33)
					.setOutputSlotBackground()
					.add(fuelDisplay.output());
			}
		}
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<T> recipeHolder, IFocusGroup focuses) {
		T recipe = recipeHolder.value();
		RecipeDisplay display = recipe.display().getFirst();
		if (display instanceof FurnaceRecipeDisplay furnaceRecipeDisplay) {
			boolean hasFuelOutput = FuelDisplay.create(furnaceRecipeDisplay.fuel()).output() != null;
			int cookTime = furnaceRecipeDisplay.duration();
			if (cookTime <= 0) {
				cookTime = regularCookTime;
			}
			builder.addAnimatedRecipeArrowWidget(cookTime)
				.setPosition(getArrowX(), 17);
			builder.addAnimatedRecipeFlameWidget(300)
				.setPosition(1, 20);

			addExperience(builder, furnaceRecipeDisplay, hasFuelOutput);
			addCookTime(builder, furnaceRecipeDisplay);
		}
	}

	private int getArrowX() {
		if (getWidth() == 82) {
			return 26;
		}
		return (getWidth() - 24) / 2;
	}

	protected void addExperience(IRecipeExtrasBuilder builder, FurnaceRecipeDisplay recipe) {
		addExperience(builder, recipe, false);
	}

	protected void addExperience(IRecipeExtrasBuilder builder, FurnaceRecipeDisplay recipe, boolean hasFuelOutput) {
		float experience = recipe.experience();
		if (experience > 0) {
			Component experienceString = Component.translatable("gui.jiv.category.smelting.experience", experience);
			if (hasFuelOutput) {
				builder.addText(experienceString, getWidth() - 42, 10)
					.setPosition(20, 0)
					.setTextAlignment(HorizontalAlignment.CENTER)
					.setColor(JivGuiColors.getColor(GuiColor.RECIPE_COOKING_EXPERIENCE_TEXT));
			} else {
				builder.addText(experienceString, getWidth() - 20, 10)
					.setPosition(0, 0, getWidth(), getHeight(), HorizontalAlignment.RIGHT, VerticalAlignment.TOP)
					.setTextAlignment(HorizontalAlignment.RIGHT)
					.setColor(JivGuiColors.getColor(GuiColor.RECIPE_COOKING_EXPERIENCE_TEXT));
			}
		}
	}

	protected void addCookTime(IRecipeExtrasBuilder builder, FurnaceRecipeDisplay recipe) {
		int cookTime = recipe.duration();
		if (cookTime <= 0) {
			cookTime = regularCookTime;
		}
		if (cookTime > 0) {
			int cookTimeSeconds = cookTime / 20;
			Component timeString = Component.translatable("gui.jiv.category.smelting.time.seconds", cookTimeSeconds);
			builder.addText(timeString, getWidth() - 42, 10)
				.setPosition(0, 0, getWidth(), getHeight(), HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM)
				.setTextAlignment(HorizontalAlignment.CENTER)
				.setTextAlignment(VerticalAlignment.BOTTOM)
				.setColor(JivGuiColors.getColor(GuiColor.RECIPE_COOKING_TIME_TEXT));
		}
	}

	@Override
	public final Codec<RecipeHolder<T>> getCodec(ICodecHelper codecHelper, IRecipeManager recipeManager) {
		return codecHelper.getRecipeHolderCodec();
	}

	private record FuelDisplay(SlotDisplay input, @Nullable SlotDisplay output) {
		public static FuelDisplay create(SlotDisplay fuel) {
			if (fuel instanceof SlotDisplay.WithRemainder withRemainder) {
				return new FuelDisplay(withRemainder.input(), withRemainder.remainder());
			}
			return new FuelDisplay(fuel, null);
		}

		public boolean isSpecific() {
			return !(input instanceof SlotDisplay.AnyFuel);
		}
	}
}
