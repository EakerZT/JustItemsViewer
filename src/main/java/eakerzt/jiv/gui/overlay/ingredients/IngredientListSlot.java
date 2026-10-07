package eakerzt.jiv.gui.overlay.ingredients;

import eakerzt.jiv.common.util.ImmutableRect2i;
import eakerzt.jiv.gui.input.ClickableIngredientInternal;
import eakerzt.jiv.gui.input.DraggableIngredientInternal;
import eakerzt.jiv.gui.input.IClickableIngredientInternal;
import eakerzt.jiv.gui.input.IDraggableIngredientInternal;
import eakerzt.jiv.gui.overlay.elements.IElement;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class IngredientListSlot {
	private final ImmutableRect2i area;
	private final int padding;
	private boolean blocked = false;
	@Nullable
	private IElement<?> element;

	public IngredientListSlot(int xPosition, int yPosition, int width, int height, int padding) {
		this.area = new ImmutableRect2i(xPosition, yPosition, width, height);
		this.padding = padding;
	}

	public Optional<IElement<?>> getOptionalElement() {
		return Optional.ofNullable(element);
	}

	public Optional<IClickableIngredientInternal<?>> getClickableIngredient() {
		return Optional.ofNullable(element)
			.map(element -> new ClickableIngredientInternal<>(element, this::isMouseOver, true, true));
	}

	public Optional<IDraggableIngredientInternal<?>> getDraggableIngredient() {
		return Optional.ofNullable(element)
			.map(element -> new DraggableIngredientInternal<>(element, area));
	}

	public void clear() {
		this.element = null;
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return (this.element != null) && area.contains(mouseX, mouseY);
	}

	public void setElement(IElement<?> element) {
		this.element = element;
	}

	public ImmutableRect2i getArea() {
		return area;
	}

	public ImmutableRect2i getRenderArea() {
		return area.insetBy(padding);
	}

	/**
	 * Set true if this ingredient is blocked by an extra gui area from a mod.
	 */
	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
	}

	public boolean isBlocked() {
		return blocked;
	}

	public int getPadding() {
		return padding;
	}
}
