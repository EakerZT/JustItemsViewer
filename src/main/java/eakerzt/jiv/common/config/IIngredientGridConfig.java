package eakerzt.jiv.common.config;

import eakerzt.jiv.api.gui.placement.HorizontalAlignment;
import eakerzt.jiv.api.gui.placement.VerticalAlignment;
import eakerzt.jiv.config.api.value.IConfigValue;

public interface IIngredientGridConfig {
	IConfigValue<Integer> maxColumns();

	int getMinColumns();

	IConfigValue<Integer> maxRows();

	int getMinRows();

	IConfigValue<Boolean> drawBackground();

	IConfigValue<IngredientGridLayoutMode> layoutMode();

	IConfigValue<HorizontalAlignment> horizontalAlignment();

	IConfigValue<VerticalAlignment> verticalAlignment();

	IConfigValue<NavigationVisibility> navigationVisibility();

	IConfigValue<IngredientGridNavigationMode> navigationMode();
}
