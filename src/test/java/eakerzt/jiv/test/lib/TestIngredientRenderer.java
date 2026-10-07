package eakerzt.jiv.test.lib;

import eakerzt.jiv.api.ingredients.IIngredientRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import org.jspecify.annotations.Nullable;

import java.util.List;

class TestIngredientRenderer implements IIngredientRenderer<TestIngredient> {
	@Override
	public void render(GuiGraphicsExtractor guiGraphics, TestIngredient ingredient) {

	}

	@Override
	public List<Component> getTooltip(TestIngredient ingredient, Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag) {
		return List.of(
			Component.literal("Test Ingredient Tooltip " + ingredient),
			Component.literal("Test ingredient tooltip " + ingredient + " line 2")
		);
	}
}
