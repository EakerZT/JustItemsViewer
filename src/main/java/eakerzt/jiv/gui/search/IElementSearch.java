package eakerzt.jiv.gui.search;

import eakerzt.jiv.api.ingredients.IIngredientHelper;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.gui.ingredients.IListElement;
import eakerzt.jiv.gui.ingredients.IListElementInfo;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

public interface IElementSearch {
	<T> void add(IListElementInfo<T> info, IIngredientManager ingredientManager);

	Collection<IListElement<?>> getAllIngredients();

	Set<IListElement<?>> getSearchResults(ElementPrefixParser.TokenInfo tokenInfo);

	@Nullable
	<T> IListElement<T> findElement(ITypedIngredient<T> ingredient, IIngredientHelper<T> ingredientHelper);

	void logStatistics();
}
