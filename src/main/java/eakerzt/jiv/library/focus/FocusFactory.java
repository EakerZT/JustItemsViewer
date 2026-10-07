package eakerzt.jiv.library.focus;

import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.IFocus;
import eakerzt.jiv.api.recipe.IFocusFactory;
import eakerzt.jiv.api.recipe.IFocusGroup;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.util.ErrorUtil;

import java.util.Collection;

public class FocusFactory implements IFocusFactory {
	private final IIngredientManager ingredientManager;

	public FocusFactory(IIngredientManager ingredientManager) {
		this.ingredientManager = ingredientManager;
	}

	@Override
	public <V> IFocus<V> createFocus(RecipeIngredientRole role, IIngredientType<V> ingredientType, V ingredient) {
		ErrorUtil.checkNotNull(role, "role");
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		return Focus.createFromApi(ingredientManager, role, ingredientType, ingredient);
	}

	@Override
	public <V> IFocus<V> createFocus(RecipeIngredientRole role, ITypedIngredient<V> typedIngredient) {
		ErrorUtil.checkNotNull(role, "role");
		ErrorUtil.checkNotNull(typedIngredient, "typedIngredient");
		return Focus.createFromApi(ingredientManager, role, typedIngredient);
	}

	@Override
	public IFocusGroup createFocusGroup(Collection<? extends IFocus<?>> focuses) {
		return FocusGroup.create(focuses, ingredientManager);
	}

	@Override
	public IFocusGroup getEmptyFocusGroup() {
		return FocusGroup.EMPTY;
	}
}
