[English](Getting-Started-en.md) | [简体中文](Getting-Started.md)

# Getting started

Create a JIV plugin in your NeoForge mod and display a demonstration recipe that turns cobblestone into stone, with a crafting-station entry and ingredient information.

This example adds **display and lookup data only**. It does not register a Minecraft recipe, add a machine, or change crafting rules. Replace the fixed data with your own client-side recipe data when integrating a real mod.

## 1. Add the dependency

JIV **0.0.1-alpha-3** is published to [Maven Central](https://central.sonatype.com/artifact/io.github.eakerzt/jiv-26.1.2-neoforge/0.0.1-alpha-3). In a mod project using NeoForge ModDevGradle, add this to `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    compileOnly("io.github.eakerzt:jiv-26.1.2-neoforge:0.0.1-alpha-3")
    runtimeOnly("io.github.eakerzt:jiv-26.1.2-neoforge:0.0.1-alpha-3")
}
```

The published mod JAR contains both the API and implementation; there is no separate `api` classifier. `compileOnly` makes the API available when compiling your integration. `runtimeOnly` loads JIV in the development runtime. Do not embed or relocate JIV classes into your mod JAR.

Use Java 25, Minecraft 26.1.2, and a matching NeoForge version. Development dependencies do not install JIV for players: declare the appropriate required or optional relationship in your mod metadata. See [Plugin lifecycle](Plugin-Lifecycle-en.md) for optional integration and client isolation.

### Optional: build from local source

For debugging unpublished changes, build the JIV repository:

```powershell
.\gradlew.bat build
```

Copy `build/libs/jiv-26.1.2-neoforge-0.0.1-alpha-3.jar` into your mod project's `libs/` directory and use:

```kotlin
dependencies {
    compileOnly(files("libs/jiv-26.1.2-neoforge-0.0.1-alpha-3.jar"))
    runtimeOnly(files("libs/jiv-26.1.2-neoforge-0.0.1-alpha-3.jar"))
}
```

Use either the published dependency or the local JAR for a development setup.

## 2. Copy the complete example

Copy these files from [Complete examples](Examples-en.md) into `src/main/java/example/jiv/` in your mod:

| File | Purpose |
| --- | --- |
| `DemoRecipe.java` | Display data and the shared JIV recipe type |
| `DemoCategory.java` | Title, icon, dimensions, and input/output slots |
| `DemoJivPlugin.java` | Discovery, registration, and runtime operations |

Replace `examplemod` with your mod ID and adjust the package to your project. The plugin must be a public, instantiable class with an accessible no-argument constructor. The example uses its implicit public no-argument constructor.

The core registration follows this structure:

```java
@JivPlugin
public final class DemoJivPlugin implements IModPlugin {
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
    }
}
```

The complete files include all imports, the category implementation, and the remaining callbacks.

## 3. Add translations

Merge these entries into `assets/examplemod/lang/en_us.json`:

```json
{
  "examplemod.jiv.category.demo": "Demo Processing",
  "examplemod.jiv.info.stone": "This is an example description for the JIV API.",
  "examplemod.jiv.alias.stone": "building stone"
}
```

For Chinese support, add corresponding translations to `zh_cn.json`:

```json
{
  "examplemod.jiv.category.demo": "演示加工",
  "examplemod.jiv.info.stone": "这是一条 JIV API 演示说明。",
  "examplemod.jiv.alias.stone": "建筑石材"
}
```

## 4. Verify the integration

1. Load your mod with JIV, enter a world, and open the inventory.
2. Hover over stone and press `R`: check the Demo Processing category and its cobblestone input and stone output.
3. Hover over cobblestone and press `U`: check that the same recipe appears.
4. Check the furnace crafting-station entry and the stone information page.
5. Enable ingredient alias search, then search for `building stone` in English or `建筑石材` in Chinese.
6. Leave and re-enter the world: check that the plugin receives the new runtime and clears the old reference.

The complete plugin includes `showRecipes`, `showUses`, `setSearch`, and `bookmark` methods for your client buttons or key bindings. They do not automatically open a GUI when a world loads.

Continue with [Recipes](Recipes-en.md) to replace the demonstration with real machine recipes.
