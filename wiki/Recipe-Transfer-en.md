[English](Recipe-Transfer-en.md) | [简体中文](Recipe-Transfer.md)

# Recipe transfer

Transfer puts ingredients the player already owns into container input slots. Register the container, recipe type, and slot rules together. Categories, recipes, and crafting stations alone do not enable transfer.

## Standard contiguous slots

In `registerRecipeTransferHandlers`:

```java
registration.addRecipeTransferHandler(
    MachineMenu.class,
    ModMenus.MACHINE.get(),
    DemoRecipe.TYPE,
    0, 2,
    3, 36
);
```

`MachineMenu` and `ModMenus` refer to your mod. This example assumes:

| Menu slot indices | Role |
| --- | --- |
| `0..1` | Two recipe input slots |
| `2` | Output, excluded from inputs |
| `3..38` | 36 player inventory and hotbar slots |

Arguments are the container class, optional `MenuType`, JIV recipe type, input start/count, and inventory start/count.

Indices refer to the order in **`AbstractContainerMenu.slots`**, not local indices in an underlying storage object or GUI coordinates. Check the order of `addSlot` calls and exclude output, upgrade, and unrelated slots. `MenuType` may be `null`; provide it when available to narrow matching.

## Noncontiguous and custom storage

Implement `IRecipeTransferInfo<C, R>` for inputs or inventory slots spread across ranges, then register it:

```java
registration.addRecipeTransferHandler(myTransferInfo);
```

It supplies the explicit slot lists while retaining JIV's standard transfer logic. For virtual items, templates, or unusual storage, implement `IRecipeTransferHandler<C, R>`.

## Current custom handler signature

```java
IRecipeTransferError transferRecipe(
    IRecipeTransferContext<R, C> context,
    boolean doTransfer
);
```

Also implement `getContainerClass()`, `getMenuType()`, and `getRecipeType()`. Register with `addRecipeTransferHandler(handler, recipeType)`.

The context exposes the recipe, menu, screen, player, displayed slots, transfer ID, and `isMaxTransfer()`.

- With `doTransfer=false`, check only. Do not modify inventory or send an actual transfer request.
- Return an `IRecipeTransferError` on failure. `registration.getTransferHelper()` supplies error helpers.
- With `doTransfer=true`, request the transfer and report completion using `context.completeRecipeTransfer(RecipeTransferResult.SUCCESS)` or `REJECTED`.
- Returning `null` means no error from that invocation. An asynchronous request being sent does not mean the server completed it; report completion after confirmation.

Do not complete a check or report the same actual request twice. Template systems supporting multiple categories can use `IUniversalRecipeTransferHandler<C>`.

## Observe actual transfers

```java
@Override
public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
    registration.addRecipeTransferListener(new IRecipeTransferListener() {
        @Override
        public void beforeRecipeTransfer(IRecipeTransferContext<?, ?> context) {
            // Record the request, for example context.getTransferId().
        }

        @Override
        public void afterRecipeTransfer(IRecipeTransferContext<?, ?> context,
                                        RecipeTransferResult result) {
            // Update feedback for SUCCESS or REJECTED.
        }
    });
}
```

`beforeRecipeTransfer` runs immediately before the selected handler performs an actual attempt, not on availability checks. `afterRecipeTransfer` runs when the handler reports completion, with the same context. A listener does not implement the transfer itself.

## Server support and validation

JIV's standard item transfer requires JIV on the server. If a custom handler uses your own protocol, the server must validate the current menu, slots, item quantities, and insertion rules before updating inventory.

Verify sufficient and missing ingredients, occupied input slots, unchanged output slots, maximum transfer, and server rejection. Check mode must leave inventory unchanged.

Source: [IRecipeTransferRegistration](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/registration/IRecipeTransferRegistration.java), [IRecipeTransferHandler](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/recipe/transfer/IRecipeTransferHandler.java), [IRecipeTransferContext](https://github.com/EakerZT/JustItemsViewer/blob/main/src/main/java/eakerzt/jiv/api/recipe/transfer/IRecipeTransferContext.java).
