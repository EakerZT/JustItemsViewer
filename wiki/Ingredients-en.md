[English](Ingredients-en.md) | [简体中文](Ingredients.md)

# Ingredients

An ingredient is any object used in the list, bookmarks, search, or recipe slots, not just a crafting input. Items use `VanillaTypes.ITEM_STACK`; NeoForge fluids use `NeoForgeTypes.FLUID_STACK`.

## Extra item stacks

Use `registerExtraIngredients` for variants of an existing type that are absent from the creative menu:

```java
@Override
public void registerExtraIngredients(IExtraIngredientRegistration registration) {
    ItemStack namedStone = new ItemStack(Items.STONE);
    namedStone.set(DataComponents.CUSTOM_NAME, Component.literal("Demo Stone"));
    registration.addExtraItemStacks(List.of(namedStone));
}
```

Import `net.minecraft.core.component.DataComponents`. Adding a stack does not define a new identity; subtype rules determine whether the variant differs from the default item.

## Component-based subtypes

```java
@Override
public void registerItemSubtypes(ISubtypeRegistration registration) {
    registration.registerFromDataComponentTypes(
        Items.STONE, DataComponents.CUSTOM_NAME);
}
```

Replace this demonstration with your own item and components that determine recipe identity, such as material or potion contents. Avoid indexing constantly changing energy levels or timers as separate variants.

For complex rules, use `registerSubtypeInterpreter(item, interpreter)`. `getSubtypeData(ingredient, context)` must return a value with stable `equals` and `hashCode`, or `null` when no subtype data exists. Avoid mutable keys or default object identity.

## Search aliases

```java
@Override
public void registerIngredientAliases(IIngredientAliasRegistration registration) {
    registration.addAlias(Items.STONE, "examplemod.jiv.alias.stone");
    registration.addAlias(new ItemStack(Items.COBBLESTONE), "rough stone");
}
```

Aliases can be plain strings or translation keys. The `Item` overload applies to all subtypes; the `ItemStack` overload applies to that ingredient identity. Player search settings determine whether aliases participate in search.

## Fluids

```java
builder.addInputSlot(8, 10).add(Fluids.WATER, 1000L);
```

`Fluids` is in `net.minecraft.world.level.material`. On NeoForge, one bucket is 1000 mB; `.add(fluid)` defaults to a bucket. For an existing `FluidStack`, use `.add(NeoForgeTypes.FLUID_STACK, fluidStack)`. Add extra variants with `addExtraIngredients` for that type.

Register fluid subtypes in `registerFluidSubtypes`, using the supplied `IPlatformFluidHelper<T>` for platform information. Do not cast generic `T` to one loader's fluid class when implementing a platform abstraction.

## Custom types

Gas, energy, or other objects need these components:

| Component | Responsibility |
| --- | --- |
| `IIngredientType<T>` | Java class and stable type UID |
| `IIngredientHelper<T>` | Names, identity, comparison, copying, and validity |
| `IIngredientRenderer<T>` | Rendering in a 16×16 space and tooltips |
| `Codec<T>` | Saving and restoring ingredients, including bookmarks |
| `Collection<T>` | Initially listed values |

Call this in `registerIngredients` after implementing the placeholder objects:

```java
registration.register(MY_TYPE, allIngredients, myHelper, myRenderer, myCodec);
```

A simple `IIngredientType<T>` can return `MyIngredient.class`. Its default UID is the class name; override `getUid()` with a stable value such as `examplemod:gas` when persistence should survive class renaming. Reuse the same type instance throughout your integration.

Required helper methods include `getIngredientType`, `getDisplayName`, `getUid`, `getIdentifier`, `copyIngredient`, and `getErrorInfo`. The UID is a comparison/index key; the identifier is the resource identity. The renderer uses `GuiGraphicsExtractor`. The codec must support normalized ingredients.

Register a custom type before using it in slots. Provide its initial values to `register`; use `registerExtraIngredients` for an existing type and [runtime addition](Runtime-en.md) for values created after startup.

## SlotDisplay interpreters

Resolving a display to item stacks can lose tag membership or all-subtype matching semantics. In `registerSlotDisplayInterpreters`, register an `ISlotDisplayInterpreter<D, T>` for your `SlotDisplay.Type<D>`. Use `registerUniversal` for composite displays whose meaning is shared by all ingredient types.

Interpreters supplement matching and tooltip meaning; they do not register an ingredient type. Ordinary displays that already describe their ingredients accurately need no interpreter.

Source: [IModIngredientRegistration](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/registration/IModIngredientRegistration.java), [IIngredientHelper](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/ingredients/IIngredientHelper.java), [ISubtypeRegistration](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/registration/ISubtypeRegistration.java), [ISlotDisplayInterpreterRegistration](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/registration/ISlotDisplayInterpreterRegistration.java).
