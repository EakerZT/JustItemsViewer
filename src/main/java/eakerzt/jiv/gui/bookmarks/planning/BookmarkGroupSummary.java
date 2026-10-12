package eakerzt.jiv.gui.bookmarks.planning;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/** Inventory comparison for recipe requirements and independent shopping-list targets. */
public record BookmarkGroupSummary(
		RecipeChain.Result calculation,
		Map<Object, Long> required,
		Map<Object, Long> missing,
		Map<Object, Long> covered,
		Map<Object, Long> craftingNeeded) {
	public static BookmarkGroupSummary calculate(
			List<RecipeChain.Recipe> recipes, boolean linked,
			Map<Object, Long> targets, Map<Object, Long> inventory) {
		return calculate(recipes, linked, targets, inventory, null);
	}

	public static BookmarkGroupSummary calculate(
			List<RecipeChain.Recipe> recipes, boolean linked,
			Map<Object, Long> targets, Map<Object, Long> inventory, BookmarkCraftPlan.@Nullable Mode mode) {
		var stock = new LinkedHashMap<>(inventory);
		var targetMissing = new LinkedHashMap<Object, Long>();
		var targetAvailable = new LinkedHashMap<Object, Long>();
		targets.forEach((key, amount) -> {
			long available = Math.max(0, stock.getOrDefault(key, 0L));
			long taken = Math.min(amount, available);
			stock.put(key, available - taken);
			if (taken > 0) targetAvailable.put(key, taken);
			if (amount > taken) targetMissing.put(key, amount - taken);
		});
		var total = RecipeChain.calculate(recipes, linked);
		var calculation = mode != null ? BookmarkCraftPlan.calculate(recipes, linked, stock, mode)
				: inventory.isEmpty() ? total : RecipeChain.calculate(recipes, linked, stock);
		var required = new LinkedHashMap<>(total.inputs());
		targets.forEach((key, amount) -> required.merge(key, amount, RecipeChain::add));
		var missing = new LinkedHashMap<>(calculation.inputs());
		targetMissing.forEach((key, amount) -> missing.merge(key, amount, RecipeChain::add));
		var covered = new LinkedHashMap<>(calculation.inventoryUsed());
		targetAvailable.forEach((key, amount) -> covered.merge(key, amount, RecipeChain::add));
		// Intermediates consumed downstream, excluding final targets and batch leftovers.
		var craftingNeeded = new LinkedHashMap<Object, Long>();
		calculation.neededByRecipe().values().forEach(materials ->
				materials.forEach((key, amount) -> craftingNeeded.merge(key, amount, RecipeChain::add)));
		return new BookmarkGroupSummary(calculation, Map.copyOf(required), Map.copyOf(missing),
				Map.copyOf(covered), Map.copyOf(craftingNeeded));
	}
}
