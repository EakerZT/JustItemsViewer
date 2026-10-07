package eakerzt.jiv.gui.config;

import eakerzt.jiv.api.constants.ModIds;
import eakerzt.jiv.api.runtime.IJivRuntime;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.config.IClientConfig;
import eakerzt.jiv.common.config.IClientConfigs;
import eakerzt.jiv.common.config.IClientToggleState;
import eakerzt.jiv.gui.config.sorting.SortingOrderConfigValues;
import eakerzt.jiv.gui.util.CheatModeUtil;
import eakerzt.jiv.config.api.value.serializer.IConfigValueSerializer;
import eakerzt.jiv.config.gui.api.ConfigGuiPlugin;
import eakerzt.jiv.config.gui.api.IConfigGuiPlugin;
import eakerzt.jiv.config.gui.api.IConfigGuiRegistration;
import eakerzt.jiv.config.gui.api.IConfigScreenCategoryBuilder;
import eakerzt.jiv.config.gui.api.IConfigScreenBuilder;
import eakerzt.jiv.config.gui.api.IConfigScreenFactory;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * JIV config GUI customizations.
 */
@ConfigGuiPlugin
public class JivConfigGuiPlugin implements IConfigGuiPlugin {
	@Nullable
	private static IConfigScreenFactory screenFactory;

	@Override
	public void onScreenFactoryAvailable(IConfigScreenFactory screenFactory) {
		JivConfigGuiPlugin.screenFactory = screenFactory;
	}

	public static Optional<Screen> createScreen(@Nullable Screen parent) {
		IConfigScreenFactory factory = screenFactory;
		if (factory == null) {
			return Optional.empty();
		}
		return Optional.of(factory.create(parent));
	}

	@Override
	public String getModId() {
		return ModIds.JIV_ID;
	}

	@Override
	public void register(IConfigGuiRegistration registration) {
		registration.registerValueEditor(AlignmentConfigValueGuiAdapter.EDITOR_TYPE, ignored -> new AlignmentConfigValueEditor());
		registration.configureScreen(screenBuilder -> {
			screenBuilder.setTitle(Component.translatable("jiv.config"));
			screenBuilder.configureCategory("debug")
				.clearDefaultValues();
			configureAlignmentValues(screenBuilder);
			screenBuilder.configureCategory("input")
				.addKeyMappings(Internal.getKeyMappings().getConfigKeyMappings());
			Internal.getOptionalJivRuntime()
				.ifPresent(runtime -> {
					configureRuntimeToggleValues(screenBuilder);
					if (Internal.getJivFeatures().isJivGuiEnabled()) {
						configureSortingOrderCategories(screenBuilder, runtime, JivGuiSortingConfigRegistration.get());
					}
				});
		});
	}

	private static void configureAlignmentValues(IConfigScreenBuilder screenBuilder) {
		IClientConfigs clientConfigs = Internal.getClientConfigs();

		IConfigScreenCategoryBuilder ingredientList = screenBuilder.configureCategory("ingredientList");
		ingredientList.getValueBuilderByName("maxColumns")
			.insertAfter(new AlignmentConfigValueGuiAdapter(
				"jiv.config.client.ingredientList.alignment",
				clientConfigs.getIngredientListConfig().horizontalAlignment(),
				clientConfigs.getIngredientListConfig().verticalAlignment()
			));
		ingredientList.getValueBuilderByName("horizontalAlignment")
			.hide();
		ingredientList.getValueBuilderByName("verticalAlignment")
			.hide();

		IConfigScreenCategoryBuilder bookmarkList = screenBuilder.configureCategory("bookmarkList");
		bookmarkList.getValueBuilderByName("maxColumns")
			.insertAfter(new AlignmentConfigValueGuiAdapter(
				"jiv.config.client.bookmarkList.alignment",
				clientConfigs.getBookmarkListConfig().horizontalAlignment(),
				clientConfigs.getBookmarkListConfig().verticalAlignment()
			));
		bookmarkList.getValueBuilderByName("horizontalAlignment")
			.hide();
		bookmarkList.getValueBuilderByName("verticalAlignment")
			.hide();
	}

	private static void configureRuntimeToggleValues(IConfigScreenBuilder screenBuilder) {
		IClientConfigs clientConfigs = Internal.getClientConfigs();
		IClientConfig clientConfig = clientConfigs.getClientConfig();
		IClientToggleState toggleState = Internal.getClientToggleState();
		IConfigValueSerializer<Boolean> serializer = clientConfig.cheatToHotbarUsingHotkeysEnabled().getEditorInfo().getSerializer();

		screenBuilder.configureCategory("ingredientList")
			.addScreenValue(new RuntimeToggleScreenValue(
				"overlaysEnabled",
				"jiv.config.client.ingredientList.overlaysEnabled",
				true,
				toggleState::isOverlayEnabled,
				toggleState::setOverlayEnabled,
				toggleState::addOverlayEnabledListener,
				serializer
			));

		screenBuilder.configureCategory("bookmarkList")
			.addScreenValue(new RuntimeToggleScreenValue(
				"bookmarkOverlayEnabled",
				"jiv.config.client.bookmarkList.bookmarkOverlayEnabled",
				true,
				toggleState::isBookmarkEnabled,
				toggleState::setBookmarkEnabled,
				toggleState::addBookmarkEnabledListener,
				serializer
			));

		screenBuilder.configureCategory("cheating")
			.addScreenValue(new RuntimeToggleScreenValue(
				"cheatModeEnabled",
				"jiv.config.client.cheating.cheatModeEnabled",
				false,
				toggleState::isCheatItemsEnabled,
				value -> CheatModeUtil.setCheatModeEnabled(toggleState, value),
				toggleState::addCheatItemsEnabledListener,
				serializer
			));

		screenBuilder.configureCategory("advanced")
			.addScreenValue(new RuntimeToggleScreenValue(
				"editModeEnabled",
				"jiv.config.client.advanced.editModeEnabled",
				false,
				toggleState::isEditModeEnabled,
				toggleState::setEditModeEnabled,
				toggleState::addEditModeEnabledListener,
				serializer
			));
	}

	private static void configureSortingOrderCategories(
		IConfigScreenBuilder screenBuilder,
		IJivRuntime runtime,
		JivGuiSortingConfigData sortingConfigData
	) {
		IClientConfigs clientConfigs = Internal.getClientConfigs();
		SortingOrderConfigValues sortingOrderConfigValues = new SortingOrderConfigValues(runtime);
		List<String> recipeCategorySortOrderValues = sortingOrderConfigValues.getRecipeCategorySortOrderValues();
		List<String> ingredientModNameSortOrderValues = sortingOrderConfigValues.getIngredientModNameSortOrderValues();
		List<String> ingredientTypeSortOrderValues = sortingOrderConfigValues.getIngredientTypeSortOrderValues();

		screenBuilder.configureCategory("recipeCategorySorting")
			.setTitle(Component.translatable("jiv.config.client.recipeCategorySorting"))
			.setDescription(Component.translatable("jiv.config.client.recipeCategorySorting.description"))
			.addStringSortingConfig(
				"recipeCategorySortOrder",
				"jiv.config.client.sorting.recipeCategorySortOrder",
				clientConfigs.getRecipeCategorySortingConfig(),
				recipeCategorySortOrderValues
			)
			.setValueName(sortingOrderConfigValues::getRecipeCategorySortOrderValueName)
			.setValueDescription(SortingOrderConfigValues::getRecipeCategorySortOrderValueDescription)
			.setValueIcon(sortingOrderConfigValues::getRecipeCategorySortOrderValueIcon);

		IConfigScreenCategoryBuilder ingredientSorting = screenBuilder.configureCategory("ingredientSorting");
		ingredientSorting.addStringSortingConfig(
				"ingredientTypeSortOrder",
				"jiv.config.client.sorting.ingredientTypeSortOrder",
				sortingConfigData.ingredientTypeSortingConfig(),
				ingredientTypeSortOrderValues
			)
			.setValueName(sortingOrderConfigValues::getIngredientTypeSortOrderValueName)
			.setValueDescription(SortingOrderConfigValues::getIngredientTypeSortOrderValueDescription)
			.setValueIcon(sortingOrderConfigValues::getIngredientTypeSortOrderValueIcon);

		ingredientSorting.addStringSortingConfig(
				"ingredientModNameSortOrder",
				"jiv.config.client.sorting.ingredientModNameSortOrder",
				sortingConfigData.ingredientModNameSortingConfig(),
				ingredientModNameSortOrderValues
			)
			.setValueName(Component::literal)
			.setValueDescription(SortingOrderConfigValues::getIngredientModNameSortOrderValueDescription)
			.setValueIcon(sortingOrderConfigValues::getIngredientModNameSortOrderValueIcon);
	}
}
