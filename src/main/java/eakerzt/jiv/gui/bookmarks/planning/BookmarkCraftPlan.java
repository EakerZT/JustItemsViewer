package eakerzt.jiv.gui.bookmarks.planning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** An execution snapshot; never changes saved bookmark quantities. */
public final class BookmarkCraftPlan {
	private BookmarkCraftPlan() {}

	public enum Mode { ALL, MISSING }

	public static RecipeChain.Result calculate(List<RecipeChain.Recipe> recipes, boolean linked,
			Map<Object, Long> inventory, Mode mode) {
		var total = RecipeChain.calculate(recipes, linked);
		Set<Object> providers = new HashSet<>();
		total.dependencies().values().forEach(providers::addAll);
		var stock = new HashMap<>(inventory);
		var targets = new LinkedHashMap<Object, Long>();
		for (var recipe : recipes) {
			if (providers.contains(recipe.id())) continue;
			for (var output : recipe.outputs()) {
				if (output.chance() == 1 && output.amount() > 0)
					targets.merge(output.key(), RecipeChain.multiply(output.amount(), recipe.multiplier()), RecipeChain::add);
			}
		}
		var needs = new HashMap<Object, Long>();
		targets.forEach((key, target) -> {
			if (target == 0) return;
			long available = Math.max(0, stock.getOrDefault(key, 0L));
			// NEI's Craft Items advances to the next multiple of the saved target.
			needs.put(key, mode == Mode.ALL && available >= target
					? target - available % target : Math.max(0, target - available));
			stock.put(key, 0L);
		});
		var adjusted = new ArrayList<RecipeChain.Recipe>();
		for (var recipe : recipes) {
			long batches = recipe.multiplier();
			if (!providers.contains(recipe.id())) {
				batches = 0;
				var yields = new LinkedHashMap<Object, Long>();
				for (var output : recipe.outputs()) {
					if (output.chance() == 1 && output.amount() > 0)
						yields.merge(output.key(), output.amount(), RecipeChain::add);
				}
				for (var output : yields.entrySet()) {
					long target = RecipeChain.multiply(output.getValue(), recipe.multiplier());
					if (target == 0) continue;
					long needed = Math.min(target, needs.getOrDefault(output.getKey(), 0L));
					batches = Math.max(batches, RecipeChain.ceil(needed, output.getValue()));
				}
				for (var output : yields.entrySet())
					needs.put(output.getKey(), Math.max(0, needs.getOrDefault(output.getKey(), 0L)
							- RecipeChain.multiply(batches, output.getValue())));
			}
			adjusted.add(new RecipeChain.Recipe(recipe.id(), recipe.inputs(), recipe.outputs(), batches));
		}
		return RecipeChain.calculate(adjusted, linked, stock);
	}

	public static List<Object> executionOrder(RecipeChain.Result result) {
		var order = new ArrayList<Object>();
		var seen = new HashSet<Object>();
		for (var id : result.crafts().keySet()) visit(id, result, seen, order);
		return List.copyOf(order);
	}

	private static void visit(Object id, RecipeChain.Result result, Set<Object> seen, List<Object> order) {
		if (!seen.add(id)) return;
		for (var provider : result.dependencies().getOrDefault(id, Set.of())) visit(provider, result, seen, order);
		if (result.crafts().getOrDefault(id, 0L) > 0) order.add(id);
	}
}
