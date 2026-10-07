package eakerzt.jiv.common.input.keys;

import com.mojang.blaze3d.platform.InputConstants;
import eakerzt.jiv.api.runtime.IJivKeyMapping;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;

public class JivMultiKeyMapping implements IJivKeyMappingWithExtraModifiers {
	private final List<IJivKeyMapping> mappings;

	public JivMultiKeyMapping(IJivKeyMapping... mappings) {
		this.mappings = Arrays.asList(mappings);
	}

	@Override
	public boolean isActiveAndMatches(InputConstants.Key key) {
		return this.mappings.stream()
			.anyMatch(m -> m.isActiveAndMatches(key));
	}

	@Override
	public boolean isActiveAndMatchesAllowingExtraModifiers(InputConstants.Key key) {
		return this.mappings.stream()
			.anyMatch(mapping -> {
				if (mapping instanceof IJivKeyMappingWithExtraModifiers withExtraModifiers) {
					return withExtraModifiers.isActiveAndMatchesAllowingExtraModifiers(key);
				}
				return mapping.isActiveAndMatches(key);
			});
	}

	@Override
	public boolean isUnbound() {
		return this.mappings.stream()
			.allMatch(IJivKeyMapping::isUnbound);
	}

	@Override
	public Component getTranslatedKeyMessage() {
		return this.mappings.stream()
			.filter(m -> !m.isUnbound())
			.map(IJivKeyMapping::getTranslatedKeyMessage)
			.findFirst()
			.orElseGet(this::getFallbackTranslatedKeyMessage);
	}

	private Component getFallbackTranslatedKeyMessage() {
		return this.mappings.stream()
			.map(IJivKeyMapping::getTranslatedKeyMessage)
			.findFirst()
			.orElseGet(() -> Component.literal("error"));
	}
}
