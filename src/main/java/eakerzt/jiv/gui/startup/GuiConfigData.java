package eakerzt.jiv.gui.startup;

import eakerzt.jiv.common.platform.Services;
import eakerzt.jiv.gui.config.BookmarkJsonConfig;
import eakerzt.jiv.gui.config.IBookmarkConfig;
import eakerzt.jiv.gui.config.ILookupHistoryConfig;
import eakerzt.jiv.gui.config.IngredientTypeSortingConfig;
import eakerzt.jiv.gui.config.JivGuiSortingConfigData;
import eakerzt.jiv.gui.config.LookupHistoryJsonConfig;
import eakerzt.jiv.gui.config.ModNameSortingConfig;

import java.nio.file.Path;

public record GuiConfigData(
	IBookmarkConfig bookmarkConfig,
	ILookupHistoryConfig lookupHistoryConfig,
	ModNameSortingConfig modNameSortingConfig,
	IngredientTypeSortingConfig ingredientTypeSortingConfig
) {
	public static GuiConfigData create(JivGuiSortingConfigData sortingConfigData) {
		Path configDir = Services.PLATFORM.getConfigHelper().createJivConfigDir();

		IBookmarkConfig bookmarkConfig = new BookmarkJsonConfig(configDir);
		ILookupHistoryConfig lookupHistoryConfig = new LookupHistoryJsonConfig(configDir);
		ModNameSortingConfig ingredientModNameSortingConfig = new ModNameSortingConfig(sortingConfigData.ingredientModNameSortingConfig());
		IngredientTypeSortingConfig ingredientTypeSortingConfig = new IngredientTypeSortingConfig(sortingConfigData.ingredientTypeSortingConfig());

		return new GuiConfigData(
			bookmarkConfig,
			lookupHistoryConfig,
			ingredientModNameSortingConfig,
			ingredientTypeSortingConfig
		);
	}
}
