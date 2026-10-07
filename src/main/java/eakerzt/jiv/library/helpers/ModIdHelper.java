package eakerzt.jiv.library.helpers;

import com.google.common.collect.ImmutableSetMultimap;
import eakerzt.jiv.api.constants.VanillaTypes;
import eakerzt.jiv.api.helpers.IModIdHelper;
import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.ingredients.TypedIngredientUtil;
import eakerzt.jiv.common.platform.IPlatformModHelper;
import eakerzt.jiv.common.platform.Services;
import eakerzt.jiv.library.config.IModIdFormatConfig;
import eakerzt.jiv.library.config.ModIdFormatConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public final class ModIdHelper implements IModIdHelper {
	private final IModIdFormatConfig modIdFormattingConfig;
	private final IIngredientManager ingredientManager;
	private final Function<ITypedIngredient<?>, String> getDisplayModId;
	private final ImmutableSetMultimap<String, String> modAliases;

	public ModIdHelper(
		IModIdFormatConfig modIdFormattingConfig,
		IIngredientManager ingredientManager,
		Function<ITypedIngredient<?>, String> getDisplayModId,
		ImmutableSetMultimap<String, String> modAliases
	) {
		this.modIdFormattingConfig = modIdFormattingConfig;
		this.ingredientManager = ingredientManager;
		this.getDisplayModId = getDisplayModId;
		this.modAliases = modAliases;
	}

	@Override
	public boolean isDisplayingModNameEnabled() {
		Component modNameFormat = modIdFormattingConfig.getModNameFormat();
		return !modNameFormat.getString().isEmpty();
	}

	@Override
	public <T> Optional<Component> getModNameForTooltip(ITypedIngredient<T> typedIngredient) {
		ITypedIngredient<T> checkedIngredient = TypedIngredientUtil.checkTypedIngredientFromApi(ingredientManager, typedIngredient);

		if (!isDisplayingModNameEnabled()) {
			return Optional.empty();
		}

		IIngredientType<T> type = checkedIngredient.getType();

		if (modIdFormattingConfig.isModNameFormatOverrideActive() && type == VanillaTypes.ITEM_STACK) {
			// we detected that another mod is adding the mod name already
			return Optional.empty();
		}

		String modId = getDisplayModId.apply(checkedIngredient);
		return Optional.of(getFormattedModNameComponentForModId(modId));
	}

	@Override
	public Component getFormattedModNameComponentForModId(String modId) {
		String modName = getModNameForModId(modId);
		modName = ChatFormatting.stripFormatting(modName); // some crazy mod has formatting in the name
		Component modNameFormat = modIdFormattingConfig.getModNameFormat();
		if (!modNameFormat.getString().isEmpty()) {
			return ModIdFormatConfig.replaceModNameFormatCode(modNameFormat, modName);
		}
		return Component.literal(modName);
	}

	@Override
	public Set<String> getModAliases(String modId) {
		return modAliases.get(modId);
	}

	@Override
	public String getModNameForModId(String modId) {
		IPlatformModHelper modHelper = Services.PLATFORM.getModHelper();
		return modHelper.getModNameForModId(modId);
	}
}
