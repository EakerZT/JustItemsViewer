package eakerzt.jiv.config.schema;

import eakerzt.jiv.config.api.schema.builder.IConfigEditorCategoryBuilder;
import eakerzt.jiv.config.util.ConfigNameUtil;
import eakerzt.jiv.config.util.ErrorUtil;
import org.jetbrains.annotations.Nullable;

public class ConfigEditorCategoryBuilder implements IConfigEditorCategoryBuilder {
	private final String name;
	private final String localizationKey;
	private final @Nullable ConfigSchemaBuilder schemaBuilder;

	public ConfigEditorCategoryBuilder(String localizationPath, String name) {
		this(null, localizationPath, name);
	}

	ConfigEditorCategoryBuilder(@Nullable ConfigSchemaBuilder schemaBuilder, String localizationPath, String name) {
		this.name = ConfigNameUtil.validateConfigName(name, "categoryName");
		localizationPath = ErrorUtil.checkNotNull(localizationPath, "localizationPath");
		this.localizationKey = localizationPath + "." + this.name;
		this.schemaBuilder = schemaBuilder;
	}

	public String getName() {
		return name;
	}

	public String getLocalizationKey() {
		return localizationKey;
	}

	@Nullable
	public ConfigSchemaBuilder getSchemaBuilder() {
		return schemaBuilder;
	}

	public ConfigEditorCategory build() {
		return new ConfigEditorCategory(localizationKey, name);
	}
}
