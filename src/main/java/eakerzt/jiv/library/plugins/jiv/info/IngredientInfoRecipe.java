package eakerzt.jiv.library.plugins.jiv.info;

import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.vanilla.IJivIngredientInfoRecipe;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.util.StringUtil;
import eakerzt.jiv.common.ingredients.TypedIngredient;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

import java.util.Collections;
import java.util.List;

public class IngredientInfoRecipe implements IJivIngredientInfoRecipe {
	private final List<FormattedText> description;
	private final List<ITypedIngredient<?>> ingredients;

	public static <T> IJivIngredientInfoRecipe create(
		IIngredientManager ingredientManager,
		List<T> ingredients,
		IIngredientType<T> ingredientType,
		Component... descriptionComponents
	) {
		List<ITypedIngredient<T>> typedIngredients = TypedIngredient.createAndFilterInvalidNonnullList(ingredientManager, ingredientType, ingredients, true);
		List<FormattedText> descriptionLines = StringUtil.expandNewlines(descriptionComponents);
		return new IngredientInfoRecipe(typedIngredients, descriptionLines);
	}

	private IngredientInfoRecipe(List<? extends ITypedIngredient<?>> ingredients, List<FormattedText> description) {
		this.description = description;
		this.ingredients = Collections.unmodifiableList(ingredients);
	}

	@Override
	public List<FormattedText> getDescription() {
		return description;
	}

	@Override
	public List<ITypedIngredient<?>> getIngredients() {
		return ingredients;
	}
}
