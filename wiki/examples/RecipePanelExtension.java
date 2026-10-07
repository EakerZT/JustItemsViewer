package examplemod.jiv;

import eakerzt.jiv.api.constants.VanillaTypes;
import eakerzt.jiv.api.gui.handlers.IGuiProperties;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotDrawable;
import eakerzt.jiv.api.gui.inputs.RecipeSlotUnderMouse;
import eakerzt.jiv.api.gui.widgets.IRecipeScreenExtension;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Source-build example: native frame, counted slots, column overflow, and ingredient lookup. */
public final class RecipePanelExtension implements IRecipeScreenExtension {
    private final IGuiHelper gui;
    private final List<IRecipeSlotDrawable> slots;
    private final boolean right;
    private final Component title;
    private Rect2i bounds = new Rect2i(0, 0, 0, 0);

    public RecipePanelExtension(IGuiHelper gui, List<ItemStack> contents, boolean right, Component title) {
        this.gui = gui;
        this.right = right;
        this.title = title;
        this.slots = contents.stream().map(stack -> {
            ItemStack copy = stack.copy();
            return gui.createRecipeSlotDrawable(RecipeIngredientRole.INPUT,
                acceptor -> acceptor.add(VanillaTypes.ITEM_STACK, copy), Set.of(), 0);
        }).toList();
    }

    @Override
    public Component getScreenTitle() { return title; }

    @Override
    public void updateScreenLayout(IGuiProperties area, int availableSideHeight) {
        if (slots.isEmpty()) {
            bounds = new Rect2i(0, 0, 0, 0);
            return;
        }
        int maxRows = Math.max(1, (availableSideHeight - 12) / 16);
        int columns = Math.ceilDiv(slots.size(), maxRows);
        int rows = Math.ceilDiv(slots.size(), columns);
        int width = 12 + columns * 16;
        int x = right ? area.guiRight() - 6 : area.guiLeft() - width + 6;
        bounds = new Rect2i(x, area.guiTop(), width, 12 + rows * 16);
        for (int i = 0; i < slots.size(); i++) {
            int column = right ? i / rows : columns - 1 - i / rows;
            slots.get(i).setPosition(x + 6 + column * 16, area.guiTop() + 6 + i % rows * 16);
        }
    }

    @Override
    public List<ScreenRectangle> getExtraGuiAreas() {
        if (slots.isEmpty()) return List.of();
        return List.of(new ScreenRectangle(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight()));
    }

    @Override
    public void drawScreen(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (slots.isEmpty()) return;
        gui.drawRecipeSidePanel(graphics, bounds, right);
        slots.forEach(slot -> slot.draw(graphics, slot.isMouseOver(mouseX, mouseY)));
    }

    @Override
    public Optional<RecipeSlotUnderMouse> getScreenSlotUnderMouse(double mouseX, double mouseY) {
        return slots.stream().filter(slot -> slot.isMouseOver(mouseX, mouseY))
            .findFirst().map(slot -> new RecipeSlotUnderMouse(slot, 0, 0));
    }

    @Override
    public void drawScreenTooltips(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        getScreenSlotUnderMouse(mouseX, mouseY)
            .ifPresent(hovered -> hovered.slot().drawTooltip(graphics, mouseX, mouseY));
    }
}
