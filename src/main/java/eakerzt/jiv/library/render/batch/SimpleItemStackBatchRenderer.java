package eakerzt.jiv.library.render.batch;

import eakerzt.jiv.api.ingredients.rendering.BatchRenderElement;
import eakerzt.jiv.library.render.ItemStackRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SimpleItemStackBatchRenderer implements IItemStackBatchRenderer {
	@Override
	public void renderBatch(GuiGraphicsExtractor guiGraphics, ItemStackRenderer itemStackRenderer, List<BatchRenderElement<ItemStack>> elements) {
		for (BatchRenderElement<ItemStack> element : elements) {
			guiGraphics.fakeItem(element.ingredient(), element.x(), element.y());
		}
	}
}
