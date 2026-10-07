package eakerzt.jiv.gui.input;

import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.common.util.ImmutableRect2i;
import eakerzt.jiv.gui.overlay.elements.IElement;

public interface IDraggableIngredientInternal<T> {
	ITypedIngredient<T> getTypedIngredient();

	IElement<T> getElement();

	ImmutableRect2i getArea();
}
