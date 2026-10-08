[English](Home-en.md) | [简体中文](Home-zh-CN.md)

# Just Items Viewer Developer Wiki

This Wiki is for mod developers integrating **Just Items Viewer (JIV)**: display custom recipes, extend ingredient search, integrate container screens, and control JIV at runtime.

The documentation targets **JIV 0.0.1-alpha-3 / Minecraft 26.1.2 / NeoForge 26.1.2.99 / Java 25**. The public API package is `eakerzt.jiv.api`; the bundled configuration API is `eakerzt.jiv.config.api`. Some source `@since` tags come from upstream and are not JIV release numbers.

## Start here

| Goal | Page |
| --- | --- |
| Add the Maven Central dependency and display your first recipe | [Getting started](Getting-Started-en.md) |
| Understand discovery, registration, and runtime availability | [Plugin lifecycle](Plugin-Lifecycle-en.md) |
| Define recipe types, categories, slots, and crafting stations | [Recipes](Recipes-en.md) |
| Register variants, fluids, custom ingredients, and aliases | [Ingredients](Ingredients-en.md) |
| Add recipe click areas, exclusion areas, and ghost dragging | [GUI integration](GUI-Integration-en.md) |
| Fill container input slots and observe transfer results | [Recipe transfer](Recipe-Transfer-en.md) |
| Open recipes, set search text, manage bookmarks, and add dynamic content | [Runtime API](Runtime-en.md) |
| Extend vanilla categories and replace search storage | [Advanced extensions](Advanced-en.md) |
| Use the bundled configuration system | [Configuration](Configuration-en.md) |
| Troubleshoot integration and migrate older examples | [FAQ](FAQ-en.md) |
| Copy the complete Java source files | [Complete examples](Examples-en.md) |

## API model

An integration implements `IModPlugin` and carries `@JivPlugin`. JIV discovers it and passes registration objects into its callbacks. After registration, `onRuntimeAvailable` provides `IJivRuntime`; `onRuntimeUnavailable` tells the plugin to release connection-specific references.

A **category** defines the recipe layout. **Recipe data** describes its inputs and outputs. An **ingredient type** identifies, searches, and renders objects such as item stacks. Recipe transfer is a separate container integration; displaying a recipe does not automatically add a working fill button.

Use the public API. Implementation classes in `common`, `library`, and `gui`, and types marked `@ApiStatus.Internal`, are not integration entry points. JIV supplies interfaces marked `@ApiStatus.NonExtendable`; call them instead of implementing them.

## Dependency and source

The release is available from [Maven Central](https://central.sonatype.com/artifact/io.github.eakerzt/jiv-26.1.2-neoforge/0.0.1-alpha-3):

```text
io.github.eakerzt:jiv-26.1.2-neoforge:0.0.1-alpha-3
```

- [Public API source](https://github.com/EakerZT/JustItemsViewer/tree/main/src/main/java/eakerzt/jiv/api)
- [Built-in plugin examples](https://github.com/EakerZT/JustItemsViewer/tree/main/src/main/java/eakerzt/jiv/library/plugins)
- [Configuration API source](https://github.com/EakerZT/JustItemsViewer/tree/main/src/main/java/eakerzt/jiv/config/api)

The Markdown sources can also be maintained in the project's `wiki/` directory. When copying them into the separate GitHub Wiki repository, remove `.md` from internal page links. `_Sidebar.md` defines the navigation; complete Java examples are included as Wiki pages.
