package eakerzt.jiv.gui.bookmarks;

import static org.junit.jupiter.api.Assertions.*;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import eakerzt.jiv.api.gui.IRecipeLayoutDrawable;
import eakerzt.jiv.api.gui.ingredient.*;
import eakerzt.jiv.api.helpers.ICodecHelper;
import eakerzt.jiv.api.ingredients.*;
import eakerzt.jiv.api.recipe.*;
import eakerzt.jiv.api.recipe.category.IRecipeCategory;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.config.IClientConfigs;
import eakerzt.jiv.common.ingredients.TypedIngredient;
import eakerzt.jiv.test.lib.TestClientConfig;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiFunction;

class RecipeBookmarkSelectionTest {
	private static final IIngredientType<String> TYPE = () -> String.class;
	private static final IRecipeType<String> RECIPE_TYPE =
			IRecipeType.create("test", "multi", String.class);
	private static final Identifier ID = Identifier.parse("test:recipe");
	private static final ITypedIngredient<String> MAIN =
			TypedIngredient.createUnvalidated(TYPE, "main");
	private static final ITypedIngredient<String> BYPRODUCT =
			TypedIngredient.createUnvalidated(TYPE, "byproduct");
	private IRecipeCategory<String> category;
	private IIngredientManager ingredients;
	private IRecipeManager recipes;
	private ICodecHelper codecs;
	private IRecipeSlotsView slots;

	@BeforeEach
	void setup() {
		Internal.setClientConfigs(
				proxy(IClientConfigs.class, (method, args) -> new TestClientConfig(false)));
		category =
				proxy(
						IRecipeCategory.class,
						(method, args) ->
								switch (method.getName()) {
									case "getIdentifier" -> ID;
									case "getRecipeType" -> RECIPE_TYPE;
									case "getCodec" -> Codec.STRING;
									default -> null;
								});
		var helper =
				new IIngredientHelper<String>() {
					public IIngredientType<String> getIngredientType() {
						return TYPE;
					}

					public String getDisplayName(String ingredient) {
						return ingredient;
					}

					public Object getUid(
							String ingredient,
							eakerzt.jiv.api.ingredients.subtypes.UidContext context) {
						return ingredient;
					}

					public Identifier getIdentifier(String ingredient) {
						return Identifier.fromNamespaceAndPath("test", ingredient);
					}

					public String copyIngredient(String ingredient) {
						return ingredient;
					}

					public String getErrorInfo(String ingredient) {
						return ingredient;
					}

					public long getAmount(String ingredient) {
						return 1;
					}
				};
		ingredients =
				proxy(
						IIngredientManager.class,
						(method, args) ->
								method.getName().equals("getIngredientHelper") ? helper : args[0]);
		slots =
				() ->
						List.of(
								slot(
										RecipeIngredientRole.INPUT,
										TypedIngredient.createUnvalidated(TYPE, "ore")),
								slot(RecipeIngredientRole.OUTPUT, MAIN),
								slot(RecipeIngredientRole.OUTPUT, BYPRODUCT));
		var layout = proxy(IRecipeLayoutDrawable.class, (method, args) -> slots);
		recipes =
				proxy(
						IRecipeManager.class,
						(method, args) ->
								switch (method.getName()) {
									case "getRecipeCategory" -> category;
									case "getRecipeIngredients" ->
											(IIngredientSupplier)
													role ->
															role == RecipeIngredientRole.OUTPUT
																	? List.of(MAIN, BYPRODUCT)
																	: List.of();
									case "createRecipeLayoutDrawable" -> Optional.of(layout);
									default -> null;
								});
		Codec<ITypedIngredient<?>> typed =
				Codec.STRING.xmap(
						value -> TypedIngredient.createUnvalidated(TYPE, value),
						value -> (String) value.getIngredient());
		codecs =
				proxy(
						ICodecHelper.class,
						(method, args) ->
								switch (method.getName()) {
									case "getRecipeTypeCodec" ->
											Codec.STRING.<IRecipeType<?>>xmap(
													value -> RECIPE_TYPE,
													value -> value.getUid().toString());
									case "getTypedIngredientCodec" -> typed.fieldOf("ingredient");
									default -> null;
								});
	}

	@Test
	void inputPreviewOnlyReordersItsRecipeAndRetainsOrderAfterDrop() {
		slots =
				() ->
						List.of(
								slot(
										RecipeIngredientRole.INPUT,
										TypedIngredient.createUnvalidated(TYPE, "ore")),
								slot(
										RecipeIngredientRole.INPUT,
										TypedIngredient.createUnvalidated(TYPE, "coal")),
								slot(RecipeIngredientRole.OUTPUT, MAIN));
		var first = base();
		var second =
				new RecipeBookmark<>(
						category, "other", Identifier.parse("test:other"), MAIN, true, null);
		var config = proxy(eakerzt.jiv.gui.config.IBookmarkConfig.class, (method, args) -> null);
		var focuses = proxy(IFocusFactory.class, (method, args) -> null);
		var list =
				new BookmarkList(
						recipes,
						focuses,
						ingredients,
						RegistryAccess.EMPTY,
						config,
						new TestClientConfig(false),
						null,
						codecs,
						null,
						null);
		list.setFromConfigFile(List.of(first, second));
		list.page().groups.get(0).todo = true;
		list.setLayoutColumns(4);
		var before = list.getElements();
		var otherPositions = positions(before, second);
		list.beginDrag(first, 0);
		assertTrue(list.moveRecipeInput(first, 0, 1));
		assertEquals(List.of(1, 0), list.state(first).inputOrder);
		assertEquals(otherPositions, positions(list.getElements(), second));
		assertEquals(List.of(first, second), list.page().bookmarks);
		// Attempts to move another recipe or its inputs during input sorting are rejected.
		list.moveBookmark(first, 1);
		assertFalse(list.moveRecipeInput(second, 0, 1));
		assertTrue(list.state(second).inputOrder.isEmpty());
		assertEquals(otherPositions, positions(list.getElements(), second));
		list.finishDrag(true);
		assertEquals(List.of(1, 0), list.state(first).inputOrder);
		assertEquals(List.of(first, second), list.page().bookmarks);
		assertEquals(otherPositions, positions(list.getElements(), second));
		assertEquals(
				List.of("main", "coal", "ore"),
				list.getElements().stream()
						.filter(e -> e.getBookmark().orElse(null) == first)
						.map(e -> (String) e.getTypedIngredient().getIngredient())
						.toList());
		list.beginDrag(first, 1);
		assertTrue(list.moveRecipeInput(first, 1, 0));
		list.finishDrag(false);
		assertEquals(List.of(1, 0), list.state(first).inputOrder);
		assertEquals(otherPositions, positions(list.getElements(), second));
	}

	private static List<Integer> positions(
			List<eakerzt.jiv.gui.overlay.elements.IElement<?>> elements, IBookmark owner) {
		return java.util.stream.IntStream.range(0, elements.size())
				.filter(i -> elements.get(i).getBookmark().orElse(null) == owner)
				.boxed()
				.toList();
	}

	@Test
	void defaultRecipeCanMovePastTheFinalSubgroupAndGroupDragKeepsWholeBlock() {
		var first = base();
		var childA =
				new RecipeBookmark<>(
						category, "childA", Identifier.parse("test:child_a"), MAIN, true, null);
		var childB =
				new RecipeBookmark<>(
						category, "childB", Identifier.parse("test:child_b"), MAIN, true, null);
		var config = proxy(eakerzt.jiv.gui.config.IBookmarkConfig.class, (method, args) -> null);
		var focuses = proxy(IFocusFactory.class, (method, args) -> null);
		var list =
				new BookmarkList(
						recipes,
						focuses,
						ingredients,
						RegistryAccess.EMPTY,
						config,
						new TestClientConfig(false),
						null,
						codecs,
						null,
						null);
		list.setFromConfigFile(List.of(first, childA, childB));
		var group = new BookmarkGroup();
		group.todo = true;
		group.linked = true;
		list.page().groups.put(1, group);
		list.state(childA).group = list.state(childB).group = 1;
		list.state(childA).multiplier = 3;
		list.state(childB).multiplier = 7;
		list.setLayoutColumns(4);
		assertEquals(List.of(first, childA, childB), visibleOwners(list));
		list.beginDrag(first, -1);
		list.moveBookmarkToGroup(first, 2, 0);
		assertEquals(List.of(childA, childB, first), visibleOwners(list));
		assertEquals(0, list.state(first).group);
		list.finishDrag(true);
		list.beginGroupDrag(1);
		assertTrue(
				list.getElements().stream()
						.filter(
								e ->
										e.getBookmark()
												.filter(b -> list.state(b).group == 1)
												.isPresent())
						.allMatch(e -> e.isDragPlaceholder()));
		assertTrue(list.moveGroupRelative(1, first, true));
		assertEquals(List.of(first, childA, childB), visibleOwners(list));
		assertEquals(1, list.state(childA).group);
		assertEquals(1, list.state(childB).group);
		assertEquals(3, list.state(childA).multiplier);
		assertEquals(7, list.state(childB).multiplier);
		assertSame(group, list.page().groups.get(1));
		list.finishDrag(false);
		assertEquals(List.of(childA, childB, first), visibleOwners(list));
		list.beginGroupDrag(1);
		assertTrue(list.moveGroupRelative(1, first, true));
		list.finishDrag(true);
		assertEquals(List.of(first, childA, childB), visibleOwners(list));
		assertFalse(list.moveGroupRelative(1, childA, false));
	}

	private static List<IBookmark> visibleOwners(BookmarkList list) {
		return list.getElements().stream()
				.flatMap(e -> e.getBookmark().stream())
				.distinct()
				.toList();
	}

	private BookmarkList bookmarkList() {
		return new BookmarkList(recipes, proxy(IFocusFactory.class, (method, args) -> null),
				ingredients, RegistryAccess.EMPTY,
				proxy(eakerzt.jiv.gui.config.IBookmarkConfig.class, (method, args) -> null),
				new TestClientConfig(false), null, codecs, null, null);
	}

	@Test void recipeToggleIgnoresOutputSelectionAndRemovedMaterials() {
		var list = bookmarkList();
		var single = base().withOutputSelection(2, 0, BYPRODUCT);
		var all = base();
		var grouped = base();
		list.page().bookmarks.addAll(List.of(single, all, grouped));
		list.state(grouped).group = 1;
		list.state(single).removedSlots.add(0);
		assertTrue(list.containsUngrouped(base()));
		list.toggleBookmark(base());
		assertEquals(1, list.page().bookmarks.size());
		assertSame(grouped, list.page().bookmarks.getFirst());
		assertFalse(list.containsUngrouped(base()));
		list.toggleBookmark(base());
		assertEquals(2, list.page().bookmarks.size());
	}

	@Test void deletingMergedInputExcludesEverySourceAndLastOutputRemovesRecipe() {
		slots = () -> List.of(
				slot(RecipeIngredientRole.INPUT, TypedIngredient.createUnvalidated(TYPE, "ore")),
				slot(RecipeIngredientRole.INPUT, TypedIngredient.createUnvalidated(TYPE, "ore")),
				slot(RecipeIngredientRole.OUTPUT, MAIN),
				slot(RecipeIngredientRole.OUTPUT, BYPRODUCT));
		var list = bookmarkList();
		var recipe = base();
		list.toggleBookmark(recipe);
		list.page().groups.get(0).todo = true;
		list.setLayoutColumns(4);
		var input = list.getElements().stream().filter(e -> e instanceof BookmarkCell<?> cell
				&& cell.role == RecipeIngredientRole.INPUT).findFirst().orElseThrow();
		assertTrue(list.removeElement(input));
		assertEquals(Set.of(0, 1), list.state(recipe).removedSlots);
		assertTrue(list.calculation(0).orElseThrow().inputs().isEmpty());
		assertTrue(list.getElements().stream().noneMatch(e -> e instanceof BookmarkCell<?> cell
				&& cell.role == RecipeIngredientRole.INPUT));
		var transfer = new BookmarkTransferSlots(slots, list.state(recipe).removedSlots);
		assertEquals(4, transfer.getSlotViews().size());
		assertTrue(transfer.getSlotViews().get(0).isEmpty());
		assertTrue(transfer.getSlotViews().get(1).isEmpty());
		assertEquals(RecipeIngredientRole.OUTPUT, transfer.getSlotViews().get(2).getRole());
		var output = list.getElements().stream().filter(e -> !e.isEmptySlot()).findFirst().orElseThrow();
		assertTrue(list.removeElement(output));
		assertEquals(1, list.page().bookmarks.size());
		var last = list.getElements().stream().filter(e -> !e.isEmptySlot()).findFirst().orElseThrow();
		assertTrue(list.removeElement(last));
		assertTrue(list.page().bookmarks.isEmpty());
	}

	private RecipeBookmark<String, String> base() {
		return new RecipeBookmark<>(category, "recipe", ID, MAIN, true, null);
	}

	private Codec<IBookmark> codec() {
		return BookmarkCodec.create(
						codecs,
						ingredients,
						recipes,
						null,
						new BookmarkFactory(codecs, RegistryAccess.EMPTY, ingredients))
				.codec();
	}

	private static IRecipeSlotView slot(RecipeIngredientRole role, ITypedIngredient<?> ingredient) {
		return proxy(
				IRecipeSlotView.class,
				(method, args) ->
						switch (method.getName()) {
							case "getRole" -> role;
							case "getDisplayedIngredient" -> Optional.of(ingredient);
							case "getAllIngredients" -> List.of(ingredient).stream();
							case "getTagKey" -> Optional.empty();
							case "isNonConsumed" -> false;
							case "getChance" -> 1.0;
							default -> null;
						});
	}

	@SuppressWarnings("unchecked")
	private static <T> T proxy(Class<T> type, BiFunction<Method, Object[], Object> behavior) {
		return (T)
				Proxy.newProxyInstance(
						type.getClassLoader(),
						new Class<?>[] {type},
						(proxy, method, args) ->
								switch (method.getName()) {
									case "equals" -> proxy == args[0];
									case "hashCode" -> System.identityHashCode(proxy);
									case "toString" -> type.getSimpleName();
									default -> behavior.apply(method, args);
								});
	}

	@Test
	void inputSelectsFirstOutputWhileOutputSelectsHoveredProduct() {
		var first = base().selectOutputs(slots, 0, false, ingredients);
		var second = base().selectOutputs(slots, 2, false, ingredients);
		assertEquals(1, first.getOutputSlot());
		assertEquals("main", first.getDisplayIngredient().getIngredient());
		assertEquals(2, second.getOutputSlot());
		assertEquals("byproduct", second.getDisplayIngredient().getIngredient());
		var all = base().selectOutputs(slots, 2, true, ingredients);
		assertEquals(-1, all.getOutputSlot());
		assertEquals("main", all.getDisplayIngredient().getIngredient());
		assertEquals(3, new HashSet<>(List.of(first, second, all)).size());
	}

	@Test
	void singleOutputPreservesRequirementsAndExcludesUnrecordedSupply() {
		var single = base().selectOutputs(slots, 2, false, ingredients);
		var data =
				BookmarkRecipeData.create(
						single, recipes, proxy(IFocusFactory.class, (method, args) -> null));
		assertEquals(
				List.of(0, 2), data.slots().stream().map(BookmarkRecipeData.Slot::index).toList());
		assertEquals("ore", data.slots().getFirst().selected(new BookmarkState()).getIngredient());
		assertEquals(
				"byproduct", data.slots().getLast().selected(new BookmarkState()).getIngredient());
		assertEquals(
				3,
				BookmarkRecipeData.create(
								base(), recipes, proxy(IFocusFactory.class, (method, args) -> null))
						.slots()
						.size());
		var state = new BookmarkState();
		state.multiplier = 2;
		var calculated =
				eakerzt.jiv.gui.bookmarks.planning.RecipeChain.calculate(
						List.of(data.asRecipe(single, state, ingredients)), false);
		assertEquals(
				Map.of(new BookmarkRecipeData.Key(TYPE, "byproduct"), 2L), calculated.outputs());
		assertEquals(Map.of(new BookmarkRecipeData.Key(TYPE, "ore"), 2L), calculated.inputs());
	}

	@Test
	void selectedOutputAndAllOutputsRoundTripAsIndependentBookmarks() {
		var single = base().withOutputSelection(2, 0, BYPRODUCT);
		var encoded = codec().encodeStart(JsonOps.INSTANCE, single).getOrThrow();
		var restored = (RecipeBookmark<?, ?>) codec().parse(JsonOps.INSTANCE, encoded).getOrThrow();
		assertEquals(single, restored);
		assertEquals("byproduct", restored.getDisplayIngredient().getIngredient());
		assertEquals(2, restored.getOutputSlot());
		assertEquals(0, restored.getOutputChoice());
		var all = base();
		var old = codec().encodeStart(JsonOps.INSTANCE, all).getOrThrow().getAsJsonObject();
		old.remove("outputSlot");
		old.remove("outputChoice");
		old.remove("selectedOutput");
		assertEquals(all, codec().parse(JsonOps.INSTANCE, old).getOrThrow());
	}
}
