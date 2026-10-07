package eakerzt.jiv.gui.recipes;

import eakerzt.jiv.api.gui.IRecipeLayoutDrawable;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotView;
import eakerzt.jiv.api.gui.inputs.RecipeSlotUnderMouse;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.IRecipeManager;
import eakerzt.jiv.gui.input.ClickableIngredientInternal;
import eakerzt.jiv.gui.input.IClickableIngredientInternal;
import eakerzt.jiv.common.input.IMouseOverable;
import eakerzt.jiv.gui.overlay.elements.IElement;
import eakerzt.jiv.gui.overlay.elements.IngredientElement;
import eakerzt.jiv.gui.overlay.elements.TagIngredientElement;
import net.minecraft.tags.TagKey;

import java.util.Optional;
import java.util.function.BooleanSupplier;

public final class RecipeSlotClickTargetFactory {
	private final IRecipeManager recipeManager;
	private final BooleanSupplier isRecipeCyclingPaused;

	public RecipeSlotClickTargetFactory(IRecipeManager recipeManager, BooleanSupplier isRecipeCyclingPaused) {
		this.recipeManager = recipeManager;
		this.isRecipeCyclingPaused = isRecipeCyclingPaused;
	}

	public Optional<IClickableIngredientInternal<?>> create(
		IRecipeLayoutDrawable<?> recipeLayout,
		double mouseX,
		double mouseY
	) {
		return recipeLayout.getSlotUnderMouse(mouseX, mouseY)
			.flatMap(slotUnderMouse -> create(
				slotUnderMouse,
				createMouseOverable(recipeLayout, slotUnderMouse)
			));
	}

	Optional<IClickableIngredientInternal<?>> create(
		RecipeSlotUnderMouse slotUnderMouse,
		IMouseOverable mouseOverable
	) {
		return slotUnderMouse.slot()
			.getDisplayedIngredient()
			.map(ingredient -> create(slotUnderMouse.slot(), ingredient, mouseOverable));
	}

	private <T> IClickableIngredientInternal<T> create(
		IRecipeSlotView slot,
		ITypedIngredient<T> ingredient,
		IMouseOverable mouseOverable
	) {
		IElement<T> element = createElement(slot, ingredient);
		return new ClickableIngredientInternal<>(element, mouseOverable, false, true);
	}

	static IMouseOverable createMouseOverable(
		IRecipeLayoutDrawable<?> recipeLayout,
		RecipeSlotUnderMouse expected
	) {
		return (mouseX, mouseY) -> recipeLayout.getSlotUnderMouse(mouseX, mouseY)
			.map(RecipeSlotUnderMouse::slot)
			.filter(slot -> slot == expected.slot())
			.isPresent();
	}

	private <T> IElement<T> createElement(IRecipeSlotView slot, ITypedIngredient<T> ingredient) {
		return getNavigableTag(slot)
			.<IElement<T>>map(tagKey -> new TagIngredientElement<>(
				ingredient,
				tagKey,
				this.recipeManager,
				this.isRecipeCyclingPaused
			))
			.orElseGet(() -> new IngredientElement<>(ingredient));
	}

	private Optional<TagKey<?>> getNavigableTag(IRecipeSlotView slot) {
		if (this.isRecipeCyclingPaused.getAsBoolean()) {
			return Optional.empty();
		}
		Optional<TagKey<?>> tagKey = slot.getTagKey();
		if (tagKey.isEmpty()) {
			return Optional.empty();
		}
		boolean hasMultipleIngredients = slot.getDisplayedIngredients()
			.limit(2)
			.count() > 1;
		if (hasMultipleIngredients) {
			return tagKey;
		}
		return Optional.empty();
	}
}
