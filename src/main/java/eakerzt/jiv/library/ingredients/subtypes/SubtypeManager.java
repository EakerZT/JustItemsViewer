package eakerzt.jiv.library.ingredients.subtypes;

import eakerzt.jiv.api.ingredients.IIngredientTypeWithSubtypes;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.ingredients.subtypes.ISubtypeInterpreter;
import eakerzt.jiv.api.ingredients.subtypes.ISubtypeManager;
import eakerzt.jiv.api.ingredients.subtypes.UidContext;
import eakerzt.jiv.common.util.ErrorUtil;
import eakerzt.jiv.common.ingredients.TypedIngredientUtil;
import org.jspecify.annotations.Nullable;

public class SubtypeManager implements ISubtypeManager {
	private final SubtypeInterpreters interpreters;

	public SubtypeManager(SubtypeInterpreters interpreters) {
		this.interpreters = interpreters;
	}

	@Override
	@Nullable
	public <T> Object getSubtypeData(IIngredientTypeWithSubtypes<?, T> ingredientType, T ingredient, UidContext context) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		ErrorUtil.checkNotNull(context, "type");

		ISubtypeInterpreter<T> interpreter = interpreters.get(ingredientType, ingredient);
		if (interpreter == null) {
			return null;
		}
		return interpreter.getSubtypeData(ingredient, context);
	}

	@Override
	public @Nullable <B, T> Object getSubtypeData(IIngredientTypeWithSubtypes<B, T> ingredientType, ITypedIngredient<T> typedIngredient, UidContext context) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(typedIngredient, "typedIngredient");
		ErrorUtil.checkNotNull(context, "type");
		ITypedIngredient<T> checkedIngredient = TypedIngredientUtil.checkTypedIngredientFromApi(typedIngredient);

		B ingredientBase = checkedIngredient.getBaseIngredient(ingredientType);
		ISubtypeInterpreter<T> interpreter = interpreters.getFromBase(ingredientType, ingredientBase);
		if (interpreter == null) {
			return null;
		}
		T ingredient = checkedIngredient.getIngredient();
		return interpreter.getSubtypeData(ingredient, context);
	}

	@Override
	public <T, B> boolean hasSubtypes(IIngredientTypeWithSubtypes<B, T> ingredientType, T ingredient) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(ingredient, "ingredient");

		return interpreters.contains(ingredientType, ingredient);
	}
}
