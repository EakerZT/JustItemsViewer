package example.jiv;

import eakerzt.jiv.api.IModPlugin;
import eakerzt.jiv.api.JivPlugin;
import eakerzt.jiv.api.constants.VanillaTypes;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.registration.IIngredientAliasRegistration;
import eakerzt.jiv.api.registration.IRecipeCatalystRegistration;
import eakerzt.jiv.api.registration.IRecipeCategoryRegistration;
import eakerzt.jiv.api.registration.IRecipeRegistration;
import eakerzt.jiv.api.runtime.IJivRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/** Keep references to this class within optional client integration code. */
@JivPlugin
public final class DemoJivPlugin implements IModPlugin {
    private static IJivRuntime runtime;

    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath("examplemod", "jiv_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
            new DemoCategory(registration.getJivHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(DemoRecipe.TYPE, List.of(
            new DemoRecipe(
                Identifier.fromNamespaceAndPath("examplemod", "demo_stone"),
                new ItemStack(Items.COBBLESTONE), new ItemStack(Items.STONE))));
        registration.addIngredientInfo(Items.STONE,
            Component.translatable("examplemod.jiv.info.stone"));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(DemoRecipe.TYPE, Items.FURNACE);
    }

    @Override
    public void registerIngredientAliases(IIngredientAliasRegistration registration) {
        registration.addAlias(Items.STONE, "examplemod.jiv.alias.stone");
    }

    @Override
    public void onRuntimeAvailable(IJivRuntime jivRuntime) {
        runtime = jivRuntime;
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }

    /** Invoke from a client UI action on the Minecraft client thread. */
    public static void showRecipes(ItemStack stack) {
        show(stack, RecipeIngredientRole.OUTPUT);
    }

    public static void showUses(ItemStack stack) {
        show(stack, RecipeIngredientRole.INPUT);
    }

    private static void show(ItemStack stack, RecipeIngredientRole role) {
        IJivRuntime current = runtime;
        if (current == null || stack.isEmpty()) {
            return;
        }
        var focus = current.getJivHelpers().getFocusFactory()
            .createFocus(role, VanillaTypes.ITEM_STACK, stack.copy());
        current.getRecipesGui().show(focus);
    }

    public static void setSearch(String text) {
        IJivRuntime current = runtime;
        if (current != null) {
            current.getIngredientFilter().setFilterText(text);
        }
    }

    public static void bookmark(ItemStack stack) {
        IJivRuntime current = runtime;
        if (current != null) {
            current.getIngredientManager()
                .createTypedIngredient(VanillaTypes.ITEM_STACK, stack.copy(), true)
                .ifPresent(ingredient -> current.getBookmarkManager().add(ingredient));
        }
    }
}
