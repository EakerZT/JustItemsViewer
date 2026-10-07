package eakerzt.jiv.gui.overlay.ingredients;

import eakerzt.jiv.api.helpers.IColorHelper;
import eakerzt.jiv.api.ingredients.IIngredientHelper;
import eakerzt.jiv.api.ingredients.IIngredientRenderer;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.config.IClientToggleState;
import eakerzt.jiv.common.config.IIngredientFilterConfig;
import eakerzt.jiv.common.gui.JivTooltip;
import eakerzt.jiv.common.input.IInternalKeyMappings;
import eakerzt.jiv.common.util.SafeIngredientUtil;
import eakerzt.jiv.common.config.SearchMode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class IngredientGridTooltipHelper {
	private final IIngredientManager ingredientManager;
	private final IIngredientFilterConfig ingredientFilterConfig;
	private final IClientToggleState toggleState;
	private final IInternalKeyMappings keyBindings;
	private final IColorHelper colorHelper;

	public IngredientGridTooltipHelper(
		IIngredientManager ingredientManager,
		IIngredientFilterConfig ingredientFilterConfig,
		IClientToggleState toggleState,
		IInternalKeyMappings keyBindings,
		IColorHelper colorHelper
	) {
		this.ingredientManager = ingredientManager;
		this.ingredientFilterConfig = ingredientFilterConfig;
		this.toggleState = toggleState;
		this.keyBindings = keyBindings;
		this.colorHelper = colorHelper;
	}

	public <T> void getIngredientTooltip(
		JivTooltip tooltip,
		ITypedIngredient<T> typedIngredient,
		IIngredientRenderer<T> ingredientRenderer,
		IIngredientHelper<T> ingredientHelper
	) {
		SafeIngredientUtil.getRichTooltip(tooltip, ingredientManager, ingredientRenderer, typedIngredient);

		if (ingredientFilterConfig.colorSearchMode().get() != SearchMode.DISABLED) {
			addColorSearchInfoToTooltip(tooltip, typedIngredient, ingredientHelper);
		}

		if (ingredientFilterConfig.searchIngredientAliases().get()) {
			addIngredientAliasesToTooltip(tooltip, typedIngredient, ingredientManager);
		}

		if (toggleState.isEditModeEnabled()) {
			addEditModeInfoToTooltip(tooltip, keyBindings);
		}
	}

	private <T> void addIngredientAliasesToTooltip(JivTooltip tooltip, ITypedIngredient<T> typedIngredient, IIngredientManager ingredientManager) {
		Collection<String> aliases = ingredientManager.getIngredientAliases(typedIngredient);
		if (aliases.isEmpty()) {
			return;
		}
		tooltip.add(Component.empty());
		tooltip.add(
			Component.translatable("jiv.tooltip.item.search.aliases")
				.withStyle(ChatFormatting.GRAY)
		);
		for (String alias : aliases) {
			tooltip.add(
				Component.literal("• " + alias)
					.withStyle(ChatFormatting.GRAY)
			);
		}
	}

	private <T> void addColorSearchInfoToTooltip(JivTooltip tooltip, ITypedIngredient<T> typedIngredient, IIngredientHelper<T> ingredientHelper) {
		Iterable<Integer> colors = ingredientHelper.getColors(typedIngredient.getIngredient());
		String colorNamesString = StreamSupport.stream(colors.spliterator(), false)
			.map(colorHelper::getClosestColorName)
			.collect(Collectors.joining(", "));
		if (!colorNamesString.isEmpty()) {
			Component colorTranslation = Component.translatable("jiv.tooltip.item.colors", colorNamesString)
				.withStyle(ChatFormatting.GRAY);
			tooltip.add(colorTranslation);
		}
	}

	private static void addEditModeInfoToTooltip(JivTooltip tooltip, IInternalKeyMappings keyBindings) {
		tooltip.add(CommonComponents.EMPTY);
		tooltip.add(
			Component.translatable("gui.jiv.editMode.description")
				.withStyle(ChatFormatting.DARK_GREEN)
		);
		tooltip.addKeyUsageComponent(
			"gui.jiv.editMode.description.hide",
			keyBindings.getToggleHideIngredient()
		);
		tooltip.addKeyUsageComponent(
			"gui.jiv.editMode.description.hide.wild",
			keyBindings.getToggleWildcardHideIngredient()
		);
	}
}
