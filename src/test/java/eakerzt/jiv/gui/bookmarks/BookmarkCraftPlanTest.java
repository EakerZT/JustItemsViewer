package eakerzt.jiv.gui.bookmarks;

import eakerzt.jiv.gui.bookmarks.planning.BookmarkCraftPlan;
import eakerzt.jiv.gui.bookmarks.planning.RecipeChain;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class BookmarkCraftPlanTest {
	private static RecipeChain.Recipe recipe(String id, String input, long needed, String output, long yield, long batches) {
		return new RecipeChain.Recipe(id, List.of(new RecipeChain.Material(input, needed)),
				List.of(new RecipeChain.Material(output, yield)), batches);
	}
	@Test void missingCraftsOnlyTheDeficitAndRoundsToWholeBatches() {
		var recipes = List.of(recipe("boards", "log", 1, "board", 4, 4));
		var result = BookmarkCraftPlan.calculate(recipes, true, Map.of("board", 6L, "log", 3L), BookmarkCraftPlan.Mode.MISSING);
		assertEquals(3L, result.crafts().get("boards"));
		assertTrue(result.inputs().isEmpty());
		assertEquals(4L, recipes.getFirst().multiplier());
	}
	@Test void missingDoesNothingWhenFinalTargetsAreCovered() {
		var result = BookmarkCraftPlan.calculate(List.of(recipe("boards", "log", 1, "board", 4, 4)),
				true, Map.of("board", 16L), BookmarkCraftPlan.Mode.MISSING);
		assertTrue(BookmarkCraftPlan.executionOrder(result).isEmpty());
	}
	@Test void allAdvancesToTheNextTargetMultipleLikeNei() {
		var recipes = List.of(recipe("boards", "log", 1, "board", 4, 4));
		var result = BookmarkCraftPlan.calculate(recipes, true, Map.of("board", 20L, "log", 3L), BookmarkCraftPlan.Mode.ALL);
		assertEquals(3L, result.crafts().get("boards"));
		assertEquals(4L, BookmarkCraftPlan.calculate(recipes, true, Map.of("board", 16L),
				BookmarkCraftPlan.Mode.ALL).crafts().get("boards"));
	}
	@Test void bothModesReuseIntermediateStockAndExecuteProvidersFirst() {
		var recipes = List.of(recipe("chest", "board", 8, "chest", 1, 2),
				recipe("boards", "log", 1, "board", 4, 1));
		for (var mode : BookmarkCraftPlan.Mode.values()) {
			var result = BookmarkCraftPlan.calculate(recipes, true, Map.of("board", 4L, "log", 3L), mode);
			assertEquals(3L, result.crafts().get("boards"));
			assertEquals(2L, result.crafts().get("chest"));
			assertTrue(result.inputs().isEmpty());
			assertEquals(List.of("boards", "chest"), BookmarkCraftPlan.executionOrder(result));
		}
	}
	@Test void missingMaterialsRemainVisibleInsteadOfCreatingAnExecutablePlan() {
		var result = BookmarkCraftPlan.calculate(List.of(recipe("chest", "board", 8, "chest", 1, 1)),
				true, Map.of("board", 3L), BookmarkCraftPlan.Mode.MISSING);
		assertEquals(Map.of("board", 5L), result.inputs());
	}
	@Test void rootsSharingAnOutputUseOneCombinedTargetAndDoNotSpendStockTwice() {
		var recipes = List.of(recipe("a", "oak", 1, "board", 1, 8),
				recipe("b", "birch", 1, "board", 1, 16));
		for (var mode : BookmarkCraftPlan.Mode.values()) {
			var result = BookmarkCraftPlan.calculate(recipes, true, Map.of("board", 6L), mode);
			assertEquals(18L, result.crafts().values().stream().mapToLong(Long::longValue).sum());
		}
	}
}
