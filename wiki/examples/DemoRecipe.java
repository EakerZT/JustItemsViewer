package example.jiv;

import eakerzt.jiv.api.recipe.types.IRecipeType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/** Display data only; this does not register a Minecraft crafting recipe. */
public record DemoRecipe(Identifier id, ItemStack input, ItemStack output) {
    public static final IRecipeType<DemoRecipe> TYPE =
        IRecipeType.create("examplemod", "demo_processing", DemoRecipe.class);
}
