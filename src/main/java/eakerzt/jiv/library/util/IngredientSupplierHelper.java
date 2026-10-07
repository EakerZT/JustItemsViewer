package eakerzt.jiv.library.util;

import eakerzt.jiv.api.recipe.category.IRecipeCategory;
import eakerzt.jiv.common.util.ErrorUtil;
import eakerzt.jiv.library.focus.FocusGroup;
import eakerzt.jiv.library.gui.recipes.supplier.builder.IngredientSupplierBuilder;
import eakerzt.jiv.library.ingredients.RecipeIngredientSupplier;
import eakerzt.jiv.library.ingredients.IIngredientManagerInternal;
import net.minecraft.util.context.ContextMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class IngredientSupplierHelper {
	private static final Logger LOGGER = LogManager.getLogger();

	private IngredientSupplierHelper() {
	}

	public static <T> RecipeIngredientSupplier getIngredientSupplier(T recipe, IRecipeCategory<T> recipeCategory, IIngredientManagerInternal ingredientManager, ContextMap contextMap) {
		IngredientSupplierBuilder builder = new IngredientSupplierBuilder(ingredientManager, contextMap);
		if (!recipeCategory.isHandled(recipe)) {
			return builder.buildIngredientSupplier();
		}
		try {
			recipeCategory.setRecipe(builder, recipe, FocusGroup.EMPTY);
		} catch (RuntimeException | LinkageError e) {
			String recipeInfo = ErrorUtil.getRecipeInfo(recipeCategory, recipe);
			LOGGER.error("Found a broken recipe, failed to setRecipe with RecipeLayoutBuilder:\n{}", recipeInfo, e);
		}

		return builder.buildIngredientSupplier();
	}
}
