package eakerzt.jiv.library.gui.recipes;

import eakerzt.jiv.api.gui.IRecipeLayoutDrawable;
import eakerzt.jiv.api.gui.builder.ITooltipBuilder;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotView;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.gui.IngredientsTooltipComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public class IngredientsTooltipCallback implements IRecipeSlotRichTooltipCallback {

	private final Supplier<@Nullable IRecipeLayoutDrawable<?>> recipeLayoutSupplier;

	public IngredientsTooltipCallback(Supplier<@Nullable IRecipeLayoutDrawable<?>> supplier) {
		this.recipeLayoutSupplier = supplier;
	}

	@Override
	public void onRichTooltip(IRecipeSlotView recipeSlotView, ITooltipBuilder tooltip) {
		if (Internal.getClientConfigs().getClientConfig().ingredientsSummaryEnabled().get()) {
			IRecipeLayoutDrawable<?> recipeLayout = recipeLayoutSupplier.get();
			if (recipeLayout != null) {
				tooltip.add(Component.translatable("jiv.tooltip.recipe.tooltips.craft.ingredients").withStyle(ChatFormatting.GRAY));
				tooltip.add(new IngredientsTooltipComponent(recipeLayout));
			}
		}
	}
}
