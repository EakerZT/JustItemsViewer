[English](FAQ-en.md) | [简体中文](FAQ.md)

# FAQ

## The plugin is not loaded

Check both `@JivPlugin` and `IModPlugin`, a public concrete class, an accessible no-argument constructor, and inclusion of the class in the mod JAR. The `Failed to load` log identifies the class and underlying exception.

For missing classes, check JIV and Minecraft versions, development runtime dependencies, and references to absent optional mods.

## The category exists but recipes are missing

Check `addRecipes` in `registerRecipes`, the shared recipe type constant, generic data types, category `isHandled` filtering, and the inputs/outputs declared through `setRecipe`.

Drawing ingredients only in `draw` does not index them. `RENDER_ONLY` ingredients do not contribute to that recipe's input/output lookup.

## Recipe and usage lookups are reversed

`OUTPUT` asks how to obtain an ingredient. `INPUT` asks what it is used for. Check both slot roles and runtime focus roles.

## Variants merge, duplicate, or match incorrectly

Register components or a subtype interpreter that actually identify your item variants. Extra stacks, subtype identity, and search aliases serve different purposes.

Preserve original `SlotDisplay` data when possible. Resolving it to ordinary stacks first can lose tag identity and all-subtype matching semantics.

## Search aliases do not work

Check translation keys, the current language, player alias-search settings, and the identity of the ingredient used for registration. An `Item` alias covers all of its subtypes.

## A crafting station appears but transfer is unavailable

Station registration only creates an entry point. Register a transfer handler matching the current menu, recipe type, and menu type; verify menu slot indices. Standard transfer also requires JIV on the server.

## Transfer listeners do not receive completion

Custom handlers must call `context.completeRecipeTransfer(...)` after actual completion. `doTransfer=false` is a check and does not run an actual transfer lifecycle. Asynchronous handlers must wait for confirmation before reporting success or rejection.

## API calls fail after leaving a world

Clear the old `IJivRuntime` reference and use the next instance provided by the lifecycle callback. Buttons, asynchronous callbacks, and caches must not keep using a previous world's runtime.

## Can existing JEI plugins be loaded directly?

No. JIV has its own annotation, packages, and current interfaces. Recheck signatures, generics, and platform versions when porting; a package-name replacement alone is insufficient.

| Older examples may use | Current JIV |
| --- | --- |
| `mezz.jei.api.*` / `@JeiPlugin` | `eakerzt.jiv.api.*` / `@JivPlugin` |
| `IJeiRuntime` / `getJeiHelpers()` | `IJivRuntime` / `getJivHelpers()` |
| `ResourceLocation` | Current Minecraft uses `Identifier` |
| Older concrete recipe-type API | `IRecipeType<T>`, distinct from Minecraft's type |
| Category `getBackground()` | `getWidth()` / `getHeight()` and optional background drawing |
| `RecipeIngredientRole.CATALYST` | `CRAFTING_STATION` |
| Older multi-parameter `transferRecipe` | `transferRecipe(context, doTransfer)` |
| Older rendering types | Current signatures use `GuiGraphicsExtractor` |

These are migration hints; the current public interfaces are authoritative.

## Maven dependencies fail to resolve

Version `0.0.1-alpha-3` is published to Maven Central. Check group `io.github.eakerzt`, artifact `jiv-26.1.2-neoforge`, version, and `mavenCentral()`. If Gradle cached a failed lookup before publication, retry with `--refresh-dependencies`. Disable offline mode and check network access to Maven Central. See [Getting started](Getting-Started-en.md).

## Does the example implement a complete machine?

It implements JIV display categories, plugin callbacks, and runtime calls. It does not include a mod entry point, machine menu, networking, or server processing logic. `MachineScreen`, `MachineMenu`, and `ModMenus` in other pages are placeholders for your project types.
