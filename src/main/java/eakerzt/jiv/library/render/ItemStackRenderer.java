package eakerzt.jiv.library.render;

import eakerzt.jiv.api.ingredients.IIngredientRenderer;
import eakerzt.jiv.api.ingredients.rendering.BatchRenderElement;
import eakerzt.jiv.common.platform.IPlatformRenderHelper;
import eakerzt.jiv.common.platform.Services;
import eakerzt.jiv.library.render.batch.IItemStackBatchRenderer;
import eakerzt.jiv.library.render.batch.SimpleItemStackBatchRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class ItemStackRenderer implements IIngredientRenderer<ItemStack> {
	private final IItemStackBatchRenderer batchRenderer = new SimpleItemStackBatchRenderer();

	@Override
	public void render(GuiGraphicsExtractor guiGraphics, @Nullable ItemStack ingredient) {
		render(guiGraphics, ingredient, 0, 0);
	}

	@Override
	public void render(GuiGraphicsExtractor guiGraphics, @Nullable ItemStack ingredient, int posX, int posY) {
		if (ingredient != null) {
			Minecraft minecraft = Minecraft.getInstance();
			Font font = getFontRenderer(minecraft, ingredient);
			guiGraphics.fakeItem(ingredient, posX, posY);
			guiGraphics.itemDecorations(font, ingredient, posX, posY);
		}
	}

	@Override
	public void renderBatch(GuiGraphicsExtractor guiGraphics, List<BatchRenderElement<ItemStack>> batchRenderElements) {
		batchRenderer.renderBatch(guiGraphics, this, batchRenderElements);
	}

	@Override
	public List<Component> getTooltip(ItemStack ingredient, Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag) {
		return ingredient.getTooltipLines(tooltipContext, player, tooltipFlag);
	}

	@Override
	public Font getFontRenderer(Minecraft minecraft, ItemStack ingredient) {
		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		return renderHelper.getFontRenderer(minecraft, ingredient);
	}

	@Override
	public int getWidth() {
		return 16;
	}

	@Override
	public int getHeight() {
		return 16;
	}
}
