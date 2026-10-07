package eakerzt.jiv.config.server;

import eakerzt.jiv.config.schema.ConfigSchema;
import eakerzt.jiv.config.util.ErrorUtil;

public record ServerConfigKey(String modId, String configFileName) {
	public ServerConfigKey {
		modId = ConfigSchema.validateModId(modId);
		configFileName = ErrorUtil.checkNotNull(configFileName, "configFileName");
		if (configFileName.isBlank()) {
			throw new IllegalArgumentException("configFileName must not be blank.");
		}
	}
}
