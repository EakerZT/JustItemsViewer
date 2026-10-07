package eakerzt.jiv.library.transfer;

import eakerzt.jiv.api.gui.ingredient.IRecipeSlotView;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotsView;
import eakerzt.jiv.common.gui.JivGuiColors;
import eakerzt.jiv.common.gui.JivGuiColors.GuiColor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.Collection;

public class RecipeTransferErrorMissingSlots extends RecipeTransferErrorTooltip {
	private final Collection<IRecipeSlotView> slots;

	public RecipeTransferErrorMissingSlots(Component message, Collection<IRecipeSlotView> slots) {
		super(message);
		this.slots = slots;
	}

	@Override
	public void showError(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, IRecipeSlotsView recipeSlotsView, int recipeX, int recipeY) {
		var poseStack = guiGraphics.pose();
		poseStack.pushMatrix();
		{
			poseStack.translate(recipeX, recipeY);

			for (IRecipeSlotView slot : slots) {
				slot.drawHighlight(guiGraphics, JivGuiColors.getColor(GuiColor.RECIPE_TRANSFER_MISSING_SLOT_HIGHLIGHT));
			}
		}
		poseStack.popMatrix();
	}

	@Override
	public int getMissingCountHint() {
		return this.slots.size();
	}
}
