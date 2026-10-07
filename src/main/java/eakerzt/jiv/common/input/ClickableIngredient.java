package eakerzt.jiv.common.input;

import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.runtime.IClickableIngredient;
import eakerzt.jiv.common.util.ErrorUtil;
import eakerzt.jiv.common.util.ImmutableRect2i;
import net.minecraft.client.renderer.Rect2i;

public class ClickableIngredient<V> implements IClickableIngredient<V> {
	private final ITypedIngredient<V> value;
	private final ImmutableRect2i area;

	public ClickableIngredient(ITypedIngredient<V> value, ImmutableRect2i area) {
		ErrorUtil.checkNotNull(value, "value");
		this.value = value;
		this.area = area;
	}

	@Override
	public ITypedIngredient<V> getTypedIngredient() {
		return value;
	}

	@Override
	public Rect2i getArea() {
		return area.toMutable();
	}
}
