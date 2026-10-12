package eakerzt.jiv.gui.bookmarks;

import eakerzt.jiv.api.gui.ingredient.IRecipeSlotView;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotsView;
import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.common.ingredients.TypedIngredient;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class BookmarkCraftTransferSlotsTest {
	private static final IIngredientType<String> TYPE = () -> String.class;

	private static IRecipeSlotView slot(RecipeIngredientRole role, List<ITypedIngredient<?>> ingredients, boolean reusable) {
		return (IRecipeSlotView) Proxy.newProxyInstance(IRecipeSlotView.class.getClassLoader(),
				new Class<?>[]{IRecipeSlotView.class}, (proxy, method, args) -> switch (method.getName()) {
					case "getRole" -> role;
					case "getAllIngredients", "getDisplayedIngredients" -> ingredients.stream();
					case "getAllIngredientsList" -> ingredients;
					case "getDisplayedIngredient" -> ingredients.stream().findFirst();
					case "getTagKey", "getSlotName" -> Optional.empty();
					case "isNonConsumed" -> reusable;
					case "getChance" -> 1.0;
					default -> throw new UnsupportedOperationException(method.getName());
				});
	}

	@Test void executionPinsSavedSubstitutionsWithoutMovingEmptySlotsOrChangingTheSource() {
		var oak = TypedIngredient.createUnvalidated(TYPE, "oak");
		var spruce = TypedIngredient.createUnvalidated(TYPE, "spruce");
		var chest = TypedIngredient.createUnvalidated(TYPE, "chest");
		List<ITypedIngredient<?>> choices = List.of(oak, spruce);
		var empty = slot(RecipeIngredientRole.INPUT, List.of(), false);
		var input = slot(RecipeIngredientRole.INPUT, choices, true);
		var output = slot(RecipeIngredientRole.OUTPUT, List.of(chest), false);
		IRecipeSlotsView source = () -> List.of(empty, input, output);
		var data = new BookmarkRecipeData(List.of(new BookmarkRecipeData.Slot(1, RecipeIngredientRole.INPUT, choices, true, 1)));
		var state = new BookmarkState();
		state.choices.put(1, 1);
		var transfer = new BookmarkTransferSlots(source, data, state);
		assertSame(empty, transfer.getSlotViews().get(0));
		assertSame(output, transfer.getSlotViews().get(2));
		assertEquals(List.of(spruce), transfer.getSlotViews().get(1).getAllIngredients().toList());
		assertEquals(Optional.of(spruce), transfer.getSlotViews().get(1).getDisplayedIngredient());
		assertTrue(transfer.getSlotViews().get(1).isNonConsumed());
		assertEquals(choices, input.getAllIngredients().toList());
	}
}
