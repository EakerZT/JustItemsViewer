package eakerzt.jiv.gui.overlay.ingredients;

import eakerzt.jiv.gui.input.IRecipeFocusSource;
import eakerzt.jiv.gui.overlay.elements.IElement;

import java.util.List;
import java.util.stream.Stream;

public interface IIngredientGrid extends IRecipeFocusSource {
	boolean isMouseOver(double mouseX, double mouseY);

	int size();

	int getColumnCount();

	int getRowCount();

	void set(int firstItemIndex, List<IElement<?>> ingredientList);

	default void set(int firstItemIndex, int smoothScrollRowPixelOffset, List<IElement<?>> ingredientList) {
		set(firstItemIndex, ingredientList);
	}

	Stream<IElement<?>> getVisibleElements();

	void tick();
}
