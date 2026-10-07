[English](GUI-Integration-en.md) | [简体中文](GUI-Integration.md)

# GUI integration

Register GUI integration in `registerGuiHandlers(IGuiHandlerRegistration registration)`. `MachineScreen` below is a placeholder for your own screen class; adapt its fields and coordinates to your implementation.

## Recipe click areas

```java
registration.addRecipeClickArea(
    MachineScreen.class, 78, 32, 28, 18, DemoRecipe.TYPE);
```

The position is relative to the container GUI's top-left corner; width and height define the hit area. Supply multiple recipe types if needed. This method applies to `AbstractContainerScreen` and registers the area through a container GUI handler.

The area opens recipe categories; it does not craft or transfer items.

## Exclude extra panels from the overlay

Report tabs, tank panels, and upgrade areas outside the normal container rectangle through `IGuiContainerHandler<T>.getGuiExtraAreas`:

```java
registration.addGuiContainerHandler(MachineScreen.class,
    new IGuiContainerHandler<MachineScreen>() {
        @Override
        public List<Rect2i> getGuiExtraAreas(MachineScreen screen) {
            return List.of(new Rect2i(
                screen.getGuiLeft() + screen.getXSize(),
                screen.getGuiTop(), 24, screen.getYSize()));
        }
    });
```

Adapt the boundary getter names to your screen. `Rect2i` is in `net.minecraft.client.renderer`. Exclusion rectangles use **absolute screen coordinates**, unlike GUI-relative click areas. Return the actual occupied bounds for expandable panels.

Use `addGlobalGuiHandler` for shared extra areas. For a regular `Screen`, register an `IScreenHandler` with `addGuiScreenHandler` to provide GUI bounds and allow overlay layout.

## Tanks and custom ingredient displays

Normal item slots usually already support `R` / `U` lookups. For a tank or custom icon, override `getClickableIngredientUnderMouse` and use the supplied `IClickableIngredientFactory` to return its clickable ingredient on a hit.

Mouse coordinates are absolute screen coordinates. The ingredient type must be registered, such as `NeoForgeTypes.FLUID_STACK` for fluids. Return `Optional.empty()` outside the target.

## Ghost ingredient dragging

Filters and recipe templates can register `IGhostIngredientHandler<T>` using `addGhostIngredientHandler(screenClass, handler)`.

1. `getTargetsTyped(gui, typedIngredient, doStart)` supplies target rectangles.
2. A successful drop calls `Target<I>.accept(I ingredient)`.
3. `onComplete()` runs when dragging ends, whether or not the drop succeeded.

Targets use absolute screen coordinates. `doStart=false` is a preview and must not change the filter. Save the setting only when an ingredient is accepted. Ghost ingredients are template data; no real item is removed from the inventory. Send and validate your own setting request when the filter belongs to the server.

Source: [IGuiHandlerRegistration](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/registration/IGuiHandlerRegistration.java), [IGuiContainerHandler](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/gui/handlers/IGuiContainerHandler.java), [IGhostIngredientHandler](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/gui/handlers/IGhostIngredientHandler.java).

## Recipe-screen extensions (since alpha-2)

Recipe-specific titles, native side panels, ingredient queries, pagination limits and direct mouse capture are documented in [Recipe-screen extension API](Recipe-Screen-Extensions-en.md), with a complete Java example. These APIs are included in `0.0.1-alpha-2` and later; they are absent from `0.0.1-alpha-1`.
