[English](Examples-en.md) | [简体中文](Examples.md)

# 完整示例

以下文件可复制到自己模组的 `src/main/java/example/jiv/` 中。使用方法见[快速开始](Getting-Started.md)，配置初始化见[配置 API](Configuration.md)。前三个文件组成 JIV 配方显示插件；`DemoConfig` 独立于插件，在模组初始化时创建一次。

## DemoRecipe.java

```java
package example.jiv;

import eakerzt.jiv.api.recipe.types.IRecipeType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/** Display data only; this does not register a Minecraft crafting recipe. */
public record DemoRecipe(Identifier id, ItemStack input, ItemStack output) {
    public static final IRecipeType<DemoRecipe> TYPE =
        IRecipeType.create("examplemod", "demo_processing", DemoRecipe.class);
}
```

## DemoCategory.java

```java
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
```

## DemoJivPlugin.java

```java
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
```

## DemoConfig.java

```java
package example.jiv;

import eakerzt.jiv.config.api.Configs;
import eakerzt.jiv.config.api.schema.IConfigSchema;
import eakerzt.jiv.config.api.value.IConfigValue;

/** Construct once during mod initialization, while the JIV runtime is installed. */
public final class DemoConfig {
    public final IConfigValue<Boolean> enabled;
    public final IConfigValue<Integer> rows;
    public final IConfigSchema schema;

    public DemoConfig() {
        var builder = Configs.forMod("examplemod")
            .createClientSchemaBuilder("client.ini", "examplemod.config.client");
        var category = builder.addCategory("display");
        enabled = category.addBoolean("enabled", true).build();
        rows = category.addInteger("rows", 8, 1, 16).build();
        schema = builder.build();
    }
}
```
