package eakerzt.jiv.common.ingredients;

import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.ingredients.ITypedIngredient;

import java.util.Optional;

public interface ITypedIngredientFactory {
	<T> Optional<ITypedIngredient<T>> createTypedIngredient(IIngredientType<T> ingredientType, T ingredient, boolean normalize);

	<T> ITypedIngredient<T> checkTypedIngredientFromApi(ITypedIngredient<T> typedIngredient);
}
