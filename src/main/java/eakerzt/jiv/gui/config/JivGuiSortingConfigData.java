package eakerzt.jiv.gui.config;

import eakerzt.jiv.config.api.sorting.ISortingConfig;

public record JivGuiSortingConfigData(
	ISortingConfig<String> ingredientModNameSortingConfig,
	ISortingConfig<String> ingredientTypeSortingConfig
) {
}
