package eakerzt.jiv.gui.bookmarks;

import static org.junit.jupiter.api.Assertions.*;

import eakerzt.jiv.gui.bookmarks.planning.RecipeChain;
import eakerzt.jiv.gui.bookmarks.planning.RecipeChain.*;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

class RecipeChainTest {
	private static Material material(String key, long amount) {
		return new Material(key, amount);
	}

	private static Recipe recipe(
			String id,
			String input,
			long inputAmount,
			String output,
			long outputAmount,
			long count) {
		return new Recipe(
				id,
				List.of(material(input, inputAmount)),
				List.of(material(output, outputAmount)),
				count);
	}

	@Test
	void roundsSharedIntermediateDemandOnce() {
		Result result =
				RecipeChain.calculate(
						List.of(
								recipe("gear", "plate", 3, "gear", 1, 1),
								recipe("pipe", "plate", 2, "pipe", 1, 1),
								recipe("plate", "ingot", 1, "plate", 4, 1)),
						true);
		assertEquals(2L, result.crafts().get("plate"));
		assertEquals(Map.of("ingot", 2L), result.inputs());
		assertEquals(Map.of("gear", 1L, "pipe", 1L), result.outputs());
		assertEquals(Map.of("plate", 3L), result.remainders());
	}

	@Test
	void computesBatchMultipliersWithoutLinking() {
		Result result =
				RecipeChain.calculate(List.of(recipe("plate", "ingot", 2, "plate", 4, 3)), false);
		assertEquals(Map.of("ingot", 6L), result.inputs());
		assertEquals(Map.of("plate", 12L), result.outputs());
	}

	@Test
	void extraIntermediateMultiplierAddsExplicitBatches() {
		Result result =
				RecipeChain.calculate(
						List.of(
								recipe("gear", "plate", 3, "gear", 1, 1),
								recipe("plate", "ingot", 1, "plate", 4, 2)),
						true);
		assertEquals(2L, result.crafts().get("plate"));
		assertEquals(Map.of("plate", 5L), result.remainders());
	}

	@Test
	void zeroTargetsDoNotConsumeMaterials() {
		Result result =
				RecipeChain.calculate(
						List.of(
								recipe("gear", "plate", 3, "gear", 1, 0),
								recipe("plate", "ingot", 1, "plate", 4, 1)),
						true);
		assertTrue(result.inputs().isEmpty());
		assertTrue(result.outputs().isEmpty());
		assertEquals(0L, result.crafts().get("plate"));
	}

	@Test
	void combinesRepeatedOutputSlotsBeforeRounding() {
		Recipe producer =
				new Recipe(
						"plate",
						List.of(material("ingot", 1)),
						List.of(material("plate", 2), material("plate", 2)),
						1);
		Result result =
				RecipeChain.calculate(
						List.of(recipe("gear", "plate", 3, "gear", 1, 1), producer), true);
		assertEquals(1L, result.crafts().get("plate"));
		assertEquals(Map.of("plate", 1L), result.remainders());
	}

	@Test
	void multiOutputProducerUsesLargestRequiredBatchCount() {
		Recipe producer =
				new Recipe(
						"separation",
						List.of(material("ore", 1)),
						List.of(material("copper", 2), material("tin", 1)),
						1);
		Recipe target =
				new Recipe(
						"bronze",
						List.of(material("copper", 3), material("tin", 3)),
						List.of(material("bronze", 4)),
						1);
		Result result = RecipeChain.calculate(List.of(producer, target), true);
		assertEquals(3L, result.crafts().get("separation"));
		assertEquals(Map.of("ore", 3L), result.inputs());
		assertEquals(Map.of("copper", 3L), result.remainders());
	}

	@Test
	void reusableToolIsNotMultipliedByCraftsOrConsumers() {
		Recipe first =
				new Recipe(
						"plate",
						List.of(material("ingot", 1), new Material("mold", 1, true, 1)),
						List.of(material("plate", 1)),
						8);
		Recipe second =
				new Recipe(
						"wire",
						List.of(material("ingot", 2), new Material("mold", 1, true, 1)),
						List.of(material("wire", 1)),
						4);
		Result result = RecipeChain.calculate(List.of(first, second), false);
		assertEquals(Map.of("ingot", 16L, "mold", 1L), result.inputs());
	}

	@Test
	void inventoryIntermediateReducesUpstreamCrafts() {
		Result result =
				RecipeChain.calculate(
						List.of(
								recipe("gear", "plate", 3, "gear", 1, 2),
								recipe("plate", "ingot", 1, "plate", 4, 1)),
						true,
						Map.of("plate", 5L));
		assertEquals(1L, result.crafts().get("plate"));
		assertEquals(Map.of("ingot", 1L), result.inputs());
		assertEquals(Map.of("plate", 3L), result.remainders());
	}

	@Test
	void inventoryIsSharedAcrossConsumers() {
		Result result =
				RecipeChain.calculate(
						List.of(
								recipe("gear", "plate", 3, "gear", 1, 1),
								recipe("pipe", "plate", 2, "pipe", 1, 1)),
						true,
						Map.of("plate", 4L));
		assertEquals(Map.of("plate", 1L), result.inputs());
	}

	@Test
	void cycleKeepsAnExternalStartingBoundaryAndTerminates() {
		Result result =
				assertTimeoutPreemptively(
						Duration.ofSeconds(1),
						() ->
								RecipeChain.calculate(
										List.of(
												recipe("a", "a", 1, "b", 1, 1),
												recipe("b", "b", 1, "a", 1, 1)),
										true));
		assertTrue(result.cycle());
		assertFalse(result.inputs().isEmpty());
		assertEquals(2, result.crafts().size());
	}

	@Test
	void chanceOutputsNeverSatisfyGuaranteedDemand() {
		Recipe random =
				new Recipe(
						"sifting",
						List.of(material("gravel", 1)),
						List.of(new Material("gem", 1, false, 0.25)),
						1);
		Result result =
				RecipeChain.calculate(
						List.of(random, recipe("ring", "gem", 1, "ring", 1, 1)), true);
		assertTrue(result.uncertain());
		assertEquals(Map.of("gravel", 1L, "gem", 1L), result.inputs());
	}

	@Test
	void quantitiesSaturateRatherThanWrapNegative() {
		Result result =
				RecipeChain.calculate(
						List.of(recipe("huge", "ore", 2, "dust", 4, Long.MAX_VALUE)), false);
		assertTrue(result.overflow());
		assertEquals(Long.MAX_VALUE, result.inputs().get("ore"));
		assertEquals(Long.MAX_VALUE, result.outputs().get("dust"));
	}

	@Test
	void rejectsInvalidMetadata() {
		assertThrows(IllegalArgumentException.class, () -> new Material("ore", -1));
		assertThrows(
				IllegalArgumentException.class, () -> new Material("ore", 1, false, Double.NaN));
	}
}
