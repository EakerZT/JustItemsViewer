[English](Configuration-en.md) | [简体中文](Configuration.md)

# Configuration API

JIV bundles a configuration system accessible through `eakerzt.jiv.config.api.Configs`. No separate MezzConfig or MezzConfigGUI installation is needed with this JAR. Some comments retain those upstream component names.

Configuration registration is independent of `IModPlugin`: create the schema during mod initialization rather than on every world entry. If JIV is optional, isolate initialization in a path that runs only after confirming JIV is installed.

## Create a configuration

See [DemoConfig.java](Examples-en.md#democonfigjava). Construct `new DemoConfig()` once during mod initialization and retain it:

```java
var builder = Configs.forMod("examplemod")
    .createClientSchemaBuilder("client.ini", "examplemod.config.client");
var category = builder.addCategory("display");

IConfigValue<Boolean> enabled = category.addBoolean("enabled", true).build();
IConfigValue<Integer> rows = category.addInteger("rows", 8, 1, 16).build();

IConfigSchema schema = builder.build();
```

Obtain registration, create the schema builder, add categories, build each value, then build the schema. A value builder is single-use; a file cannot be registered twice.

## Scope

| Method | Purpose |
| --- | --- |
| `createClientSchemaBuilder` | Client preferences shared across worlds |
| `createClientPerWorldSchemaBuilder` | Client preferences for one world or server |
| `createServerSchemaBuilder` | World-owned settings synchronized to clients |

String filenames are relative to the mod's corresponding config directory. The `Path` overload of `createClientSchemaBuilder` accepts the complete path and does not append a mod ID or filename. Prefer conventional directories unless an explicit location is needed.

Client schemas are inactive and default-backed on dedicated servers. Per-world schemas require an active world or connection. A synchronized server schema may be active on a remote client while `getPath()` is empty because its file belongs to the server. Use `isActive()` to distinguish active context from file ownership.

## Read and update

```java
boolean showPanel = enabled.get();
rows.set(10);
```

`get()` returns the effective value. `set()` validates and saves a value. Invalid values throw `IllegalArgumentException`; updates without an active local backing file, including direct updates to synchronized server values on a remote client, throw `IllegalStateException`.

For related settings, use an atomic batch:

```java
schema.batchUpdate(batch -> {
    batch.set(enabled, true);
    batch.set(rows, 10);
});
```

All values must belong to that schema. Invalid updates or an exception from the callback prevent the entire batch from being applied.

## Effective values and listeners

`setRestartRequirement(...)` on the value builder determines when an edit takes effect. For restart-required changes, `get()` still returns the active value; `getEditorInfo().getPendingValue()` returns the saved value awaiting activation.

`value.addListener(listener)` observes effective changes and returns a removal `Runnable`. `schema.addBatchListener(listener)` observes the complete batch. Listeners execute synchronously. Remove listeners that capture a screen or connection when its lifetime ends, and obey client threading rules when updating GUI state from listeners.

## Editors, migration, and serialization

Building the schema registers it for discovery through `Configs.getSchemas()`. The separate `eakerzt.jiv.config.gui.api` provides configuration GUI plugins and screen factories. Follow its interfaces; `@JivPlugin` is not the configuration GUI annotation.

Use `setLegacySources`, `addLegacyName`, `addLegacyValue`, or migrators when storage changes. Migration imports data when the destination does not yet exist; it is not performed on every read. Custom serialized values must be effectively immutable with stable equality.

Source: [Configs](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/config/api/Configs.java), [IConfigRegistration](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/config/api/IConfigRegistration.java), [IConfigSchema](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/config/api/schema/IConfigSchema.java).
