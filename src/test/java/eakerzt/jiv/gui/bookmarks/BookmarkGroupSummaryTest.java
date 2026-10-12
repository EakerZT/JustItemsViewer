package eakerzt.jiv.gui.bookmarks;

import eakerzt.jiv.gui.bookmarks.planning.BookmarkGroupSummary;
import eakerzt.jiv.gui.bookmarks.planning.RecipeChain;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BookmarkGroupSummaryTest {
	@Test void previewModesUseDifferentFinalInventoryRules() {
		var recipes = List.of(recipe("plate", "iron", 2, "plate", 1));
		var stock = Map.<Object, Long>of("plate", 1L, "iron", 2L);
		var all = BookmarkGroupSummary.calculate(recipes, true, Map.of(), stock,
				eakerzt.jiv.gui.bookmarks.planning.BookmarkCraftPlan.Mode.ALL);
		var missing = BookmarkGroupSummary.calculate(recipes, true, Map.of(), stock,
				eakerzt.jiv.gui.bookmarks.planning.BookmarkCraftPlan.Mode.MISSING);
		assertEquals(1L, all.calculation().crafts().get("plate"));
		assertEquals(0L, missing.calculation().crafts().get("plate"));
		assertEquals(Map.of("iron", 2L), all.covered());
		assertTrue(missing.covered().isEmpty());
		assertTrue(missing.missing().isEmpty());
		assertEquals(Map.of("iron", 2L), missing.required());
	}

	private static RecipeChain.Recipe recipe(String id, String input, long amount, String output, long yield) {
		return new RecipeChain.Recipe(id, List.of(new RecipeChain.Material(input, amount)),
				List.of(new RecipeChain.Material(output, yield)), 1);
	}

	@Test void plainShoppingListPreservesBothAvailableAndMissingAmountsOfTheSameMaterial() {
		var result = BookmarkGroupSummary.calculate(List.of(), false,
				Map.of("iron", 64L, "glass", 32L), Map.of("iron", 10L, "glass", 48L));
		assertEquals(Map.of("iron", 54L), result.missing());
		assertEquals(Map.of("iron", 10L, "glass", 32L), result.covered());
		assertTrue(result.calculation().outputs().isEmpty());
	}

	@Test void mixedTargetsAndRecipesCannotSpendTheSameInventoryTwice() {
		var result = BookmarkGroupSummary.calculate(List.of(recipe("plate", "iron", 2, "plate", 1)), false,
				Map.of("iron", 3L), Map.of("iron", 4L));
		assertEquals(Map.of("iron", 5L), result.required());
		assertEquals(Map.of("iron", 1L), result.missing());
		assertEquals(Map.of("iron", 4L), result.covered());
	}

	@Test void intermediateInventoryReducesUpstreamCraftsWithoutIncreasingTarget() {
		var result = BookmarkGroupSummary.calculate(List.of(
				recipe("gear", "plate", 3, "gear", 1), recipe("plate", "iron", 1, "plate", 4)), true,
				Map.of(), Map.of("plate", 3L, "gear", 64L));
		assertTrue(result.missing().isEmpty());
		assertEquals(0L, result.calculation().crafts().get("plate"));
		assertEquals(1L, result.calculation().crafts().get("gear"));
		assertEquals(Map.of("gear", 1L), result.calculation().outputs());
		assertEquals(Map.of("plate", 3L), result.covered());
		assertTrue(result.craftingNeeded().isEmpty());
	}

	@Test void disabledLinkingKeepsEachRecipesOwnRequirements() {
		var result = BookmarkGroupSummary.calculate(List.of(
				recipe("gear", "plate", 3, "gear", 1), recipe("plate", "iron", 1, "plate", 4)), false,
				Map.of(), Map.of("plate", 3L));
		assertEquals(Map.of("iron", 1L), result.missing());
		assertEquals(1L, result.calculation().crafts().get("plate"));
		assertTrue(result.craftingNeeded().isEmpty());
	}

	@Test void craftingNeededExcludesFinalProductsAndRoundedBatchLeftovers() {
		var result = BookmarkGroupSummary.calculate(List.of(
				recipe("gear", "plate", 3, "gear", 1), recipe("plate", "iron", 1, "plate", 4)), true,
				Map.of(), Map.of());
		assertEquals(Map.of("plate", 3L), result.craftingNeeded());
		assertEquals(Map.of("plate", Map.of("plate", 3L)), result.calculation().neededByRecipe());
		assertEquals(Map.of("plate", 1L), result.calculation().remainders());
	}

	@Test void craftingNeededUsesOnlyIntermediateDemandNotCoveredByStock() {
		var result = BookmarkGroupSummary.calculate(List.of(
				recipe("gear", "plate", 3, "gear", 1), recipe("plate", "iron", 1, "plate", 4)), true,
				Map.of(), Map.of("plate", 2L));
		assertEquals(Map.of("plate", 1L), result.craftingNeeded());
		assertEquals(Map.of("plate", Map.of("plate", 1L)), result.calculation().neededByRecipe());
		assertEquals(Map.of("plate", 2L), result.covered());
		assertEquals(Map.of("plate", 3L), result.calculation().remainders());
	}

	@Test void sharedReusableToolIsNotMultipliedOrSpentByEachRecipe() {
		var tool = new RecipeChain.Material("hammer", 1, true, 1);
		var result = BookmarkGroupSummary.calculate(List.of(
				new RecipeChain.Recipe("a", List.of(tool), List.of(new RecipeChain.Material("a", 1)), 64),
				new RecipeChain.Recipe("b", List.of(tool), List.of(new RecipeChain.Material("b", 1)), 32)), false,
				Map.of(), Map.of("hammer", 1L));
		assertTrue(result.missing().isEmpty());
		assertEquals(Map.of("hammer", 1L), result.covered());
	}

	@Test void neededMaterialsKeepTheActualSupplyingRecipeWhenOutputsHaveTheSameType() {
		var result = BookmarkGroupSummary.calculate(List.of(
				recipe("gear", "plate", 3, "gear", 1),
				recipe("small_press", "iron", 1, "plate", 4),
				recipe("large_press", "copper", 2, "plate", 8)), true, Map.of(), Map.of());
		assertEquals(Map.of("large_press", Map.of("plate", 3L)), result.calculation().neededByRecipe());
		assertEquals(Map.of("plate", 3L), result.craftingNeeded());
		assertEquals(Map.of("gear", 1L, "plate", 4L), result.calculation().outputs());
		assertEquals(Map.of("plate", 5L), result.calculation().remainders());
	}
}
