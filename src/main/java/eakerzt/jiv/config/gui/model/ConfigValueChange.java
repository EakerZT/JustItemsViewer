package eakerzt.jiv.config.gui.model;

import eakerzt.jiv.config.api.value.editor.ConfigValueRestartRequirement;
import eakerzt.jiv.config.gui.api.IConfigScreenValue;

import java.util.List;

/**
 * A pending GUI edit for one config value.
 */
public record ConfigValueChange<T>(
	IConfigScreenValue<T> configValue,
	T value
) {
	public static ConfigValueRestartRequirement getRestartRequirement(List<ConfigValueChange<?>> changes) {
		ConfigValueRestartRequirement result = ConfigValueRestartRequirement.NONE;
		for (ConfigValueChange<?> change : changes) {
			ConfigValueRestartRequirement restartRequirement = change.configValue().getRestartRequirement();
			if (restartRequirement == ConfigValueRestartRequirement.GAME_RESTART) {
				return restartRequirement;
			}
			if (restartRequirement == ConfigValueRestartRequirement.WORLD_RESTART) {
				result = restartRequirement;
			}
		}
		return result;
	}

	public boolean apply() {
		return configValue.set(value);
	}
}
