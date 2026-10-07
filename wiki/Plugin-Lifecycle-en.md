[English](Plugin-Lifecycle-en.md) | [简体中文](Plugin-Lifecycle.md)

# Plugin lifecycle

## Entry point and discovery

```java
package example.jiv;

import eakerzt.jiv.api.IModPlugin;
import eakerzt.jiv.api.JivPlugin;
import net.minecraft.resources.Identifier;

@JivPlugin
public final class ExamplePlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath("examplemod", "main");
    }
}
```

NeoForge scans mod files for `@JivPlugin`, then instantiates the class through its no-argument constructor. Use a public concrete top-level class or public static nested class implementing `IModPlugin`, with a public no-argument constructor.

The plugin UID must be unique. Use your mod ID as its namespace; multiple plugins in one mod should have different paths. Keep construction lightweight and do not access the player, world, or JIV runtime there.

## Registration callbacks

Only `getPluginUid()` is mandatory. Other methods have default implementations; override those needed by your integration. This table is an index of responsibilities, not a strict ordering of every callback.

| Callback | Purpose |
| --- | --- |
| `configureJiv` | Configure features before content collection, such as disabling the default GUI |
| `registerItemSubtypes` / `registerFluidSubtypes` | Define ingredient variant identity |
| `registerIngredients` | Register custom types, helpers, renderers, and codecs |
| `registerExtraIngredients` | Add values to already registered ingredient types |
| `registerIngredientAliases` / `registerModInfo` | Add ingredient and mod search aliases |
| `registerSlotDisplayInterpreters` | Preserve tags and subtype matching semantics |
| `registerCategories` | Register recipe categories |
| `registerVanillaCategoryExtensions` | Extend crafting, smithing, and brewing categories |
| `registerRecipes` | Add recipes and ingredient information |
| `registerRecipeCatalysts` | Register crafting-station entries |
| `registerRecipeTransferHandlers` | Register transfer handlers and listeners |
| `registerGuiHandlers` | Register click areas, exclusion areas, and dragging |
| `registerAdvancedSearch` | Replace search storage or its builder |
| `registerAdvanced` | Extend queries, decorators, and recipe buttons |
| `registerRuntime` | Replace runtime components for advanced integrations |
| `onRuntimeAvailable` / `onRuntimeUnavailable` | Obtain and release the current runtime |

Subtypes and ingredients are registered before the ingredient manager is used. Categories are registered before recipes. The runtime is provided after all mods have registered. Obtain helpers from the current registration's `getJivHelpers()` instead of accessing internal global state.

## Runtime references and threading

Save the instance passed to `onRuntimeAvailable(IJivRuntime runtime)` and clear it in `onRuntimeUnavailable()`. A runtime belongs to its current world or connection; use the newly supplied instance after a context change.

Buttons and key bindings must check runtime availability before calling it. Perform GUI operations on the Minecraft client thread. Dispatch network or background callbacks to that thread before interacting with the GUI. See the [complete plugin](Examples-en.md#demojivpluginjava).

## Optional dependencies and dedicated servers

Keep JIV integration in an isolated client compatibility package. Avoid referencing plugin classes from the common mod entry point, common static initialization, server menus, or network data classes.

If JIV is optional, verify both the client environment and `ModList.get().isLoaded("jiv")` before actively loading integration code. A check inside the plugin cannot prevent a caller from loading a class whose API dependency is absent. JIV discovers the plugin itself; do not construct it manually from your mod entry point.

Test clients with JIV, clients without JIV, and dedicated server startup. If your mod requires JIV to function, declare a required dependency instead.

Source: [IModPlugin](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/IModPlugin.java), [ForgePluginFinder](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/neoforge/startup/ForgePluginFinder.java).
