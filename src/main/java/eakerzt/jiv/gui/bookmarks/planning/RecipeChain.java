package eakerzt.jiv.gui.bookmarks.planning;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Amount-only adapter for NEI's selected-recipe graph. No GUI or Minecraft dependency. */
public final class RecipeChain {
	private RecipeChain() {}

	public record Material(Object key, long amount, boolean reusable, double chance) {
		public Material {
			if (amount < 0 || !Double.isFinite(chance) || chance < 0 || chance > 1)
				throw new IllegalArgumentException("Invalid material amount or chance");
		}

		public Material(Object key, long amount) {
			this(key, amount, false, 1);
		}
	}

	public record Recipe(
			Object id, List<Material> inputs, List<Material> outputs, long multiplier) {
		public Recipe {
			inputs = List.copyOf(inputs);
			outputs = List.copyOf(outputs);
			if (multiplier < 0) throw new IllegalArgumentException("Negative multiplier");
		}
	}

	public record Result(
			Map<Object, Long> crafts,
			Map<Object, Long> inputs,
			Map<Object, Long> outputs,
			Map<Object, Long> remainders,
			Map<Object, Set<Object>> dependencies,
			boolean cycle,
			boolean uncertain,
			boolean overflow) {}

	public static Result calculate(List<Recipe> recipes, boolean linked) {
		return calculate(recipes, linked, Map.of());
	}

	public static Result calculate(
			List<Recipe> recipes, boolean linked, Map<Object, Long> inventory) {
		Map<Object, Long> stock = new HashMap<>(inventory);
		Map<Object, Recipe> byId = new LinkedHashMap<>();
		for (Recipe recipe : recipes) {
			if (byId.put(recipe.id(), recipe) != null)
				throw new IllegalArgumentException("Duplicate recipe id");
		}
		Map<Object, Set<Object>> dependencies = new LinkedHashMap<>();
		Map<Object, Object> providers = new HashMap<>();
		boolean uncertain = false;
		for (Recipe recipe : recipes) {
			dependencies.put(recipe.id(), new LinkedHashSet<>());
			for (Material output : recipe.outputs()) uncertain |= output.chance() < 1;
			if (!linked) continue;
			for (Material input : recipe.inputs()) {
				if (input.reusable() || input.amount() == 0) continue;
				Recipe best = null;
				long yield = 0;
				for (Recipe candidate : recipes) {
					if (candidate.id().equals(recipe.id())) continue;
					long amount =
							candidate.outputs().stream()
									.filter(o -> o.key().equals(input.key()) && o.chance() == 1)
									.mapToLong(Material::amount)
									.reduce(0, RecipeChain::add);
					if (amount > yield) {
						best = candidate;
						yield = amount;
					}
				}
				if (best != null) {
					providers.put(new Edge(recipe.id(), input.key()), best.id());
					dependencies.get(recipe.id()).add(best.id());
				}
			}
		}
		// Break back edges into external-input boundaries, as NEI does for recycling loops.
		boolean[] cycle = {false};
		Set<Object> visiting = new LinkedHashSet<>(), visited = new LinkedHashSet<>();
		for (Recipe recipe : recipes)
			breakCycles(recipe.id(), dependencies, providers, visiting, visited, cycle);
		Map<Object, Integer> consumers = new LinkedHashMap<>();
		for (Recipe recipe : recipes) consumers.put(recipe.id(), 0);
		dependencies
				.values()
				.forEach(values -> values.forEach(id -> consumers.merge(id, 1, Integer::sum)));
		Map<Object, Long> crafts = new LinkedHashMap<>(),
				demand = new LinkedHashMap<>(),
				required = new LinkedHashMap<>();
		Map<Object, Long> reusable = new LinkedHashMap<>(), external = new LinkedHashMap<>();
		ArrayDeque<Object> queue = new ArrayDeque<>();
		consumers.forEach(
				(id, count) -> {
					if (count == 0) queue.add(id);
				});
		for (Recipe recipe : recipes) {
			// Multiplier 1 on an intermediate is the link marker; >1 adds explicit extra batches.
			crafts.put(
					recipe.id(),
					consumers.get(recipe.id()) == 0 || !linked
							? recipe.multiplier()
							: Math.max(0, recipe.multiplier() - 1));
		}
		boolean overflow = false;
		while (!queue.isEmpty()) {
			Object id = queue.remove();
			Recipe recipe = byId.get(id);
			long batches = crafts.get(id);
			for (Material output : recipe.outputs()) {
				if (output.chance() != 1 || output.amount() == 0) continue;
				long outputPerBatch =
						recipe.outputs().stream()
								.filter(o -> o.key().equals(output.key()) && o.chance() == 1)
								.mapToLong(Material::amount)
								.reduce(0, RecipeChain::add);
				batches =
						Math.max(
								batches,
								add(
										crafts.get(id),
										ceil(
												demand.getOrDefault(new Edge(id, output.key()), 0L),
												outputPerBatch)));
			}
			crafts.put(id, batches);
			for (Material input : recipe.inputs()) {
				long amount =
						input.reusable()
								? (batches > 0 ? input.amount() : 0)
								: multiply(input.amount(), batches);
				if (input.reusable()) reusable.merge(input.key(), amount, Math::max);
				else {
					long available = Math.max(0, stock.getOrDefault(input.key(), 0L));
					long taken = Math.min(amount, available);
					amount -= taken;
					stock.put(input.key(), available - taken);
					required.merge(input.key(), amount, RecipeChain::add);
					Object provider = providers.get(new Edge(id, input.key()));
					if (provider != null)
						demand.merge(new Edge(provider, input.key()), amount, RecipeChain::add);
					else external.merge(input.key(), amount, RecipeChain::add);
				}
			}
			for (Object provider : dependencies.get(id)) {
				if (consumers.merge(provider, -1, Integer::sum) == 0) queue.add(provider);
			}
		}
		Map<Object, Long> finals = new LinkedHashMap<>(), remainders = new LinkedHashMap<>();
		Set<Object> usedProviders = new LinkedHashSet<>(providers.values());
		for (Recipe recipe : recipes) {
			Map<Object, Long> outputs = new LinkedHashMap<>();
			for (Material output : recipe.outputs()) {
				if (output.chance() != 1) continue;
				long amount = multiply(output.amount(), crafts.get(recipe.id()));
				outputs.merge(output.key(), amount, RecipeChain::add);
				overflow |= amount == Long.MAX_VALUE;
			}
			for (var output : outputs.entrySet()) {
				long remaining =
						Math.max(
								0,
								output.getValue()
										- demand.getOrDefault(
												new Edge(recipe.id(), output.getKey()), 0L));
				if (remaining > 0)
					(usedProviders.contains(recipe.id()) ? remainders : finals)
							.merge(output.getKey(), remaining, RecipeChain::add);
			}
		}
		reusable.forEach(
				(key, amount) ->
						external.merge(
								key,
								Math.max(0, amount - stock.getOrDefault(key, 0L)),
								RecipeChain::add));
		external.values().removeIf(amount -> amount == 0);
		overflow |= required.containsValue(Long.MAX_VALUE) || crafts.containsValue(Long.MAX_VALUE);
		Map<Object, Set<Object>> immutableDependencies = new LinkedHashMap<>();
		dependencies.forEach((key, value) -> immutableDependencies.put(key, Set.copyOf(value)));
		return new Result(
				Map.copyOf(crafts),
				Map.copyOf(external),
				Map.copyOf(finals),
				Map.copyOf(remainders),
				Map.copyOf(immutableDependencies),
				cycle[0],
				uncertain,
				overflow);
	}

	private record Edge(Object recipe, Object material) {}

	private static void breakCycles(
			Object id,
			Map<Object, Set<Object>> dependencies,
			Map<Object, Object> providers,
			Set<Object> visiting,
			Set<Object> visited,
			boolean[] cycle) {
		if (visited.contains(id)) return;
		visiting.add(id);
		for (Object provider : new ArrayList<>(dependencies.get(id))) {
			if (visiting.contains(provider)) {
				dependencies.get(id).remove(provider);
				providers
						.entrySet()
						.removeIf(
								e ->
										e.getKey() instanceof Edge edge
												&& edge.recipe().equals(id)
												&& e.getValue().equals(provider));
				cycle[0] = true;
			} else breakCycles(provider, dependencies, providers, visiting, visited, cycle);
		}
		visiting.remove(id);
		visited.add(id);
	}

	public static long add(long a, long b) {
		return a > Long.MAX_VALUE - b ? Long.MAX_VALUE : a + b;
	}

	public static long multiply(long a, long b) {
		return a != 0 && b > Long.MAX_VALUE / a ? Long.MAX_VALUE : a * b;
	}

	public static long ceil(long amount, long perBatch) {
		return amount / perBatch + (amount % perBatch == 0 ? 0 : 1);
	}
}
