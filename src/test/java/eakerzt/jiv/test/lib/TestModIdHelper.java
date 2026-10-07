package eakerzt.jiv.test.lib;

import eakerzt.jiv.api.helpers.IModIdHelper;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import net.minecraft.network.chat.Component;

import java.util.Optional;
import java.util.Set;

public class TestModIdHelper implements IModIdHelper {
	@Override
	public String getModNameForModId(String modId) {
		return "ModName(" + modId + ")";
	}

	@Override
	public Component getFormattedModNameComponentForModId(String modId) {
		return Component.literal(getModNameForModId(modId));
	}

	@Override
	public <T> Optional<Component> getModNameForTooltip(ITypedIngredient<T> typedIngredient) {
		return Optional.empty();
	}

	@Override
	public boolean isDisplayingModNameEnabled() {
		return false;
	}

	@Override
	public Set<String> getModAliases(String modId) {
		return Set.of();
	}
}
