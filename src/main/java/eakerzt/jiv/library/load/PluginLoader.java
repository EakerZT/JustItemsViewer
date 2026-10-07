package eakerzt.jiv.library.load;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import eakerzt.jiv.api.IModPlugin;
import eakerzt.jiv.api.constants.VanillaTypes;
import eakerzt.jiv.api.gui.builder.IIngredientAcceptor;
import eakerzt.jiv.api.helpers.IColorHelper;
import eakerzt.jiv.api.helpers.IJivHelpers;
import eakerzt.jiv.api.helpers.IModIdHelper;
import eakerzt.jiv.api.helpers.IStackHelper;
import eakerzt.jiv.api.ingredients.IIngredientHelper;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.advanced.IRecipeButtonControllerFactory;
import eakerzt.jiv.api.recipe.advanced.IRecipeManagerPlugin;
import eakerzt.jiv.api.recipe.category.IRecipeCategory;
import eakerzt.jiv.api.recipe.category.extensions.IRecipeCategoryDecorator;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferHandlerHelper;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferManager;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import eakerzt.jiv.api.registration.ISlotDisplayInterpreterRegistration;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.api.runtime.IJivFeatures;
import eakerzt.jiv.api.runtime.IScreenHelper;
import eakerzt.jiv.api.search.ISearchStorageBuilderFactory;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.config.IClientToggleState;
import eakerzt.jiv.common.network.IConnectionToServer;
import eakerzt.jiv.common.platform.IPlatformFluidHelperInternal;
import eakerzt.jiv.common.platform.Services;
import eakerzt.jiv.common.recipes.BrewingExtensionHelper;
import eakerzt.jiv.common.search.BakedSubstringIndexBuilder;
import eakerzt.jiv.common.util.LoggedTimer;
import eakerzt.jiv.common.util.StackHelper;
import eakerzt.jiv.library.config.EditModeConfig;
import eakerzt.jiv.library.config.IModIdFormatConfig;
import eakerzt.jiv.library.config.RecipeCategorySortingConfig;
import eakerzt.jiv.library.focus.FocusFactory;
import eakerzt.jiv.library.gui.helpers.GuiHelper;
import eakerzt.jiv.library.helpers.CodecHelper;
import eakerzt.jiv.library.helpers.ModIdHelper;
import eakerzt.jiv.library.ingredients.IngredientBlacklistInternal;
import eakerzt.jiv.library.ingredients.IngredientManager;
import eakerzt.jiv.library.ingredients.IIngredientManagerInternal;
import eakerzt.jiv.library.ingredients.IngredientVisibility;
import eakerzt.jiv.library.ingredients.subtypes.SubtypeInterpreters;
import eakerzt.jiv.library.ingredients.subtypes.SubtypeManager;
import eakerzt.jiv.library.load.registration.AdvancedRegistration;
import eakerzt.jiv.library.load.registration.AdvancedSearchRegistration;
import eakerzt.jiv.library.load.registration.GuiHandlerRegistration;
import eakerzt.jiv.library.load.registration.IngredientManagerBuilder;
import eakerzt.jiv.library.load.registration.ModInfoRegistration;
import eakerzt.jiv.library.load.registration.RecipeCatalystRegistration;
import eakerzt.jiv.library.load.registration.RecipeCategoryRegistration;
import eakerzt.jiv.library.load.registration.RecipeManagerPluginHelper;
import eakerzt.jiv.library.load.registration.RecipeRegistration;
import eakerzt.jiv.library.load.registration.RecipeTransferRegistration;
import eakerzt.jiv.library.load.registration.SubtypeRegistration;
import eakerzt.jiv.library.load.registration.VanillaCategoryExtensionRegistration;
import eakerzt.jiv.library.plugins.vanilla.VanillaPlugin;
import eakerzt.jiv.library.plugins.vanilla.VanillaRecipeFactory;
import eakerzt.jiv.library.plugins.vanilla.anvil.SmithingRecipeCategory;
import eakerzt.jiv.library.plugins.vanilla.crafting.CraftingRecipeCategory;
import eakerzt.jiv.library.recipes.RecipeManager;
import eakerzt.jiv.library.recipes.RecipeManagerInternal;
import eakerzt.jiv.library.runtime.JivHelpers;
import eakerzt.jiv.library.startup.StartData;
import eakerzt.jiv.library.transfer.RecipeTransferHandlerHelper;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.function.Consumer;

public final class PluginLoader {
	private PluginLoader() {}

	public static SubtypeManager registerSubtypes(StartData data) {
		IPlatformFluidHelperInternal<?> fluidHelper = Services.PLATFORM.getFluidHelper();
		List<IModPlugin> plugins = data.plugins();
		SubtypeRegistration subtypeRegistration = new SubtypeRegistration();
		PluginCaller.callOnPlugins("Registering item subtypes", plugins, p -> p.registerItemSubtypes(subtypeRegistration));
		PluginCaller.callOnPlugins("Registering fluid subtypes", plugins, p -> {
			p.registerFluidSubtypes(subtypeRegistration, fluidHelper);
		});
		SubtypeInterpreters subtypeInterpreters = subtypeRegistration.getInterpreters();
		return new SubtypeManager(subtypeInterpreters);
	}

	public static IngredientManager registerIngredients(
		StartData data,
		SubtypeManager subtypeManager,
		IColorHelper colorHelper,
		ContextMap contextMap
	) {
		List<IModPlugin> plugins = data.plugins();
		IngredientManagerBuilder ingredientManagerBuilder = new IngredientManagerBuilder(subtypeManager, colorHelper, contextMap);
		PluginCaller.callOnPlugins("Registering ingredients", plugins, ingredientManagerBuilder::registerIngredients);
		PluginCaller.callOnPlugins("Registering extra ingredients", plugins, p -> p.registerExtraIngredients(ingredientManagerBuilder));

		PluginCaller.callOnPlugins("Registering search ingredient aliases", plugins, p -> p.registerIngredientAliases(ingredientManagerBuilder));

		ISlotDisplayInterpreterRegistration slotDisplayInterpreterRegistration = ingredientManagerBuilder.getSlotDisplayInterpreterRegistration();
		PluginCaller.callOnPlugins(
			"Registering slot display interpreters",
			plugins,
			p -> p.registerSlotDisplayInterpreters(slotDisplayInterpreterRegistration)
		);
		return ingredientManagerBuilder.build();
	}

	public static ImmutableSetMultimap<String, String> registerModAliases(StartData data) {
		List<IModPlugin> plugins = data.plugins();
		ModInfoRegistration modInfoRegistration = new ModInfoRegistration();
		PluginCaller.callOnPlugins("Registering Mod Info", plugins, p -> p.registerModInfo(modInfoRegistration));
		return modInfoRegistration.getModAliases();
	}

	public static JivHelpers createJivHelpers(
		ImmutableSetMultimap<String, String> modAliases,
		IModIdFormatConfig modIdFormatConfig,
		IColorHelper colorHelper,
		EditModeConfig editModeConfig,
		FocusFactory focusFactory,
		CodecHelper codecHelper,
		IIngredientManagerInternal ingredientManager,
		SubtypeManager subtypeManager,
		ContextMap contextMap
	) {
		IIngredientHelper<ItemStack> ingredientHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM_STACK);
		VanillaRecipeFactory vanillaRecipeFactory = new VanillaRecipeFactory(ingredientHelper, contextMap);
		StackHelper stackHelper = new StackHelper(subtypeManager);
		GuiHelper guiHelper = new GuiHelper(ingredientManager, contextMap);
		IModIdHelper modIdHelper = new ModIdHelper(
			modIdFormatConfig,
			ingredientManager,
			typedIngredient -> getDisplayModId(ingredientManager, typedIngredient),
			modAliases
		);

		IClientToggleState toggleState = Internal.getClientToggleState();
		IngredientBlacklistInternal blacklist = new IngredientBlacklistInternal(ingredientManager);
		ingredientManager.registerIngredientListener(blacklist);

		IngredientVisibility ingredientVisibility = new IngredientVisibility(
			blacklist,
			toggleState,
			editModeConfig,
			ingredientManager
		);

		return new JivHelpers(
			guiHelper,
			stackHelper,
			modIdHelper,
			focusFactory,
			colorHelper,
			ingredientManager,
			vanillaRecipeFactory,
			codecHelper,
			ingredientVisibility
		);
	}

	private static <T> String getDisplayModId(IIngredientManager ingredientManager, ITypedIngredient<T> typedIngredient) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(typedIngredient.getType());
		return ingredientHelper.getDisplayModId(typedIngredient.getIngredient());
	}

	@Unmodifiable
	private static List<IRecipeCategory<?>> createRecipeCategories(
		List<IModPlugin> plugins,
		VanillaPlugin vanillaPlugin,
		JivHelpers jivHelpers
	) {
		RecipeCategoryRegistration recipeCategoryRegistration = new RecipeCategoryRegistration(jivHelpers);
		PluginCaller.callOnPlugins("Registering categories", plugins, p -> p.registerCategories(recipeCategoryRegistration));
		CraftingRecipeCategory craftingCategory = vanillaPlugin.getCraftingCategory()
			.orElseThrow(() -> new NullPointerException("vanilla crafting category"));
		SmithingRecipeCategory smithingCategory = vanillaPlugin.getSmithingCategory()
			.orElseThrow(() -> new NullPointerException("vanilla smithing category"));
		BrewingExtensionHelper brewingExtensionHelper = vanillaPlugin.getBrewingExtensionHelper()
			.orElseThrow(() -> new NullPointerException("vanilla brewing extension helper"));
		VanillaCategoryExtensionRegistration vanillaCategoryExtensionRegistration = new VanillaCategoryExtensionRegistration(
			craftingCategory,
			smithingCategory,
			brewingExtensionHelper,
			jivHelpers
		);
		PluginCaller.callOnPlugins("Registering vanilla category extensions", plugins, p -> p.registerVanillaCategoryExtensions(vanillaCategoryExtensionRegistration));
		return recipeCategoryRegistration.getRecipeCategories();
	}

	public static IScreenHelper createGuiScreenHelper(List<IModPlugin> plugins, IJivHelpers jivHelpers, IIngredientManagerInternal ingredientManager) {
		GuiHandlerRegistration guiHandlerRegistration = new GuiHandlerRegistration(jivHelpers);
		PluginCaller.callOnPlugins("Registering gui handlers", plugins, p -> p.registerGuiHandlers(guiHandlerRegistration));
		return guiHandlerRegistration.createGuiScreenHelper(ingredientManager);
	}

	public static IRecipeTransferManager createRecipeTransferManager(
		VanillaPlugin vanillaPlugin,
		List<IModPlugin> plugins,
		JivHelpers jivHelpers,
		IConnectionToServer connectionToServer
	) {
		IStackHelper stackHelper = jivHelpers.getStackHelper();
		CraftingRecipeCategory craftingCategory = vanillaPlugin.getCraftingCategory()
			.orElseThrow(() -> new NullPointerException("vanilla crafting category"));
		IIngredientManager ingredientManager = jivHelpers.getIngredientManager();
		IRecipeTransferHandlerHelper handlerHelper = new RecipeTransferHandlerHelper(stackHelper, ingredientManager, craftingCategory, connectionToServer);
		RecipeTransferRegistration recipeTransferRegistration = new RecipeTransferRegistration(stackHelper, handlerHelper, jivHelpers, connectionToServer);
		PluginCaller.callOnPlugins("Registering recipes transfer handlers", plugins, p -> p.registerRecipeTransferHandlers(recipeTransferRegistration));
		return recipeTransferRegistration.createRecipeTransferManager();
	}

	public static ISearchStorageBuilderFactory createSearchStorageFactory(List<IModPlugin> plugins) {
		ISearchStorageBuilderFactory defaultSearchStorageBuilderFactory = BakedSubstringIndexBuilder::new;
		AdvancedSearchRegistration searchRegistration = new AdvancedSearchRegistration(defaultSearchStorageBuilderFactory);
		PluginCaller.callOnPlugins("Registering advanced search", plugins, p -> p.registerAdvancedSearch(searchRegistration));
		return searchRegistration.getSearchStorageBuilderFactoryOverride()
			.orElse(defaultSearchStorageBuilderFactory);
	}

	public static RecipeManager createRecipeManager(
		List<IModPlugin> plugins,
		VanillaPlugin vanillaPlugin,
		RecipeCategorySortingConfig recipeCategorySortingConfig,
		JivHelpers jivHelpers,
		IIngredientManagerInternal ingredientManager,
		ContextMap contextMap
	) {
		List<IRecipeCategory<?>> recipeCategories = createRecipeCategories(plugins, vanillaPlugin, jivHelpers);

		RecipeCatalystRegistration recipeCatalystRegistration = new RecipeCatalystRegistration(ingredientManager, jivHelpers, contextMap);
		PluginCaller.callOnPlugins("Registering recipe catalysts", plugins, p -> p.registerRecipeCatalysts(recipeCatalystRegistration));
		ImmutableListMultimap<IRecipeType<?>, Consumer<IIngredientAcceptor<?>>> craftingStations = recipeCatalystRegistration.getCraftingStations();

		LoggedTimer timer = new LoggedTimer();
		timer.start("Building recipe registry");
		RecipeManagerInternal recipeManagerInternal = new RecipeManagerInternal(
			recipeCategories,
			craftingStations,
			ingredientManager,
			contextMap,
			recipeCategorySortingConfig,
			jivHelpers.getIngredientVisibility()
		);
		timer.stop();

		IJivFeatures jivFeatures = Internal.getJivFeatures();
		RecipeManagerPluginHelper recipeManagerPluginHelper = new RecipeManagerPluginHelper(recipeManagerInternal);
		AdvancedRegistration advancedRegistration = new AdvancedRegistration(jivHelpers, recipeManagerPluginHelper);
		PluginCaller.callOnPlugins("Registering advanced plugins", plugins, p -> p.registerAdvanced(advancedRegistration));

		List<IRecipeManagerPlugin> recipeManagerPlugins = advancedRegistration.getRecipeManagerPlugins();
		List<IRecipeButtonControllerFactory> recipeButtonControllerFactories = advancedRegistration.getRecipeButtonControllerFactories();
		ImmutableListMultimap<IRecipeType<?>, IRecipeCategoryDecorator<?>> recipeCategoryDecorators = advancedRegistration.getRecipeCategoryDecorators();
		recipeManagerInternal.addPlugins(recipeManagerPlugins);

		RecipeRegistration recipeRegistration = new RecipeRegistration(jivHelpers, ingredientManager, recipeManagerInternal, contextMap);
		PluginCaller.callOnPlugins("Registering recipes", plugins, p -> p.registerRecipes(recipeRegistration));

		recipeManagerInternal.compact();

		return new RecipeManager(recipeManagerInternal, ingredientManager, recipeCategoryDecorators, recipeButtonControllerFactories, contextMap);
	}
}
