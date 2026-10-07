[English](Runtime-en.md) | [简体中文](Runtime.md)

# Runtime API

The plugin receives `IJivRuntime` through `onRuntimeAvailable`. Here, `runtime` means the current available instance: check for `null` before calling it and clear it in `onRuntimeUnavailable`. See the [complete plugin](Examples-en.md#demojivpluginjava).

## Open recipes and uses

```java
var focus = runtime.getJivHelpers().getFocusFactory().createFocus(
    RecipeIngredientRole.OUTPUT,
    VanillaTypes.ITEM_STACK,
    new ItemStack(Items.STONE)
);
runtime.getRecipesGui().show(focus);
```

`OUTPUT` finds ways to make an item; `INPUT` finds recipes that use it. Do not create a focus for an empty stack. The recipe GUI opens only if matching recipes are found.

Open a whole category with a nonempty list:

```java
runtime.getRecipesGui().showTypes(List.of(DemoRecipe.TYPE));
```

Use `showRecipes(category, recipes, focuses)` to display specific recipes.

## Search text

```java
String oldText = runtime.getIngredientFilter().getFilterText();
runtime.getIngredientFilter().setFilterText("@minecraft stone");
```

This changes the player's search input, so normally trigger it from an explicit UI action. Background recipe queries do not require changing the filter.

## Query and hide recipes

```java
var recipes = runtime.getRecipeManager()
    .createRecipeLookup(DemoRecipe.TYPE)
    .limitFocus(List.of(focus))
    .get()
    .toList();
```

Lookups exclude hidden recipes by default; `.includeHidden()` includes them. Omit `limitFocus` to query all visible recipes of the type.

```java
runtime.getRecipeManager().hideRecipes(DemoRecipe.TYPE, recipes);
runtime.getRecipeManager().unhideRecipes(DemoRecipe.TYPE, recipes);
```

Hide and restore entire categories with `hideRecipeCategory(type)` and `unhideRecipeCategory(type)`. Hiding affects JIV display; it does not enforce server crafting permissions or progression.

Add dynamic display data with `getRecipeManager().addRecipes(type, recipes)`. The category must already be registered. Avoid duplicate additions when receiving repeated notifications.

## Dynamic ingredients

```java
runtime.getIngredientManager().addIngredientsAtRuntime(
    VanillaTypes.ITEM_STACK, extraStacks);
runtime.getIngredientManager().removeIngredientsAtRuntime(
    VanillaTypes.ITEM_STACK, removedStacks);
```

Supply lists from your client synchronization or other runtime data source. Prefer registration callbacks for static ingredients. `getAllItemStacks()` and `getAllIngredients(type)` return unmodifiable collections.

## Ingredient bookmarks

```java
runtime.getIngredientManager()
    .createTypedIngredient(VanillaTypes.ITEM_STACK, stack.copy(), true)
    .ifPresent(typed -> runtime.getBookmarkManager().add(typed));
```

Invalid or empty ingredients produce `Optional.empty()`. `normalize=true` uses normalized identity. The bookmark manager also supplies `contains(typed)` and `remove(typed)`; the return values of `add` and `remove` indicate whether the collection changed.

`IBookmarkManager` handles ingredient bookmarks. Recipe bookmark persistence relies on category IDs and codecs; this interface is not a recipe bookmark API.

## Other accessors

| Method | Purpose |
| --- | --- |
| `getIngredientListOverlay()` | Ingredient overlay and hovered ingredients |
| `getBookmarkOverlay()` | Bookmark overlay |
| `getScreenHelper()` | Screen information |
| `getKeyMappings()` | Player-configured JIV keys |
| `getRecipeTransferManager()` | Registered transfer handlers |
| `getEditModeConfig()` | Edit-mode ingredient hiding |

Source: [IJivRuntime](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/runtime/IJivRuntime.java), [IRecipeManager](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/recipe/IRecipeManager.java), [IBookmarkManager](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/runtime/IBookmarkManager.java).
