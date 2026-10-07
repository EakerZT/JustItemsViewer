package example.jiv;

import eakerzt.jiv.api.gui.builder.IRecipeLayoutBuilder;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.api.recipe.IFocusGroup;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

public final class DemoCategory extends AbstractRecipeCategory<DemoRecipe> {
    public DemoCategory(IGuiHelper guiHelper) {
        super(DemoRecipe.TYPE,
            Component.translatable("examplemod.jiv.category.demo"),
            guiHelper.createDrawableItemLike(Items.FURNACE), 100, 36);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DemoRecipe recipe,
                          IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 8, 10)
            .setStandardSlotBackground()
            .add(recipe.input());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 76, 10)
            .setStandardSlotBackground()
            .add(recipe.output());
    }

    @Override
    public Identifier getIdentifier(DemoRecipe recipe) {
        return recipe.id();
    }
}
