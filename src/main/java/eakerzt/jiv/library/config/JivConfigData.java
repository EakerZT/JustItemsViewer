package eakerzt.jiv.library.config;

import eakerzt.jiv.common.config.ClientConfigs;

public record JivConfigData(
	ModIdFormatConfig modIdFormatConfig,
	ColorNameConfig colorNameConfig,
	ClientConfigs clientConfigs
) {
}
