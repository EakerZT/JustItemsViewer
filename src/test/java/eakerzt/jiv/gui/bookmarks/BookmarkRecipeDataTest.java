package eakerzt.jiv.gui.bookmarks;

import static org.junit.jupiter.api.Assertions.*;

import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.common.ingredients.TypedIngredient;

import org.junit.jupiter.api.Test;

import java.util.List;

class BookmarkRecipeDataTest {
	@Test
	void mergedCandidatesSwitchTogetherAndWrapBack() {
		var candidates =
				List.<eakerzt.jiv.api.ingredients.ITypedIngredient<?>>of(
						TypedIngredient.createUnvalidated(TYPE, new Material("oak", 1)),
						TypedIngredient.createUnvalidated(TYPE, new Material("spruce", 1)));
		var data =
				new BookmarkRecipeData(
						List.of(
								new BookmarkRecipeData.Slot(
										1, RecipeIngredientRole.INPUT, candidates, false, 1),
								new BookmarkRecipeData.Slot(
										2, RecipeIngredientRole.INPUT, candidates, false, 1)));
		var state = new BookmarkState();
		java.util.function.Function<eakerzt.jiv.api.ingredients.ITypedIngredient<?>, Object>
				identity = i -> i.getIngredient(TYPE).orElseThrow().id();
		java.util.function.ToLongFunction<eakerzt.jiv.api.ingredients.ITypedIngredient<?>>
				quantity = i -> i.getIngredient(TYPE).orElseThrow().amount();
		data.displaySlots(state, identity, quantity).getFirst().cycleChoice(state, 1);
		var cells = data.displaySlots(state, identity, quantity);
		assertEquals(1, cells.size());
		assertEquals(
				"spruce", cells.getFirst().ingredient().getIngredient(TYPE).orElseThrow().id());
		assertEquals(2, cells.getFirst().amount());
		assertEquals(state.choices.get(1), state.choices.get(2));
		cells.getFirst().cycleChoice(state, 1);
		assertEquals(
				"oak",
				data.displaySlots(state, identity, quantity)
						.getFirst()
						.ingredient()
						.getIngredient(TYPE)
						.orElseThrow()
						.id());
	}

	private record Material(String id, long amount) {}

	private static final IIngredientType<Material> TYPE = () -> Material.class;

	private static BookmarkRecipeData.Slot slot(
			int index,
			RecipeIngredientRole role,
			String id,
			long amount,
			boolean reusable,
			double chance) {
		return new BookmarkRecipeData.Slot(
				index,
				role,
				List.of(TypedIngredient.createUnvalidated(TYPE, new Material(id, amount))),
				reusable,
				chance);
	}

	private static List<BookmarkRecipeData.DisplaySlot> display(BookmarkRecipeData data) {
		return data.displaySlots(
				new BookmarkState(),
				i -> i.getIngredient(TYPE).orElseThrow().id(),
				i -> i.getIngredient(TYPE).orElseThrow().amount());
	}

	@Test
	void duplicateRecipeInputsSumAmountsAndKeepSourceSlots() {
		var data =
				new BookmarkRecipeData(
						List.of(
								slot(0, RecipeIngredientRole.INPUT, "stick", 1, false, 1),
								slot(1, RecipeIngredientRole.INPUT, "coal", 1, false, 1),
								slot(2, RecipeIngredientRole.INPUT, "stick", 1, false, 1),
								slot(3, RecipeIngredientRole.INPUT, "stick", 1, false, 1)));
		var cells = display(data);
		assertEquals(2, cells.size());
		assertEquals(3, cells.getFirst().amount());
		assertEquals(
				List.of(0, 2, 3),
				cells.getFirst().sources().stream().map(BookmarkRecipeData.Slot::index).toList());
		assertEquals(1, cells.get(1).amount());
	}

	@Test
	void inputsOutputsToolsAndProbabilitiesRemainSeparate() {
		var data =
				new BookmarkRecipeData(
						List.of(
								slot(0, RecipeIngredientRole.INPUT, "same", 1, false, 1),
								slot(1, RecipeIngredientRole.OUTPUT, "same", 2, false, 1),
								slot(2, RecipeIngredientRole.INPUT, "same", 1, true, 1),
								slot(3, RecipeIngredientRole.OUTPUT, "same", 1, false, 0.5)));
		assertEquals(4, display(data).size());
	}

	@Test
	void duplicateOutputsSaturateInsteadOfOverflowing() {
		var data =
				new BookmarkRecipeData(
						List.of(
								slot(
										0,
										RecipeIngredientRole.OUTPUT,
										"same",
										Long.MAX_VALUE,
										false,
										1),
								slot(1, RecipeIngredientRole.OUTPUT, "same", 1, false, 1)));
		assertEquals(Long.MAX_VALUE, display(data).getFirst().amount());
	}

	@Test
	void reorderingMovesMergedInputsWithoutChangingAmountsOrOutputPosition() {
		var data =
				new BookmarkRecipeData(
						List.of(
								slot(0, RecipeIngredientRole.INPUT, "stick", 1, false, 1),
								slot(1, RecipeIngredientRole.INPUT, "coal", 1, false, 1),
								slot(2, RecipeIngredientRole.INPUT, "stick", 2, false, 1),
								slot(3, RecipeIngredientRole.OUTPUT, "torch", 4, false, 1)));
		var state = new BookmarkState();
		java.util.function.Function<eakerzt.jiv.api.ingredients.ITypedIngredient<?>, Object>
				identity = i -> i.getIngredient(TYPE).orElseThrow().id();
		java.util.function.ToLongFunction<eakerzt.jiv.api.ingredients.ITypedIngredient<?>>
				quantity = i -> i.getIngredient(TYPE).orElseThrow().amount();
		assertTrue(data.moveInput(state, 0, 1, data.displaySlots(state, identity, quantity)));
		assertEquals(List.of(1, 0, 2), state.inputOrder);
		var after = data.displaySlots(state, identity, quantity);
		assertEquals(
				List.of("torch", "coal", "stick"),
				after.stream()
						.map(i -> i.ingredient().getIngredient(TYPE).orElseThrow().id())
						.toList());
		assertEquals(3, after.getLast().amount());
		assertFalse(data.moveInput(state, 1, 3, after));
		assertFalse(data.moveInput(state, 1, 99, after));
		assertEquals(List.of(1, 0, 2), state.inputOrder);
	}
}
