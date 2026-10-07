package eakerzt.jiv.library.load.registration;

import com.google.common.collect.ImmutableSetMultimap;
import eakerzt.jiv.api.registration.IModInfoRegistration;
import eakerzt.jiv.common.collect.SetMultiMap;

import java.util.Collection;

public class ModInfoRegistration implements IModInfoRegistration {
	private final SetMultiMap<String, String> modAliases = new SetMultiMap<>();

	@Override
	public void addModAliases(String modId, Collection<String> aliases) {
		modAliases.putAll(modId, aliases);
	}

	public ImmutableSetMultimap<String, String> getModAliases() {
		return modAliases.toImmutable();
	}
}
