package eakerzt.jiv.test;

import eakerzt.jiv.api.helpers.IColorHelper;
import eakerzt.jiv.api.helpers.IModIdHelper;
import eakerzt.jiv.api.ingredients.IIngredientRenderer;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.ingredients.subtypes.UidContext;
import eakerzt.jiv.api.runtime.IEditModeConfig;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.api.runtime.IIngredientVisibility;
import eakerzt.jiv.api.search.ISearchStorageBuilder;
import eakerzt.jiv.api.search.ISearchStorageBuilderFactory;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.config.IClientConfig;
import eakerzt.jiv.common.config.IClientConfigs;
import eakerzt.jiv.common.config.IClientToggleState;
import eakerzt.jiv.common.config.IIngredientFilterConfig;
import eakerzt.jiv.common.config.IIngredientGridConfig;
import eakerzt.jiv.common.config.ClientToggleState;
import eakerzt.jiv.common.search.GeneralizedSuffixTreeSearchStorage;
import eakerzt.jiv.common.search.SearchStorageBuilderAdapter;
import eakerzt.jiv.gui.filter.FilterTextSource;
import eakerzt.jiv.gui.filter.IFilterTextSource;
import eakerzt.jiv.gui.config.IngredientTypeSortingConfig;
import eakerzt.jiv.gui.ingredients.IListElementInfo;
import eakerzt.jiv.gui.ingredients.IngredientFilter;
import eakerzt.jiv.gui.ingredients.IngredientListElementFactory;
import eakerzt.jiv.gui.ingredients.ListElementInfo;
import eakerzt.jiv.library.config.EditModeConfig;
import eakerzt.jiv.library.ingredients.IngredientBlacklistInternal;
import eakerzt.jiv.library.ingredients.IngredientVisibility;
import eakerzt.jiv.library.ingredients.subtypes.SubtypeInterpreters;
import eakerzt.jiv.library.ingredients.subtypes.SubtypeManager;
import eakerzt.jiv.library.load.registration.IngredientManagerBuilder;
import eakerzt.jiv.test.lib.TestClientConfig;
import eakerzt.jiv.test.lib.TestColorHelper;
import eakerzt.jiv.test.lib.TestIngredient;
import eakerzt.jiv.test.lib.TestIngredientFilterConfig;
import eakerzt.jiv.test.lib.TestIngredientHelper;
import eakerzt.jiv.test.lib.TestModIdHelper;
import eakerzt.jiv.test.lib.TestPlugin;
import eakerzt.jiv.config.api.sorting.ISortingConfig;
import eakerzt.jiv.config.api.migration.ISortingConfigMigrator;
import net.minecraft.network.chat.Component;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class IngredientFilterTest {
	private static final int EXTRA_INGREDIENT_COUNT = 5;
	@Nullable
	private IIngredientManager ingredientManager;
	@Nullable
	private IngredientFilter ingredientFilter;
	@Nullable
	private IIngredientVisibility ingredientVisibility;
	@Nullable
	private List<IListElementInfo<?>> baseList;
	@Nullable
	private EditModeConfig editModeConfig;
	@Nullable
	private FilterTextSource filterTextSource;
	@Nullable
	private IModIdHelper modIdHelper;
	@Nullable
	private TestIngredientFilterConfig ingredientFilterConfig;
	@Nullable
	private ClientToggleState toggleState;
	@Nullable
	private TestSortingConfig ingredientTypeSortingConfig;

	private static final TestIngredient ALIASED_INGREDIENT = new TestIngredient(0);
	private static final String INGREDIENT_ALIAS = "aliasedingredientzero";
	private static final String ALIASED_MOD_ID = "jiv_test_mod";
	private static final String MOD_ALIAS = "testmodalias";

	@BeforeEach
	public void setup() {
		setup(false);
	}

	@AfterEach
	public void tearDown() {
		Internal.getOptionalClientConfigs().ifPresent(IClientConfigs::onRuntimeStopped);
	}

	private void setup(boolean lowMemorySlowSearchEnabled) {
		TestPlugin testPlugin = new TestPlugin();

		SubtypeInterpreters subtypeInterpreters = new SubtypeInterpreters();
		SubtypeManager subtypeManager = new SubtypeManager(subtypeInterpreters);

		IColorHelper colorHelper = new TestColorHelper();
		IngredientManagerBuilder ingredientManagerBuilder = new IngredientManagerBuilder(
			subtypeManager,
			colorHelper,
			new ContextMap.Builder().create(new ContextKeySet.Builder().build())
		);
		testPlugin.registerIngredients(ingredientManagerBuilder);
		ingredientManagerBuilder.addAlias(TestIngredient.TYPE, ALIASED_INGREDIENT, INGREDIENT_ALIAS);
		this.ingredientManager = ingredientManagerBuilder.build();

		IngredientBlacklistInternal blacklist = new IngredientBlacklistInternal(ingredientManager);
		this.modIdHelper = new TestModIdHelper() {
			@Override
			public Set<String> getModAliases(String modId) {
				if (ALIASED_MOD_ID.equals(modId)) {
					return Set.of(MOD_ALIAS);
				}
				return Set.of();
			}
		};
		IClientConfig clientConfig = new TestClientConfig(lowMemorySlowSearchEnabled);

		this.ingredientFilterConfig = new TestIngredientFilterConfig();
		TestIngredientFilterConfig ingredientFilterConfig = this.ingredientFilterConfig;
		Internal.getOptionalClientConfigs().ifPresent(IClientConfigs::onRuntimeStopped);
		Internal.setClientConfigs(new TestClientConfigs(clientConfig, ingredientFilterConfig));

		this.baseList = IngredientListElementFactory.createBaseList(ingredientManager, ingredientFilterConfig, modIdHelper);

		this.editModeConfig = new EditModeConfig(new NullSerializer(), ingredientManager);

		this.toggleState = new ClientToggleState();
		IClientToggleState toggleState = this.toggleState;
		this.ingredientTypeSortingConfig = new TestSortingConfig();

		this.ingredientVisibility = new IngredientVisibility(blacklist, toggleState, editModeConfig, ingredientManager);
		this.filterTextSource = new FilterTextSource();
		this.ingredientFilter = new IngredientFilter(
			filterTextSource,
			clientConfig,
			ingredientFilterConfig,
			ingredientManager,
			ingredients -> Comparator.comparingInt(Object::hashCode),
			baseList,
			modIdHelper,
			ingredientVisibility,
			new IngredientTypeSortingConfig(ingredientTypeSortingConfig),
			colorHelper,
			new ISearchStorageBuilderFactory() {
				@Override
				public <T> ISearchStorageBuilder<T> create() {
					return new SearchStorageBuilderAdapter<>(new GeneralizedSuffixTreeSearchStorage<>());
				}
			},
			toggleState
		);

		this.ingredientManager.registerIngredientListener(blacklist);
		this.ingredientManager.registerIngredientListener(ingredientFilter);

		this.ingredientVisibility.registerListener(this.ingredientFilter);
	}

	@Test
	public void testSetup() {
		Assertions.assertNotNull(ingredientFilter);

		List<?> ingredientList = ingredientFilter.getElements();
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, ingredientList.size());
	}

	@Test
	public void testRemovedIngredientTypesAreHidden() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(ingredientTypeSortingConfig);

		ingredientTypeSortingConfig.setVisible(false);
		ingredientFilter.onIngredientTypeSortOrderConfigChanged();
		Assertions.assertTrue(ingredientFilter.getElements().isEmpty());

		ingredientTypeSortingConfig.setVisible(true);
		ingredientFilter.onIngredientTypeSortOrderConfigChanged();
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, ingredientFilter.getElements().size());
	}

	@Test
	public void testAddingAndRemovingIngredients() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(ingredientManager);
		Assertions.assertNotNull(ingredientVisibility);
		Assertions.assertNotNull(filterTextSource);
		Assertions.assertNotNull(modIdHelper);
		Assertions.assertNotNull(ingredientFilterConfig);

		List<TestIngredient> ingredients = createIngredients();

		addIngredients(ingredientFilter, filterTextSource, ingredientVisibility, ingredientManager, modIdHelper, ingredientFilterConfig, ingredients);
		removeIngredients(ingredientFilter, filterTextSource, ingredientVisibility, ingredientManager, modIdHelper, ingredientFilterConfig, ingredients);
		addIngredients(ingredientFilter, filterTextSource, ingredientVisibility, ingredientManager, modIdHelper, ingredientFilterConfig, ingredients);
	}

	@Test
	public void testHidingIngredientsInMultipleContexts() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(ingredientManager);
		Assertions.assertNotNull(ingredientVisibility);
		Assertions.assertNotNull(filterTextSource);
		Assertions.assertNotNull(modIdHelper);
		Assertions.assertNotNull(ingredientFilterConfig);

		List<TestIngredient> ingredients = createIngredients();
		addIngredients(ingredientFilter, filterTextSource, ingredientVisibility, ingredientManager, modIdHelper, ingredientFilterConfig, ingredients);

		ingredientVisibility.hideIngredients(
			TestIngredient.TYPE,
			ingredients,
			Set.of(
				UidContext.Ingredient,
				UidContext.Recipe
			)
		);

		filterTextSource.setFilterText("");
		List<TestIngredient> filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, filteredIngredients.size());
		for (TestIngredient ingredient : ingredients) {
			Assertions.assertFalse(filteredIngredients.contains(ingredient));
			Assertions.assertFalse(ingredientVisibility.isIngredientVisible(
				TestIngredient.TYPE,
				ingredient,
				UidContext.Ingredient
			));
			Assertions.assertFalse(ingredientVisibility.isIngredientVisible(
				TestIngredient.TYPE,
				ingredient,
				UidContext.Recipe
			));
		}

		Collection<TestIngredient> registeredIngredients = ingredientManager.getAllIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT, registeredIngredients.size());
		Assertions.assertTrue(registeredIngredients.containsAll(ingredients));

		ingredientFilter.updateHidden();
		filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, filteredIngredients.size());

		ingredientVisibility.unhideIngredients(
			TestIngredient.TYPE,
			ingredients,
			Set.of(UidContext.Ingredient)
		);
		filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT, filteredIngredients.size());
		for (TestIngredient ingredient : ingredients) {
			Assertions.assertTrue(ingredientVisibility.isIngredientVisible(TestIngredient.TYPE, ingredient));
			Assertions.assertFalse(ingredientVisibility.isIngredientVisible(
				TestIngredient.TYPE,
				ingredient,
				UidContext.Recipe
			));
		}
	}

	@Test
	public void testUnhidingIngredientsWithLowMemorySearch() {
		setup(true);
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(ingredientManager);
		Assertions.assertNotNull(ingredientVisibility);
		Assertions.assertNotNull(filterTextSource);
		Assertions.assertNotNull(modIdHelper);
		Assertions.assertNotNull(ingredientFilterConfig);

		List<TestIngredient> ingredients = createIngredients();
		addIngredients(ingredientFilter, filterTextSource, ingredientVisibility, ingredientManager, modIdHelper, ingredientFilterConfig, ingredients);
		ingredientVisibility.hideIngredients(
			TestIngredient.TYPE,
			ingredients,
			Set.of(UidContext.Ingredient)
		);
		Assertions.assertEquals(
			TestPlugin.BASE_INGREDIENT_COUNT,
			ingredientFilter.getFilteredIngredients(TestIngredient.TYPE).size()
		);

		ingredientVisibility.unhideIngredients(
			TestIngredient.TYPE,
			ingredients,
			Set.of(UidContext.Ingredient)
		);
		Assertions.assertEquals(
			TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT,
			ingredientFilter.getFilteredIngredients(TestIngredient.TYPE).size()
		);
	}

	@Test
	public void testRecipeVisibilityUsesRecipeUid() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(ingredientManager);
		Assertions.assertNotNull(ingredientVisibility);
		Assertions.assertNotNull(filterTextSource);
		Assertions.assertNotNull(modIdHelper);
		Assertions.assertNotNull(ingredientFilterConfig);

		List<TestIngredient> ingredients = createIngredients();
		addIngredients(ingredientFilter, filterTextSource, ingredientVisibility, ingredientManager, modIdHelper, ingredientFilterConfig, ingredients);

		TestIngredient hiddenIngredient = ingredients.getFirst();
		TestIngredient recipeEquivalentIngredient = ingredients.get(2);
		TestIngredient differentRecipeIngredient = ingredients.get(1);
		ingredientVisibility.hideIngredients(
			TestIngredient.TYPE,
			Set.of(hiddenIngredient),
			Set.of(UidContext.Recipe)
		);

		Assertions.assertTrue(ingredientVisibility.isIngredientVisible(
			TestIngredient.TYPE,
			recipeEquivalentIngredient,
			UidContext.Ingredient
		));
		Assertions.assertFalse(ingredientVisibility.isIngredientVisible(
			TestIngredient.TYPE,
			recipeEquivalentIngredient,
			UidContext.Recipe
		));
		Assertions.assertTrue(ingredientVisibility.isIngredientVisible(
			TestIngredient.TYPE,
			differentRecipeIngredient,
			UidContext.Recipe
		));

		List<TestIngredient> filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT, filteredIngredients.size());

		ingredientVisibility.unhideIngredients(
			TestIngredient.TYPE,
			Set.of(hiddenIngredient),
			Set.of(UidContext.Recipe)
		);
		Assertions.assertTrue(ingredientVisibility.isIngredientVisible(
			TestIngredient.TYPE,
			recipeEquivalentIngredient,
			UidContext.Recipe
		));
	}

	@Test
	public void testAddingAndRemovingIngredientsWithTooltipStrings() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(ingredientManager);
		Assertions.assertNotNull(ingredientVisibility);
		Assertions.assertNotNull(filterTextSource);
		Assertions.assertNotNull(modIdHelper);
		Assertions.assertNotNull(ingredientFilterConfig);

		List<TestIngredient> ingredients = createIngredients();
		TestIngredient testIngredient = ingredients.getFirst();
		IIngredientRenderer<TestIngredient> ingredientRenderer = ingredientManager.getIngredientRenderer(TestIngredient.TYPE);
		Set<String> tooltipStrings = getTooltipStrings(ingredientRenderer, testIngredient);

		addIngredients(ingredientFilter, filterTextSource, ingredientVisibility, ingredientManager, modIdHelper, ingredientFilterConfig, ingredients);
		for (String tooltipString : tooltipStrings) {
			filterTextSource.setFilterText(tooltipString);
			List<TestIngredient> filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
			Assertions.assertTrue(filteredIngredients.contains(testIngredient), tooltipString);
		}

		removeIngredients(ingredientFilter, filterTextSource, ingredientVisibility, ingredientManager, modIdHelper, ingredientFilterConfig, ingredients);
		for (String tooltipString : tooltipStrings) {
			filterTextSource.setFilterText(tooltipString);
			List<TestIngredient> filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
			Assertions.assertFalse(filteredIngredients.contains(testIngredient), tooltipString);
		}

		addIngredients(ingredientFilter, filterTextSource, ingredientVisibility, ingredientManager, modIdHelper, ingredientFilterConfig, ingredients);
		for (String tooltipString : tooltipStrings) {
			filterTextSource.setFilterText(tooltipString);
			List<TestIngredient> filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
			Assertions.assertTrue(filteredIngredients.contains(testIngredient), tooltipString);
		}
	}

	@Test
	public void testIngredientAliasesCanBeToggledWithoutRestart() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(filterTextSource);
		Assertions.assertNotNull(ingredientFilterConfig);

		filterTextSource.setFilterText(INGREDIENT_ALIAS);
		List<TestIngredient> filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertFalse(filteredIngredients.contains(ALIASED_INGREDIENT));

		ingredientFilterConfig.searchIngredientAliases().set(true);
		filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(List.of(ALIASED_INGREDIENT), filteredIngredients);

		ingredientFilterConfig.searchIngredientAliases().set(false);
		filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertFalse(filteredIngredients.contains(ALIASED_INGREDIENT));
	}

	@Test
	public void testModAliasesCanBeToggledWithoutRestart() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(filterTextSource);
		Assertions.assertNotNull(ingredientFilterConfig);

		filterTextSource.setFilterText("@" + MOD_ALIAS);
		List<TestIngredient> filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertTrue(filteredIngredients.isEmpty());

		ingredientFilterConfig.searchModAliases().set(true);
		filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, filteredIngredients.size());
		Assertions.assertTrue(filteredIngredients.contains(new TestIngredient(0)));
		Assertions.assertTrue(filteredIngredients.contains(new TestIngredient(1)));

		ingredientFilterConfig.searchModAliases().set(false);
		filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertTrue(filteredIngredients.isEmpty());
	}

	@Test
	public void testConfigBlacklist() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(baseList);
		Assertions.assertNotNull(editModeConfig);

		IListElementInfo<?> elementInfo = baseList.getFirst();
		ITypedIngredient<?> typedIngredient = elementInfo.getTypedIngredient();
		@SuppressWarnings("unchecked")
		ITypedIngredient<TestIngredient> blacklistedIngredient = (ITypedIngredient<TestIngredient>) typedIngredient;
		TestIngredientHelper testIngredientHelper = new TestIngredientHelper();
		editModeConfig.addIngredientToConfigBlacklist(blacklistedIngredient, IEditModeConfig.HideMode.SINGLE, testIngredientHelper);

		ingredientFilter.updateHidden();

		List<?> ingredientList = ingredientFilter.getElements();
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT - 1, ingredientList.size());
	}

	@Test
	public void testConfigBlacklistInEditMode() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(baseList);
		Assertions.assertNotNull(editModeConfig);
		Assertions.assertNotNull(toggleState);

		IListElementInfo<?> elementInfo = baseList.getFirst();
		ITypedIngredient<?> typedIngredient = elementInfo.getTypedIngredient();
		@SuppressWarnings("unchecked")
		ITypedIngredient<TestIngredient> blacklistedIngredient = (ITypedIngredient<TestIngredient>) typedIngredient;
		TestIngredientHelper testIngredientHelper = new TestIngredientHelper();

		toggleState.toggleEditModeEnabled();
		editModeConfig.addIngredientToConfigBlacklist(
			blacklistedIngredient,
			IEditModeConfig.HideMode.SINGLE,
			testIngredientHelper
		);

		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, ingredientFilter.getElements().size());

		toggleState.toggleEditModeEnabled();
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT - 1, ingredientFilter.getElements().size());

		toggleState.toggleEditModeEnabled();
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, ingredientFilter.getElements().size());
	}

	public static Set<String> getTooltipStrings(IIngredientRenderer<TestIngredient> ingredientRenderer, TestIngredient testIngredient) {
		List<Component> components = ingredientRenderer.getTooltip(testIngredient, Item.TooltipContext.EMPTY, null, TooltipFlag.Default.NORMAL);
		return ListElementInfo.getStrings(components);
	}

	public static List<TestIngredient> createIngredients() {
		List<TestIngredient> ingredients = new ArrayList<>();
		for (int i = TestPlugin.BASE_INGREDIENT_COUNT; i < TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT; i++) {
			ingredients.add(new TestIngredient(i));
		}
		Assertions.assertEquals(EXTRA_INGREDIENT_COUNT, ingredients.size());
		return ingredients;
	}

	private static void addIngredients(
		IngredientFilter ingredientFilter,
		IFilterTextSource filterTextSource,
		IIngredientVisibility ingredientVisibility,
		IIngredientManager ingredientManager,
		IModIdHelper modIdHelper,
		TestIngredientFilterConfig ingredientFilterConfig,
		List<TestIngredient> ingredientsToAdd
	) {
		List<IListElementInfo<TestIngredient>> listToAdd = IngredientListElementFactory.createTestList(ingredientManager, TestIngredient.TYPE, ingredientsToAdd, ingredientFilterConfig, modIdHelper);
		Assertions.assertEquals(EXTRA_INGREDIENT_COUNT, listToAdd.size());

		ingredientManager.addIngredientsAtRuntime(TestIngredient.TYPE, ingredientsToAdd);

		Collection<TestIngredient> testIngredients = ingredientManager.getAllIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT, testIngredients.size());
		for (TestIngredient testIngredient : ingredientsToAdd) {
			Assertions.assertTrue(testIngredients.contains(testIngredient));
		}

		filterTextSource.setFilterText("");
		List<TestIngredient> filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT, filteredIngredients.size());
		for (TestIngredient testIngredient : filteredIngredients) {
			Assertions.assertTrue(testIngredients.contains(testIngredient));
		}

		for (TestIngredient ingredient : ingredientsToAdd) {
			Assertions.assertTrue(ingredientVisibility.isIngredientVisible(TestIngredient.TYPE, ingredient));
		}
	}

	private static void removeIngredients(
		IngredientFilter ingredientFilter,
		IFilterTextSource filterTextSource,
		IIngredientVisibility ingredientVisibility,
		IIngredientManager ingredientManager,
		IModIdHelper modIdHelper,
		TestIngredientFilterConfig ingredientFilterConfig,
		List<TestIngredient> ingredientsToRemove
	) {
		List<IListElementInfo<TestIngredient>> listToRemove = IngredientListElementFactory.createTestList(ingredientManager, TestIngredient.TYPE, ingredientsToRemove, ingredientFilterConfig, modIdHelper);
		Assertions.assertEquals(EXTRA_INGREDIENT_COUNT, listToRemove.size());

		ingredientManager.removeIngredientsAtRuntime(TestIngredient.TYPE, ingredientsToRemove);

		filterTextSource.setFilterText("");
		List<TestIngredient> filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, filteredIngredients.size());
		for (TestIngredient testIngredient : filteredIngredients) {
			Assertions.assertFalse(ingredientsToRemove.contains(testIngredient));
		}

		Collection<TestIngredient> testIngredients = ingredientManager.getAllIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, testIngredients.size());
		for (TestIngredient testIngredient : testIngredients) {
			Assertions.assertFalse(ingredientsToRemove.contains(testIngredient));
		}

		for (TestIngredient ingredient : ingredientsToRemove) {
			Assertions.assertFalse(ingredientVisibility.isIngredientVisible(TestIngredient.TYPE, ingredient));
			for (UidContext context : UidContext.values()) {
				Assertions.assertFalse(ingredientVisibility.isIngredientVisible(TestIngredient.TYPE, ingredient, context));
			}
		}

		ingredientVisibility.unhideIngredients(
			TestIngredient.TYPE,
			ingredientsToRemove,
			Set.of(UidContext.values())
		);
		for (TestIngredient ingredient : ingredientsToRemove) {
			for (UidContext context : UidContext.values()) {
				Assertions.assertFalse(ingredientVisibility.isIngredientVisible(TestIngredient.TYPE, ingredient, context));
			}
		}
	}

	private static class NullSerializer implements EditModeConfig.ISerializer {
		@Override
		public void initialize(EditModeConfig config) {

		}

		@Override
		public void save(EditModeConfig config) {

		}

		@Override
		public void load(EditModeConfig config) {

		}
	}

	private static class TestSortingConfig implements ISortingConfig<String> {
		private boolean visible = true;

		public void setVisible(boolean visible) {
			this.visible = visible;
		}

		@Override
		public List<String> getSortedValues(Collection<String> allValues) {
			return allValues.stream()
				.sorted(getComparator(allValues))
				.toList();
		}

		@Override
		public List<String> getDefaultSortedValues(Collection<String> allValues) {
			return getSortedValues(allValues);
		}

		@Override
		public boolean setSortedValues(Collection<String> allValues, List<String> sortedValues) {
			return false;
		}

		@Override
		public Comparator<String> getComparator(Collection<String> allValues) {
			return Comparator.naturalOrder();
		}

		@Override
		public boolean isVisible(Collection<String> allValues, String value) {
			return visible;
		}

		@Override
		public boolean allowsRemovingValues() {
			return true;
		}

		@Override
		public Runnable addChangeListener(Runnable listener) {
			return () -> {};
		}

		@Override
		public ISortingConfig<String> setLegacyMigration(
			List<Path> legacyPaths,
			ISortingConfigMigrator<String> migrator
		) {
			return this;
		}
	}

	private static class TestClientConfigs implements IClientConfigs {
		private final IClientConfig clientConfig;
		private final IIngredientFilterConfig ingredientFilterConfig;
		private final List<Runnable> listenerRemovals = new ArrayList<>();

		private TestClientConfigs(IClientConfig clientConfig, IIngredientFilterConfig ingredientFilterConfig) {
			this.clientConfig = clientConfig;
			this.ingredientFilterConfig = ingredientFilterConfig;
		}

		@Override
		public IClientConfig getClientConfig() {
			return clientConfig;
		}

		@Override
		public IIngredientFilterConfig getIngredientFilterConfig() {
			return ingredientFilterConfig;
		}

		@Override
		public IIngredientGridConfig getIngredientListConfig() {
			throw new UnsupportedOperationException();
		}

		@Override
		public IIngredientGridConfig getBookmarkListConfig() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ISortingConfig<String> getRecipeCategorySortingConfig() {
			return new TestSortingConfig();
		}

		@Override
		public void registerRuntimeListenerRemoval(Runnable listenerRemoval) {
			listenerRemovals.add(listenerRemoval);
		}

		@Override
		public void onRuntimeStopped() {
			listenerRemovals.forEach(Runnable::run);
			listenerRemovals.clear();
		}
	}
}
