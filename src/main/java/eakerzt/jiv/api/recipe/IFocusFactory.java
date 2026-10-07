package eakerzt.jiv.api.recipe;

import eakerzt.jiv.api.helpers.IJivHelpers;
import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;

/**
 * {@link IFocusFactory} helps with creating {@link IFocus} for JIV.
 * Get an instance from {@link IJivHelpers#getFocusFactory()}.
 */
@ApiStatus.NonExtendable
public interface IFocusFactory {
	/**
	 * Returns a new focus.
	 *
	 * @since 9.3.0
	 */
	<V> IFocus<V> createFocus(RecipeIngredientRole role, IIngredientType<V> ingredientType, V ingredient);

	/**
	 * Returns a new focus.
	 *
	 * @since 11.5.0
	 */
	<V> IFocus<V> createFocus(RecipeIngredientRole role, ITypedIngredient<V> typedIngredient);

	/**
	 * Returns a new focus group.
	 *
	 * @since 11.5.0
	 */
	IFocusGroup createFocusGroup(Collection<? extends IFocus<?>> focuses);

	/**
	 * Returns an empty focus group.
	 *
	 * @since 11.5.0
	 */
	IFocusGroup getEmptyFocusGroup();
}
