[English](Advanced-en.md) | [简体中文](Advanced.md)

# Advanced extensions

Most integrations need only category, ingredient, and GUI registration. These hooks support unusual queries, complex display rules, or replacement of default behavior.

## Vanilla category extensions

Obtain the category in `registerVanillaCategoryExtensions`:

| Registration method | Extension interface |
| --- | --- |
| `getCraftingCategory()` | `ICraftingCategoryExtension<T extends CraftingRecipe>` |
| `getSmithingCategory()` | `ISmithingCategoryExtension<T extends SmithingRecipe>` |
| `getBrewingCategory()` | `IBrewingCategoryExtension<T>` |

Register an extension for your recipe class using the corresponding category interface, then implement its layout and behavior. The registration overloads vary by category; consult the [vanilla extension interfaces](https://github.com/EakerZT/JustItemsViewer/tree/main/src/main/java/eakerzt/jiv/api/recipe/category/extensions/vanilla). Standard recipes need no extension unless the default display cannot describe their behavior.

## Replace search storage

`registerAdvancedSearch(IAdvancedSearchRegistration registration)` can replace the index for phonetic, transliterated, or other custom matching.

| Overload | Purpose |
| --- | --- |
| `replaceSearchStorage(ISearchStorageFactory)` | Create live storage directly |
| `replaceSearchStorage(ISearchStorageBuilderFactory)` | Collect initial data, then preprocess or bake an index |

Each factory call must return a new empty storage or builder. Builders are single-use; storage returned by `build()` must still support runtime `put`. Implement the generic factory signature defined in [ISearchStorageFactory](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/search/ISearchStorageFactory.java) or [ISearchStorageBuilderFactory](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/search/ISearchStorageBuilderFactory.java).

`ISearchStorage<T>` provides:

- `put(key, value)` to index searchable text.
- `getSearchResults(token, consumer)` to deliver matches.
- `getAllElements(consumer)` to deliver all stored values.
- `statistics()` to provide logging statistics.

The replacement applies to all indexed search storage, including backing indexes in limited string storage. If multiple plugins replace it, the last replacement wins; consider interoperability with other search plugins.

To extend default matching, wrap `getDefaultSearchStorageBuilderFactory()`. Apply consistent normalization to indexed text and query tokens.

## Recipe manager plugins, decorators, and buttons

`registerAdvanced` registers advanced recipe manager plugins, category decorators, and recipe button factories. Use them for dynamically derived lookups, extra display information, or actions next to recipes.

Use public extension interfaces instead of internal indexes. A decorator adds to an existing layout; it does not replace correct input/output declarations.

Entry point: [IAdvancedRegistration](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/registration/IAdvancedRegistration.java).

## Feature configuration and runtime replacement

`configureJiv(IJivFeatures features)` runs before collecting ingredients, recipes, and GUI registrations. For example:

```java
@Override
public void configureJiv(IJivFeatures features) {
    features.disableJivGui();
}
```

This changes the overall JIV GUI and is intended for integrations providing a replacement interface, rather than normal machine plugins. `registerRuntime` replaces runtime components; ensure replacement implementations satisfy the contracts other plugins access through `IJivRuntime`.

Source: [IAdvancedSearchRegistration](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/registration/IAdvancedSearchRegistration.java), [IJivFeatures](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/runtime/IJivFeatures.java), [IRuntimeRegistration](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/registration/IRuntimeRegistration.java).
