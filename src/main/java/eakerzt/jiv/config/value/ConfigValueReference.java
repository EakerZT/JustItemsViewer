package eakerzt.jiv.config.value;

import eakerzt.jiv.config.util.ErrorUtil;

public record ConfigValueReference(
	String categoryName,
	String valueName
) {
	public ConfigValueReference {
		categoryName = ErrorUtil.checkNotNull(categoryName, "categoryName");
		valueName = ErrorUtil.checkNotNull(valueName, "valueName");
	}
}
