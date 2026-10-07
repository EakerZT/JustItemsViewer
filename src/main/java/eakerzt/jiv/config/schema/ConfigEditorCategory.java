package eakerzt.jiv.config.schema;

import eakerzt.jiv.config.api.schema.category.IConfigEditorCategory;
import eakerzt.jiv.config.util.ConfigNameUtil;
import eakerzt.jiv.config.util.ErrorUtil;

public class ConfigEditorCategory implements IConfigEditorCategory {
	private final String name;
	private final String localizationKey;

	public ConfigEditorCategory(String localizationKey, String name) {
		this.name = ConfigNameUtil.validateConfigName(name, "categoryName");
		this.localizationKey = ErrorUtil.checkNotNull(localizationKey, "localizationKey");
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getLocalizationKey() {
		return localizationKey;
	}
}
