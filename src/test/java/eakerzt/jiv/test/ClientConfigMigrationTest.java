package eakerzt.jiv.test;

import eakerzt.jiv.api.gui.placement.HorizontalAlignment;
import eakerzt.jiv.api.gui.placement.VerticalAlignment;
import eakerzt.jiv.common.config.BookmarkAddPosition;
import eakerzt.jiv.common.config.ClientConfigs;
import eakerzt.jiv.common.config.DebugConfig;
import eakerzt.jiv.common.config.GiveMode;
import eakerzt.jiv.common.config.HistoryDisplaySide;
import eakerzt.jiv.common.config.IClientConfig;
import eakerzt.jiv.common.config.IIngredientFilterConfig;
import eakerzt.jiv.common.config.IIngredientGridConfig;
import eakerzt.jiv.common.config.IngredientGridLayoutMode;
import eakerzt.jiv.common.config.IngredientGridNavigationMode;
import eakerzt.jiv.common.config.IngredientSortStage;
import eakerzt.jiv.common.config.NavigationVisibility;
import eakerzt.jiv.common.config.SearchBarPosition;
import eakerzt.jiv.common.config.SearchMode;
import eakerzt.jiv.common.config.legacy.LegacyConfigPaths;
import eakerzt.jiv.config.api.sorting.ISortingConfig;
import eakerzt.jiv.config.file.ConfigFileWatcherSettings;
import eakerzt.jiv.config.file.ConfigFileUtil;
import eakerzt.jiv.config.file.ConfigManager;
import eakerzt.jiv.config.schema.ConfigSchemaBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClientConfigMigrationTest {
	@Test
	public void bookmarkAndHistoryVisibilityDefaultsAreSavedAndEditable(@TempDir Path tempDir) {
		ConfigFileWatcherSettings watcher = ConfigFileWatcherSettings.clientDefaults().withEnabled(false);
		ConfigManager manager = new ConfigManager("Visibility Defaults Test", watcher, watcher);
		Path configFile = tempDir.resolve("jiv-client.ini");
		var sorting = manager.createInMemorySortingConfig(Comparator.<String>naturalOrder(), true);
		ClientConfigs configs = new ClientConfigs(new ConfigSchemaBuilder("jiv", configFile, "jiv.config.client", manager), false, sorting);
		IClientConfig client = configs.getClientConfig();
		assertTrue(client.bookmarkEnabled().get());
		assertTrue(client.lookupHistoryEnabled().get());
		assertEquals(HistoryDisplaySide.RIGHT, client.lookupHistoryDisplaySide().get());
		client.bookmarkEnabled().set(false);
		client.lookupHistoryEnabled().set(false);
		client.lookupHistoryDisplaySide().set(HistoryDisplaySide.LEFT);
		((eakerzt.jiv.config.internal.scheduler.DelayedExecutor) manager.getSaveScheduler()).shutdown();
		ConfigManager reloadedManager = new ConfigManager("Visibility Reload Test", watcher, watcher);
		var reloadedSorting = reloadedManager.createInMemorySortingConfig(Comparator.<String>naturalOrder(), true);
		ClientConfigs reloaded = new ClientConfigs(new ConfigSchemaBuilder("jiv", configFile, "jiv.config.client", reloadedManager), false, reloadedSorting);
		assertFalse(reloaded.getClientConfig().bookmarkEnabled().get());
		assertFalse(reloaded.getClientConfig().lookupHistoryEnabled().get());
		assertEquals(HistoryDisplaySide.LEFT, reloaded.getClientConfig().lookupHistoryDisplaySide().get());
	}
	@ParameterizedTest
	@ValueSource(strings = {"resourceLocationSearchMode", "identifierSearchMode"})
	public void loadsEveryReleasedClientConfigValue(String identifierSearchKey, @TempDir Path tempDir) throws IOException {
		Path configDirectory = tempDir.resolve("jiv");
		UUID profileId = UUID.randomUUID();
		Path rootLegacyFile = configDirectory.resolve("jiv-client.ini");
		Path legacyFile = configDirectory.resolve("players").resolve(profileId.toString()).resolve("jiv-client.ini");
		Path configFile = configDirectory.resolve("client").resolve("jiv-client.ini");
		Files.createDirectories(legacyFile.getParent());
		Files.writeString(rootLegacyFile, "[appearance]\ncenterSearch = false\n");
		Files.writeString(legacyFile, """
			[appearance]
			centerSearch = true
			recipeGuiHeight = 411
			toastReflowEnabled = false

			[cheating]
			giveMode = INVENTORY
			cheatToHotbarUsingHotkeysEnabled = true
			showHiddenIngredients = true
			showTagRecipesEnabled = false

			[bookmarks]
			addBookmarksToFrontEnabled = true
			bookmarkOutputAsRecipe = false
			dragToRearrangeBookmarksEnabled = false

			[tooltips]
			bookmarkTooltipFeatures = INGREDIENTS
			holdShiftToShowBookmarkTooltipFeatures = false
			showCreativeTabNamesEnabled = true
			tagContentTooltipEnabled = false
			hideSingleTagContentTooltipEnabled = false
			enableRecipesGuiIngredientsSummary = true

			[performance]
			lowMemorySlowSearchEnabled = true

			[lookups]
			lookupFluidContentsEnabled = true
			lookupBlockTagsEnabled = false

			[lookupHistory]
			enabled = true
			maxRows = 6
			maxIngredients = 321
			displaySide = RIGHT

			[advanced]
			catchRenderErrorsEnabled = false
			recipeSyncWarningEnabled = false

			[input]
			dragDelayInMilliseconds = 234
			smoothScrollRate = 17
			recipeSlotCyclingEnabled = false

			[sorting]
			ingredientSortStages = ALPHABETICAL, MOD_NAME
			recipeSorterStages = CRAFTABLE

			[search]
			modNameSearchMode = ENABLED
			tagSearchMode = DISABLED
			tooltipSearchMode = REQUIRE_PREFIX
			colorSearchMode = ENABLED
			%s = REQUIRE_PREFIX
			creativeTabSearchMode = ENABLED
			searchAdvancedTooltips = true
			searchModIds = false
			searchModAliases = false
			searchShortModNames = true
			searchIngredientAliases = false

			[ingredientList]
			maxRows = 12
			maxColumns = 7
			horizontalAlignment = LEFT
			verticalAlignment = BOTTOM
			buttonNavigationVisibility = DISABLED
			drawBackground = true
			layoutMode = MAXIMIZE_AVAILABLE_SPACE
			navigationMode = SCROLLING

			[bookmarkList]
			maxRows = 13
			maxColumns = 6
			horizontalAlignment = RIGHT
			verticalAlignment = CENTER
			buttonNavigationVisibility = AUTO_HIDE
			drawBackground = true
			layoutMode = MAXIMIZE_AVAILABLE_SPACE
			navigationMode = SMOOTH_SCROLLING
			""".formatted(identifierSearchKey));

		ConfigFileWatcherSettings disabledWatcher = ConfigFileWatcherSettings.clientDefaults().withEnabled(false);
		ConfigManager configManager = new ConfigManager("JIV Config Migration Test", disabledWatcher, disabledWatcher);
		ISortingConfig<String> recipeSorting = configManager.createInMemorySortingConfig(Comparator.naturalOrder(), true);
		ConfigSchemaBuilder schemaBuilder = new ConfigSchemaBuilder("jiv", configFile, "jiv.config.client", configManager);
		schemaBuilder.setLegacySources(LegacyConfigPaths.get(configDirectory, profileId, "jiv-client.ini"));
		ClientConfigs configs = new ClientConfigs(
			schemaBuilder,
			false,
			recipeSorting
		);

		IClientConfig client = configs.getClientConfig();
		assertEquals(SearchBarPosition.CENTERED, client.searchBarPosition().get());
		assertEquals(411, client.maxRecipeGuiHeight().get());
		assertFalse(client.toastReflowEnabled().get());
		assertEquals(GiveMode.INVENTORY, client.giveMode().get());
		assertTrue(client.cheatToHotbarUsingHotkeysEnabled().get());
		assertTrue(client.showHiddenIngredients().get());
		assertFalse(client.showTagRecipesEnabled().get());
		assertEquals(BookmarkAddPosition.FRONT, client.bookmarkAddPosition().get());
		assertFalse(client.dragToRearrangeBookmarksEnabled().get());
		assertFalse(client.bookmarkTooltipPreviewEnabled().get());
		assertTrue(client.bookmarkTooltipIngredientsEnabled().get());
		assertFalse(client.holdShiftToShowBookmarkTooltipFeaturesEnabled().get());
		assertTrue(client.showCreativeTabNamesEnabled().get());
		assertFalse(client.tagContentTooltipEnabled().get());
		assertFalse(client.hideSingleTagContentTooltipEnabled().get());
		assertTrue(client.ingredientsSummaryEnabled().get());
		assertTrue(client.lowMemorySlowSearchEnabled().get());
		assertTrue(client.lookupFluidContentsEnabled().get());
		assertFalse(client.lookupBlockTagsEnabled().get());
		assertTrue(client.lookupHistoryEnabled().get());
		assertEquals(6, client.maxLookupHistoryRows().get());
		assertEquals(321, client.maxLookupHistoryIngredients().get());
		assertEquals(HistoryDisplaySide.RIGHT, client.lookupHistoryDisplaySide().get());
		assertFalse(client.catchRenderErrorsEnabled().get());
		assertFalse(client.recipeSyncWarningEnabled().get());
		assertEquals(234, client.dragDelayMs().get());
		assertEquals(17, client.smoothScrollRate().get());
		assertFalse(client.recipeSlotCyclingEnabled().get());
		assertEquals(List.of(IngredientSortStage.ALPHABETICAL, IngredientSortStage.MOD_NAME), client.ingredientSorterStages().get());
		assertFalse(client.recipeSortingBookmarksEnabled().get());
		assertTrue(client.recipeSortingCraftableEnabled().get());

		IIngredientFilterConfig filter = configs.getIngredientFilterConfig();
		assertEquals(SearchMode.ENABLED, filter.modNameSearchMode().get());
		assertEquals(SearchMode.DISABLED, filter.tagSearchMode().get());
		assertEquals(SearchMode.REQUIRE_PREFIX, filter.tooltipSearchMode().get());
		assertEquals(SearchMode.ENABLED, filter.colorSearchMode().get());
		assertEquals(SearchMode.REQUIRE_PREFIX, filter.identifierSearchMode().get());
		assertEquals(SearchMode.ENABLED, filter.creativeTabSearchMode().get());
		assertTrue(filter.searchAdvancedTooltips().get());
		assertFalse(filter.searchModIds().get());
		assertFalse(filter.searchModAliases().get());
		assertTrue(filter.searchShortModNames().get());
		assertFalse(filter.searchIngredientAliases().get());

		assertGridConfig(
			configs.getIngredientListConfig(),
			12,
			7,
			HorizontalAlignment.LEFT,
			VerticalAlignment.BOTTOM,
			NavigationVisibility.DISABLED,
			IngredientGridLayoutMode.MAXIMIZE_AVAILABLE_SPACE,
			IngredientGridNavigationMode.SCROLLING
		);
		assertGridConfig(
			configs.getBookmarkListConfig(),
			13,
			6,
			HorizontalAlignment.RIGHT,
			VerticalAlignment.CENTER,
			NavigationVisibility.DISABLED,
			IngredientGridLayoutMode.MAXIMIZE_AVAILABLE_SPACE,
			IngredientGridNavigationMode.SCROLLING
		);
		assertTrue(Files.exists(configFile));
		assertEquals(Files.readString(legacyFile), Files.readString(ConfigFileUtil.getBackupPath(legacyFile, 1)));
		assertTrue(Files.notExists(ConfigFileUtil.getBackupPath(rootLegacyFile, 1)));

		ConfigManager reloadedConfigManager = new ConfigManager("Reloaded JIV Client Config", disabledWatcher, disabledWatcher);
		ISortingConfig<String> reloadedRecipeSorting = reloadedConfigManager.createInMemorySortingConfig(Comparator.naturalOrder(), true);
		ClientConfigs reloaded = new ClientConfigs(
			new ConfigSchemaBuilder("jiv", configFile, "jiv.config.client", reloadedConfigManager),
			false,
			reloadedRecipeSorting
		);
		assertFalse(reloaded.getClientConfig().recipeSyncWarningEnabled().get());
		assertFalse(reloaded.getClientConfig().recipeSlotCyclingEnabled().get());
		assertEquals(IngredientGridLayoutMode.MAXIMIZE_AVAILABLE_SPACE, reloaded.getIngredientListConfig().layoutMode().get());
		assertEquals(IngredientGridNavigationMode.SCROLLING, reloaded.getBookmarkListConfig().navigationMode().get());
	}

	@Test
	public void migratesValidLegacyValuesWhenAnotherValueIsInvalid(@TempDir Path tempDir) throws IOException {
		Path configDirectory = tempDir.resolve("jiv");
		Path legacyFile = configDirectory.resolve("jiv-client.ini");
		Path configFile = configDirectory.resolve("client").resolve("jiv-client.ini");
		Files.createDirectories(configDirectory);
		Files.writeString(legacyFile, """
			[appearance]
			centerSearch = true
			recipeGuiHeight = invalid
			""");

		ConfigFileWatcherSettings disabledWatcher = ConfigFileWatcherSettings.clientDefaults().withEnabled(false);
		ConfigManager configManager = new ConfigManager("Partial JIV Config Migration Test", disabledWatcher, disabledWatcher);
		ISortingConfig<String> recipeSorting = configManager.createInMemorySortingConfig(Comparator.naturalOrder(), true);
		ConfigSchemaBuilder schemaBuilder = new ConfigSchemaBuilder("jiv", configFile, "jiv.config.client", configManager);
		schemaBuilder.setLegacySources(LegacyConfigPaths.get(configDirectory, UUID.randomUUID(), "jiv-client.ini"));
		ClientConfigs configs = new ClientConfigs(
			schemaBuilder,
			false,
			recipeSorting
		);

		IClientConfig client = configs.getClientConfig();
		assertEquals(SearchBarPosition.CENTERED, client.searchBarPosition().get());
		assertEquals(IClientConfig.defaultRecipeGuiHeight, client.maxRecipeGuiHeight().get());
		assertTrue(Files.exists(configFile));
		assertEquals(Files.readString(legacyFile), Files.readString(ConfigFileUtil.getBackupPath(legacyFile, 1)));
	}

	private static void assertGridConfig(
		IIngredientGridConfig config,
		int maxRows,
		int maxColumns,
		HorizontalAlignment horizontalAlignment,
		VerticalAlignment verticalAlignment,
		NavigationVisibility navigationVisibility,
		IngredientGridLayoutMode layoutMode,
		IngredientGridNavigationMode navigationMode
	) {
		assertEquals(maxRows, config.maxRows().get());
		assertEquals(maxColumns, config.maxColumns().get());
		assertEquals(horizontalAlignment, config.horizontalAlignment().get());
		assertEquals(verticalAlignment, config.verticalAlignment().get());
		assertEquals(navigationVisibility, config.navigationVisibility().get());
		assertTrue(config.drawBackground().get());
		assertEquals(layoutMode, config.layoutMode().get());
		assertEquals(navigationMode, config.navigationMode().get());
	}

	@Test
	public void migratesLegacyDebugConfigTransactionallyThroughMezzConfig(@TempDir Path tempDir) throws IOException {
		Path configDirectory = tempDir.resolve("jiv");
		Path legacyFile = configDirectory.resolve("jiv-debug.ini");
		Path configFile = configDirectory.resolve("client").resolve("jiv-debug.ini");
		Files.createDirectories(configDirectory);
		Files.writeString(legacyFile, """
			[debug]
			debugMode = true
			debugGuis = true
			debugInputs = true
			debugInfoTooltipsEnabled = true
			logSuffixTreeStats = true
			""");

		ConfigFileWatcherSettings disabledWatcher = ConfigFileWatcherSettings.clientDefaults().withEnabled(false);
		ConfigManager configManager = new ConfigManager("JIV Debug Config Migration Test", disabledWatcher, disabledWatcher);
		ConfigSchemaBuilder schemaBuilder = new ConfigSchemaBuilder("jiv", configFile, "jiv.config.debug", configManager);
		DebugConfig.create(schemaBuilder);
		schemaBuilder.setLegacySources(LegacyConfigPaths.get(configDirectory, UUID.randomUUID(), "jiv-debug.ini"));
		schemaBuilder.build();

		assertTrue(DebugConfig.isDebugIngredientsEnabled());
		assertTrue(DebugConfig.isDebugGuisEnabled());
		assertTrue(DebugConfig.isDebugInputsEnabled());
		assertTrue(DebugConfig.isDebugInfoTooltipsEnabled());
		assertTrue(DebugConfig.isLogSuffixTreeStatsEnabled());
		assertTrue(Files.exists(configFile));
		assertEquals(Files.readString(legacyFile), Files.readString(ConfigFileUtil.getBackupPath(legacyFile, 1)));
	}
}
