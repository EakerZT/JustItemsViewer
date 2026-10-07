package eakerzt.jiv.gui.recipes;

import eakerzt.jiv.api.gui.ingredient.IRecipeSlotDrawable;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.common.gui.IngredientGridTooltipComponent;
import eakerzt.jiv.common.util.LazyMappedList;
import eakerzt.jiv.gui.input.ClickableIngredientInternal;
import eakerzt.jiv.gui.input.IClickableIngredientInternal;
import eakerzt.jiv.gui.overlay.elements.IElement;
import eakerzt.jiv.gui.overlay.elements.IngredientElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public final class InteractiveIngredientGridTooltipComponent extends IngredientGridTooltipComponent<ITypedIngredient<?>> {
	private final List<IRecipeSlotDrawable> slots;

	public InteractiveIngredientGridTooltipComponent(IGuiHelper guiHelper, List<ITypedIngredient<?>> ingredients) {
		super(ingredients);
		this.slots = new LazyMappedList<>(
			ingredients,
			ingredient -> guiHelper.createRecipeSlotDrawable(
				RecipeIngredientRole.OUTPUT,
				List.of(Optional.of(ingredient)),
				Set.of(0),
				0
			),
			MAX_VISIBLE_INGREDIENTS
		);
	}

	@Override
	protected void drawIngredient(
		GuiGraphicsExtractor guiGraphics,
		ITypedIngredient<?> ingredient,
		int index,
		int x,
		int y,
		boolean hovered
	) {
		IRecipeSlotDrawable slot = this.slots.get(index);
		slot.setPosition(x, y);
		slot.draw(guiGraphics, hovered);
	}

	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		int index = getIngredientIndexUnderMouse(mouseX, mouseY);
		if (index < 0) {
			return Stream.empty();
		}
		ITypedIngredient<?> ingredient = getIngredient(index);
		return Stream.of(createCandidateIngredient(ingredient, index));
	}

	public Optional<ITypedIngredient<?>> getTypedIngredientUnderMouse(double mouseX, double mouseY) {
		int index = getIngredientIndexUnderMouse(mouseX, mouseY);
		if (index < 0) {
			return Optional.empty();
		}
		return Optional.of(getIngredient(index));
	}

	private <T> IClickableIngredientInternal<T> createCandidateIngredient(ITypedIngredient<T> ingredient, int index) {
		IElement<T> element = new IngredientElement<>(ingredient);
		return new ClickableIngredientInternal<>(element, (mouseX, mouseY) -> getIngredientIndexUnderMouse(mouseX, mouseY) == index, false, true);
	}
}
