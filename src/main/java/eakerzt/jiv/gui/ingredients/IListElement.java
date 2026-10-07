package eakerzt.jiv.gui.ingredients;

import eakerzt.jiv.api.ingredients.ITypedIngredient;

public interface IListElement<V> {
	ITypedIngredient<V> getTypedIngredient();

	int getSortedIndex();

	void setSortedIndex(int sortIndex);

	int getCreatedIndex();

	boolean isVisible();

	void setVisible(boolean visible);
}
