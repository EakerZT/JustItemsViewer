package eakerzt.jiv.config.gui.neoforge.config;

import eakerzt.jiv.config.gui.ConfigScreenSchema;

import java.util.Collection;
import java.util.List;

final class NeoForgeConfigSchema implements ConfigScreenSchema {
	private final List<NeoForgeConfigCategory> categories;

	public NeoForgeConfigSchema(Collection<NeoForgeConfigCategory> categories) {
		this.categories = List.copyOf(categories);
	}

	@Override
	public List<NeoForgeConfigCategory> getCategories() {
		return categories;
	}
}
