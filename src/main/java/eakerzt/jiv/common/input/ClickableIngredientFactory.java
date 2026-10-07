package eakerzt.jiv.common.input;

import eakerzt.jiv.api.gui.builder.IClickableIngredientFactory;
import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.runtime.IClickableIngredient;
import eakerzt.jiv.common.ingredients.ITypedIngredientFactory;
import eakerzt.jiv.common.util.ImmutableRect2i;
import net.minecraft.client.renderer.Rect2i;

import java.util.Optional;

public class ClickableIngredientFactory implements IClickableIngredientFactory {
	private final ITypedIngredientFactory typedIngredientFactory;

	public ClickableIngredientFactory(ITypedIngredientFactory typedIngredientFactory) {
		this.typedIngredientFactory = typedIngredientFactory;
	}

	@Override
	public <T> IBuilder<T> createBuilder(ITypedIngredient<T> value) {
		ITypedIngredient<T> checkedValue = typedIngredientFactory.checkTypedIngredientFromApi(value);
		return new WithIngredient<>(checkedValue);
	}

	@Override
	public <T> IBuilder<T> createBuilder(IIngredientType<T> ingredientType, T ingredient) {
		return typedIngredientFactory.createTypedIngredient(ingredientType, ingredient, false)
			.<IBuilder<T>>map(WithIngredient::new)
			.orElse(WithoutIngredient.getInstance());
	}

	private static class WithIngredient<T> implements IBuilder<T> {
		private final ITypedIngredient<T> ingredient;

		private WithIngredient(ITypedIngredient<T> ingredient) {
			this.ingredient = ingredient;
		}

		@Override
		public Optional<IClickableIngredient<T>> buildWithArea(int x, int y, int width, int height) {
			ImmutableRect2i area = new ImmutableRect2i(x, y, width, height);
			ClickableIngredient<T> result = new ClickableIngredient<>(ingredient, area);
			return Optional.of(result);
		}

		@Override
		public Optional<IClickableIngredient<T>> buildWithArea(Rect2i area) {
			ImmutableRect2i immutableArea = new ImmutableRect2i(area);
			ClickableIngredient<T> result = new ClickableIngredient<>(ingredient, immutableArea);
			return Optional.of(result);
		}
	}

	private static class WithoutIngredient<T> implements IBuilder<T> {
		public static final WithoutIngredient<?> INSTANCE = new WithoutIngredient<>();

		public static <T> IBuilder<T> getInstance() {
			@SuppressWarnings("unchecked")
			IBuilder<T> cast = (IBuilder<T>) INSTANCE;
			return cast;
		}

		private WithoutIngredient() {}

		@Override
		public Optional<IClickableIngredient<T>> buildWithArea(int x, int y, int width, int height) {
			return Optional.empty();
		}

		@Override
		public Optional<IClickableIngredient<T>> buildWithArea(Rect2i area) {
			return Optional.empty();
		}
	}
}
