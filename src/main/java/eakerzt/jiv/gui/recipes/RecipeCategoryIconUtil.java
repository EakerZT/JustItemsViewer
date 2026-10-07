package eakerzt.jiv.gui.recipes;

import eakerzt.jiv.api.gui.drawable.IDrawable;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.IRecipeManager;
import eakerzt.jiv.api.recipe.category.IRecipeCategory;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import eakerzt.jiv.common.gui.JivGuiColors;
import eakerzt.jiv.common.gui.JivGuiColors.GuiColor;
import eakerzt.jiv.common.gui.elements.DrawableText;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class RecipeCategoryIconUtil {
	public static <T> IDrawable create(
		IRecipeCategory<T> recipeCategory,
		IRecipeManager recipeManager,
		IGuiHelper guiHelper
	) {
		IDrawable icon = recipeCategory.getIcon();
		if (icon != null) {
			return icon;
		}
		IRecipeType<T> recipeType = recipeCategory.getRecipeType();
		Optional<ITypedIngredient<?>> firstCatalyst = recipeManager.createCraftingStationLookup(recipeType)
			.get()
			.findFirst();

		if (firstCatalyst.isPresent()) {
			ITypedIngredient<?> ingredient = firstCatalyst.get();
			return guiHelper.createDrawableIngredient(ingredient);
		} else {
			Component title = recipeCategory.getTitle();
			String text = title.getString().substring(0, 2);
			return new DrawableText(text, 16, 16, JivGuiColors.getColor(GuiColor.RECIPE_CATEGORY_ICON_TEXT));
		}
	}
}
