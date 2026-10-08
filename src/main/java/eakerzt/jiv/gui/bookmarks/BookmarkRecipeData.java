package eakerzt.jiv.gui.bookmarks;

import eakerzt.jiv.api.gui.ingredient.IRecipeSlotView;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.ingredients.subtypes.UidContext;
import eakerzt.jiv.api.recipe.IFocusFactory;
import eakerzt.jiv.api.recipe.IRecipeManager;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.gui.bookmarks.planning.RecipeChain;

import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToLongFunction;

public record BookmarkRecipeData(List<Slot> slots) {
	public BookmarkRecipeData {
		slots = List.copyOf(slots);
	}

	public record Key(Object type, Object uid) {}

	public record DisplaySlot(
			Slot slot, List<Slot> sources, ITypedIngredient<?> ingredient, long amount) {
		public DisplaySlot {
			sources = List.copyOf(sources);
		}

		public void cycleChoice(BookmarkState state, int delta) {
			for (Slot source : sources) {
				state.choices.put(
						source.index(),
						Math.floorMod(
								state.choices.getOrDefault(source.index(), 0) + delta,
								source.candidates().size()));
			}
		}
	}

	private record DisplayKey(
			Object ingredient, RecipeIngredientRole role, boolean reusable, double chance) {}

	/**
	 * Merge equal materials within a recipe, retaining the source slots for candidate selection.
	 */
	public List<DisplaySlot> displaySlots(BookmarkState state, IIngredientManager manager) {
		return displaySlots(
				state,
				ingredient -> key(ingredient, manager),
				ingredient -> amount(ingredient, manager));
	}

	List<DisplaySlot> displaySlots(
			BookmarkState state,
			Function<ITypedIngredient<?>, Object> identity,
			ToLongFunction<ITypedIngredient<?>> quantity) {
		var merged = new LinkedHashMap<DisplayKey, DisplaySlot>();
		for (Slot slot : orderedSlots(state)) {
			if (slot.role() != RecipeIngredientRole.INPUT
					&& slot.role() != RecipeIngredientRole.OUTPUT) continue;
			var ingredient = slot.selected(state);
			var key =
					new DisplayKey(
							identity.apply(ingredient),
							slot.role(),
							slot.reusable(),
							slot.chance());
			long amount = Math.max(0, quantity.applyAsLong(ingredient));
			var previous = merged.get(key);
			var sources = new ArrayList<Slot>();
			if (previous != null) {
				sources.addAll(previous.sources());
				amount = RecipeChain.add(previous.amount(), amount);
			}
			sources.add(slot);
			merged.put(
					key,
					new DisplaySlot(
							previous == null ? slot : previous.slot(),
							sources,
							ingredient,
							amount));
		}
		return List.copyOf(merged.values());
	}

	private List<Slot> orderedSlots(BookmarkState state) {
		if (state.inputOrder.isEmpty()) return slots;
		var ordered = new ArrayList<>(slots);
		ordered.sort(
				Comparator.comparing((Slot slot) -> slot.role() == RecipeIngredientRole.INPUT)
						.thenComparingInt(
								slot -> {
									int index = state.inputOrder.indexOf(slot.index());
									return index < 0
											? state.inputOrder.size() + slot.index()
											: index;
								}));
		return ordered;
	}

	/**
	 * Restrict actual recorded supply to the selected output, preserving all input requirements.
	 */
	public BookmarkRecipeData selectOutput(int index, int choice) {
		if (index < 0) return this;
		return new BookmarkRecipeData(
				slots.stream()
						.filter(
								slot ->
										slot.role() != RecipeIngredientRole.OUTPUT
												|| slot.index() == index)
						.map(
								slot ->
										slot.role() != RecipeIngredientRole.OUTPUT
												? slot
												: new Slot(
														slot.index(),
														slot.role(),
														List.of(
																slot.candidates()
																		.get(
																				Math.floorMod(
																						choice,
																						slot.candidates()
																								.size()))),
														slot.reusable(),
														slot.chance(),
														slot.tagKey()))
						.toList());
	}

	public boolean moveInput(
			BookmarkState state, int source, int target, IIngredientManager manager) {
		return moveInput(state, source, target, displaySlots(state, manager));
	}

	boolean moveInput(BookmarkState state, int source, int target, List<DisplaySlot> displayed) {
		var inputs =
				new ArrayList<>(
						displayed.stream()
								.filter(
										display ->
												display.slot().role() == RecipeIngredientRole.INPUT)
								.toList());
		int from = -1, to = -1;
		for (int i = 0; i < inputs.size(); i++) {
			if (inputs.get(i).slot().index() == source) from = i;
			if (inputs.get(i).slot().index() == target) to = i;
		}
		if (from < 0 || to < 0 || from == to) return false;
		var moved = inputs.remove(from);
		inputs.add(to, moved);
		state.inputOrder.clear();
		inputs.forEach(
				display -> display.sources().forEach(slot -> state.inputOrder.add(slot.index())));
		return true;
	}

	public record Slot(
			int index,
			RecipeIngredientRole role,
			List<ITypedIngredient<?>> candidates,
			boolean reusable,
			double chance,
			Optional<TagKey<?>> tagKey) {
		public Slot(
				int index,
				RecipeIngredientRole role,
				List<ITypedIngredient<?>> candidates,
				boolean reusable,
				double chance) {
			this(index, role, candidates, reusable, chance, Optional.empty());
		}

		public Slot {
			candidates = List.copyOf(candidates);
		}

		public ITypedIngredient<?> selected(BookmarkState state) {
			return candidates.get(
					Math.floorMod(state.choices.getOrDefault(index, 0), candidates.size()));
		}
	}

	public static <R> BookmarkRecipeData create(
			RecipeBookmark<R, ?> bookmark, IRecipeManager manager, IFocusFactory focuses) {
		List<Slot> slots = new ArrayList<>();
		manager.createRecipeLayoutDrawable(
						bookmark.getRecipeCategory(),
						bookmark.getRecipe(),
						focuses.getEmptyFocusGroup())
				.ifPresent(
						layout -> {
							int index = 0;
							for (IRecipeSlotView slot :
									layout.getRecipeSlotsView().getSlotViews()) {
								List<ITypedIngredient<?>> candidates =
										slot.getAllIngredients().toList();
								if (!candidates.isEmpty())
									slots.add(
											new Slot(
													index,
													slot.getRole(),
													candidates,
													slot.isNonConsumed(),
													slot.getChance(),
													slot.getTagKey()));
								index++;
							}
						});
		return new BookmarkRecipeData(slots)
				.selectOutput(bookmark.getOutputSlot(), bookmark.getOutputChoice());
	}

	public RecipeChain.Recipe asRecipe(
			IBookmark bookmark, BookmarkState state, IIngredientManager manager) {
		List<RecipeChain.Material> inputs = new ArrayList<>(), outputs = new ArrayList<>();
		for (Slot slot : slots) {
			ITypedIngredient<?> ingredient = slot.selected(state);
			long amount = amount(ingredient, manager);
			if (slot.role() == RecipeIngredientRole.INPUT)
				inputs.add(
						new RecipeChain.Material(
								key(ingredient, manager),
								Math.max(0, amount),
								slot.reusable(),
								slot.chance()));
			else if (slot.role() == RecipeIngredientRole.OUTPUT)
				outputs.add(
						new RecipeChain.Material(
								key(ingredient, manager),
								Math.max(0, amount),
								false,
								slot.chance()));
		}
		return new RecipeChain.Recipe(bookmark, inputs, outputs, state.multiplier);
	}

	public static <T> Key key(ITypedIngredient<T> ingredient, IIngredientManager manager) {
		var helper = manager.getIngredientHelper(ingredient.getType());
		return new Key(ingredient.getType(), helper.getUid(ingredient, UidContext.Ingredient));
	}

	public static <T> long amount(ITypedIngredient<T> ingredient, IIngredientManager manager) {
		return manager.getIngredientHelper(ingredient.getType())
				.getAmount(ingredient.getIngredient());
	}
}
