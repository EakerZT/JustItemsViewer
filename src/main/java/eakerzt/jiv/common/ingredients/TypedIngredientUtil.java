package eakerzt.jiv.common.ingredients;

import eakerzt.jiv.api.ingredients.IIngredientHelper;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.ingredients.itemStacks.TypedItemStack;
import eakerzt.jiv.common.platform.Services;
import eakerzt.jiv.common.util.ErrorUtil;
import org.jspecify.annotations.Nullable;

public final class TypedIngredientUtil {
	private TypedIngredientUtil() {
	}

	/**
	 * Checks that a typed ingredient passed to an API was created by JIV.
	 * Unknown implementations are defensively copied, but the ingredient is not validated.
	 *
	 * @throws IllegalArgumentException for unknown implementations in a development environment
	 */
	public static <T> ITypedIngredient<T> checkTypedIngredientFromApi(ITypedIngredient<T> typedIngredient) {
		ErrorUtil.checkNotNull(typedIngredient, "typedIngredient");
		if (isKnownType(typedIngredient)) {
			return typedIngredient;
		}
		checkUnknownImplementation(typedIngredient);

		IIngredientManager ingredientManager = Internal.getJivRuntime().getIngredientManager();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(typedIngredient.getType());
		return copyTypedIngredient(ingredientHelper, typedIngredient);
	}

	/**
	 * Returns JIV-created values unchanged, and defensively copies unknown implementations without validating them.
	 */
	public static <T> ITypedIngredient<T> checkTypedIngredientFromApi(
		IIngredientManager ingredientManager,
		ITypedIngredient<T> typedIngredient
	) {
		ErrorUtil.checkNotNull(ingredientManager, "ingredientManager");
		ErrorUtil.checkNotNull(typedIngredient, "typedIngredient");
		if (isKnownType(typedIngredient)) {
			return typedIngredient;
		}
		checkUnknownImplementation(typedIngredient);

		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(typedIngredient.getType());
		return copyTypedIngredient(ingredientHelper, typedIngredient);
	}

	/**
	 * Returns JIV-created values unchanged, and defensively copies unknown implementations without validating them.
	 */
	public static <T> ITypedIngredient<T> checkTypedIngredientFromApi(
		IIngredientHelper<T> ingredientHelper,
		ITypedIngredient<T> typedIngredient
	) {
		ErrorUtil.checkNotNull(ingredientHelper, "ingredientHelper");
		ErrorUtil.checkNotNull(typedIngredient, "typedIngredient");
		if (isKnownType(typedIngredient)) {
			return typedIngredient;
		}
		checkUnknownImplementation(typedIngredient);

		return copyTypedIngredient(ingredientHelper, typedIngredient);
	}

	/**
	 * Returns JIV-created values unchanged, and defensively copies and validates unknown implementations.
	 *
	 * @return the checked typed ingredient, or null when an unknown implementation contains an invalid ingredient
	 */
	public static <T> @Nullable ITypedIngredient<T> checkAndValidateTypedIngredientFromApi(
		IIngredientManager ingredientManager,
		ITypedIngredient<T> typedIngredient
	) {
		ErrorUtil.checkNotNull(ingredientManager, "ingredientManager");
		ErrorUtil.checkNotNull(typedIngredient, "typedIngredient");
		if (isKnownType(typedIngredient)) {
			return typedIngredient;
		}
		checkUnknownImplementation(typedIngredient);

		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(typedIngredient.getType());
		T ingredientCopy = ingredientHelper.copyIngredient(typedIngredient.getIngredient());
		return TypedIngredient.createAndFilterInvalid(ingredientHelper, ingredientHelper.getIngredientType(), ingredientCopy, false);
	}

	private static <T> ITypedIngredient<T> copyTypedIngredient(
		IIngredientHelper<T> ingredientHelper,
		ITypedIngredient<T> typedIngredient
	) {
		T ingredientCopy = ingredientHelper.copyIngredient(typedIngredient.getIngredient());
		return TypedIngredient.createUnvalidated(ingredientHelper.getIngredientType(), ingredientCopy);
	}

	private static void checkUnknownImplementation(ITypedIngredient<?> typedIngredient) {
		if (Services.PLATFORM.getModHelper().isInDev()) {
			throw new IllegalArgumentException(
				"Invalid ITypedIngredient implementation: " + typedIngredient.getClass().getName() + ". " +
					"Create typed ingredients with IIngredientManager#createTypedIngredient(...) " +
					"instead of implementing ITypedIngredient."
			);
		}
	}

	private static boolean isKnownType(ITypedIngredient<?> typedIngredient) {
		return typedIngredient instanceof TypedIngredient<?> ||
			typedIngredient instanceof TypedItemStack;
	}
}
