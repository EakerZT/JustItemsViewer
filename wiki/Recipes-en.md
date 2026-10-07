[English](Recipes-en.md) | [简体中文](Recipes.md)

# Recipes

## Choose an integration approach

Use Minecraft's recipe system and client display data for standard crafting, smelting, smoking, stonecutting, and similar recipes. JIV already provides those categories; do not duplicate their registration.

For custom machines or independent display data, define an `IRecipeType<T>` and `IRecipeCategory<T>`. For unusual recipes that still belong to crafting, smithing, or brewing, consider a [vanilla category extension](Advanced-en.md).

## Recipe type and data

```java
public record DemoRecipe(Identifier id, ItemStack input, ItemStack output) {
    public static final IRecipeType<DemoRecipe> TYPE =
        IRecipeType.create("examplemod", "demo_processing", DemoRecipe.class);
}
```

`IRecipeType<T>` identifies a JIV category; it differs from Minecraft's `RecipeType<T>`. Give it a unique UID and keep `T` consistent across the category and registered data. Share one constant across recipe, category, station, and transfer registration.

`IRecipeType.create(vanillaRecipeType)` creates an `IRecipeHolderType<R>` for an existing Minecraft type. Its data is `RecipeHolder<R>`. Creating the type alone does not register a category or collect recipes.

Minecraft's current client displays use `SlotDisplay` and a resolution context. Determine whether your data comes from those displays or your own server synchronization; do not assume every server recipe object is available on the client. Fixed demonstration data only illustrates registration.

## Categories and slots

`AbstractRecipeCategory<T>` sets the type, title, icon, width, and height in its constructor. Implement `setRecipe` to declare the layout:

```java
@Override
public void setRecipe(IRecipeLayoutBuilder builder, DemoRecipe recipe,
                      IFocusGroup focuses) {
    builder.addInputSlot(8, 10).setStandardSlotBackground().add(recipe.input());
    builder.addOutputSlot(76, 10).setStandardSlotBackground().add(recipe.output());
}
```

Coordinates are relative to the recipe layout. Include ingredient backgrounds and extra controls in its dimensions. `setRecipe` also builds lookup data: drawing an item only in `draw` does not make it searchable.

| `RecipeIngredientRole` | Meaning |
| --- | --- |
| `INPUT` | Consumed input; used in usage lookups |
| `OUTPUT` | Result; used in recipe lookups |
| `CRAFTING_STATION` | Required but not consumed |
| `RENDER_ONLY` | Displayed but excluded from recipe lookup indexing |

Slots accept `ItemStack`, `ItemLike`, `Ingredient`, `SlotDisplay`, or a custom `(ingredientType, ingredient)` through `.add(...)`. `.addItemStacks(list)` provides rotating alternatives. Two simultaneously consumed items require two input slots, rather than two alternatives in one slot.

Prefer `.add(display)` for a `SlotDisplay`: JIV resolves it with the slot context and preserves interpreter semantics. For manual resolution, use `builder.getContextMap()`.

## Register categories, recipes, and stations

These calls belong to different callbacks and use different registration types:

```java
// registerCategories
registration.addRecipeCategories(
    new DemoCategory(registration.getJivHelpers().getGuiHelper()));

// registerRecipes; recipes is List<DemoRecipe>
registration.addRecipes(DemoRecipe.TYPE, recipes);

// registerRecipeCatalysts
registration.addCraftingStation(DemoRecipe.TYPE, Items.FURNACE);
```

The crafting-station method takes the **recipe type first**. Replace the furnace with your machine's block item. Station registration provides an entry point; it does not change machine logic or register transfer support.

## IDs, bookmarks, and codecs

Provide a stable ID for custom recipe data:

```java
@Override
public Identifier getIdentifier(DemoRecipe recipe) {
    return recipe.id();
}
```

The ID supports recipe information and bookmark restoration. Do not generate a random ID on each load. `RecipeHolder` uses its own ID by default; other data defaults to `null`. The default codec can find recipes by ID, but large or complex categories can provide a dedicated codec to avoid slow lookup.

## Linked alternatives

For alternatives such as `[oak planks, spruce planks]` producing `[oak stairs, spruce stairs]`, link the slots:

```java
builder.createFocusLink(inputSlot, outputSlot);
```

Linked slots must contain the same number of alternatives in corresponding order. `onDisplayedIngredientsUpdate` can calculate complex display overrides, but those overrides do not become lookup entries. Declare searchable inputs and outputs in `setRecipe`; use `addInvisibleIngredients(role)` for indexed values that should not be drawn.

## Extras and tooltips

`createRecipeExtras` creates per-layout text, scrolling regions, and controls. `draw` handles additional rendering using `GuiGraphicsExtractor`. A category is shared across recipes; keep recipe-specific mutable UI state in per-layout objects.

Use `addRichTooltipCallback` for slot tooltips and category `getTooltip` for other regions. To add a simple description in `registerRecipes`:

```java
registration.addIngredientInfo(Items.STONE,
    Component.translatable("examplemod.jiv.info.stone"));
```

Source: [IRecipeCategory](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/recipe/category/IRecipeCategory.java), [IRecipeLayoutBuilder](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/gui/builder/IRecipeLayoutBuilder.java), [IRecipeRegistration](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/registration/IRecipeRegistration.java).

## Per-page limits and screen extensions (since alpha-2)

Override `getMaxRecipesPerPage()` to cap visible recipes, for example `return 1` for a 3D preview. Values below one become one; the default is unlimited. Register `IRecipeScreenExtension` with `IRecipeExtrasBuilder.addScreenExtension()` inside `createRecipeExtras`. See [Recipe-screen extensions](Recipe-Screen-Extensions-en.md) for title, coordinate, input capture and native panel contracts. Changing category dimensions invalidates cached layouts at the next layout update. These additions are available starting with `0.0.1-alpha-2`, not alpha-1.

## In-slot fluid amount labels (since alpha-2)

`IRecipeSlotBuilder.setShowFluidAmount(boolean)` enables or disables a fluid amount label at the slot's bottom-right. It is off by default, independent of fluid fill settings and tooltip capacity. No custom renderer or amount overlay is required.

```java
builder.addInputSlot(20, 40)
    .add(Fluids.WATER, 10000)
    .setFluidRenderer(16000, true, 16, 32)
    .setShowFluidAmount(true);
```

This displays `10K`, fills the tank at 10,000 / 16,000, and retains amount/capacity tooltips. The option also works with the default 16-by-16 fluid renderer. Pass `false` to disable it again; configuration order does not matter.

- Reads the currently displayed fluid, following candidate cycling and display overrides.
- Uses NeoForge mB without a unit suffix. Values below 10,000 remain exact; larger values use truncated integer `K/M/G/T/P/E` abbreviations. Tooltips retain precise amounts.
- White shadowed text aligns bottom-right and scales down to fit the slot dimensions.
- Empty slots, non-fluids and non-positive amounts draw no label. Item stack counts are unchanged.
- Does not replace a custom overlay. Draw order: ingredient, custom overlay, fluid amount, non-consumed/chance decorations, candidate badge, hover foreground.
- This is a presentation option; ingredient indexing, consumption and server quantities are unchanged.

This method is included in the Maven Central `0.0.1-alpha-2` artifact. Use alpha-2 or newer for both compilation and runtime.

## Non-consumed and chance markers (since alpha-2)

JIV provides native slot decorations for item/fluid inputs and outputs, as well as other ingredient types. Both are off by default and enabled through `IRecipeSlotBuilder`. No mod-specific texture, overlay or tooltip callback is needed.

| Method | Behavior |
| --- | --- |
| `setNonConsumed(true)` | Green infinity marker inside the top-left and a “Not consumed” tooltip; `false` disables it |
| `setChance(0.25)` | Stores a probability in [0,1] and enables its label and tooltip; here, 25% |
| `setShowChance(false)` | Hides the chance label and tooltip, retaining the value; `true` shows it again |

```java
builder.addInputSlot(20, 20).add(new ItemStack(Items.STONE))
    .setStandardSlotBackground().setNonConsumed(true);
builder.addOutputSlot(60, 20).add(new ItemStack(Items.COBBLESTONE))
    .setStandardSlotBackground().setChance(0.25);
builder.addInputSlot(20, 50).add(Fluids.WATER, 1000)
    .setStandardSlotBackground().setNonConsumed(true)
    .setChance(0.5).setShowFluidAmount(true);
builder.addOutputSlot(60, 50).add(Fluids.LAVA, 1000)
    .setStandardSlotBackground().setChance(0.1).setShowFluidAmount(true);
```

- Supply a fraction, not a percentage: `0.25` means 25%. Zero and one are valid; out-of-range values, NaN and infinity throw `IllegalArgumentException`. Convert EndlessTech's basis points by dividing by `10000.0`.
- Reserve six pixels above the slot for the chance label. Text scales to fit and shows at most two decimals. Tiny probabilities show `<0.01%`; probabilities very close to but below one show `>99.99%`. The tooltip retains the full decimal percentage of the supplied value.
- Zero displays `0%`. One omits the compact label but retains a 100% tooltip. Calling `setChance` again always enables display, including after `setShowChance(false)`.
- Both decorations can coexist with each other, item/fluid counts, renderers and custom overlays. Existing overlays and rich tooltip callbacks are retained. Empty slots have no marker or metadata tooltip.
- Metadata belongs to the slot and follows its displayed ingredient, including cycling and overrides. It is not configured separately per candidate.
- These are presentation APIs. They do not implement consumption, production, random rolls, server execution or ingredient indexing. Input slots use the generic “Chance” tooltip; the recipe defines its meaning.

These methods are available from Maven Central starting with `0.0.1-alpha-2`. EndlessTech can still use `-PjivDev` for source debugging and retains its older Mixin compatibility path.
