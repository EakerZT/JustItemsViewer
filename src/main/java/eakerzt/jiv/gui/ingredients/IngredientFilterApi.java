package eakerzt.jiv.gui.ingredients;

import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.runtime.IIngredientFilter;
import eakerzt.jiv.common.util.ErrorUtil;
import eakerzt.jiv.gui.filter.IFilterTextSource;

import java.util.List;

public class IngredientFilterApi implements IIngredientFilter {
	private final IngredientFilter ingredientFilter;
	private final IFilterTextSource filterTextSource;

	public IngredientFilterApi(IngredientFilter ingredientFilter, IFilterTextSource filterTextSource) {
		this.ingredientFilter = ingredientFilter;
		this.filterTextSource = filterTextSource;
	}

	@Override
	public String getFilterText() {
		return filterTextSource.getFilterText();
	}

	@Override
	public void setFilterText(String filterText) {
		ErrorUtil.checkNotNull(filterText, "filterText");
		filterTextSource.setFilterText(filterText);
	}

	@Override
	public <T> List<T> getFilteredIngredients(IIngredientType<T> ingredientType) {
		return ingredientFilter.getFilteredIngredients(ingredientType);
	}
}
