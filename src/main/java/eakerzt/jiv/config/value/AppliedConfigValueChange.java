package eakerzt.jiv.config.value;

import eakerzt.jiv.config.api.value.change.IAppliedConfigValueChange;

public record AppliedConfigValueChange<T>(
	ConfigValue<T> configValue,
	T oldValue,
	T newValue
) implements IAppliedConfigValueChange<T> {}
