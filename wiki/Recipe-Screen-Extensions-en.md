[English](Recipe-Screen-Extensions-en.md) | [简体中文](Recipe-Screen-Extensions.md)

# Recipe-screen extension API

**Availability: JIV `0.0.1-alpha-2` and later, published to Maven Central.** Use alpha-2 or newer at compile time and runtime. The `0.0.1-alpha-1` artifact does not contain these methods.

Use this API for recipe-specific titles, left/right ingredient panels and direct mouse gestures in the recipe screen, without Mixins or implementation classes. Register per-layout extensions in `IRecipeCategory.createRecipeExtras`. Extensions do not change global crafting-station registration or execute server-side gameplay operations.

## Registration and pagination

In an existing `IRecipeCategory<DemoRecipe>`, `gui` is the category's `IGuiHelper`; the recipe methods below represent your own data model:

```java
@Override
public int getMaxRecipesPerPage() { return 1; }

@Override
public void createRecipeExtras(IRecipeExtrasBuilder builder, DemoRecipe recipe, IFocusGroup focuses) {
    builder.addScreenExtension(new RecipePanelExtension(gui, recipe.materials(), false, recipe.title()));
    builder.addScreenExtension(new RecipePanelExtension(gui, recipe.alternatives(), true, recipe.title()));
    // Register ordinary preview/text widgets separately with builder.addWidget(...).
}
```

`getMaxRecipesPerPage` defaults to `Integer.MAX_VALUE`; it adds no limit beyond the available physical space. Values below one become one. With several visible recipes, the first non-null extension title in layout/registration order wins.

Changing category width or height invalidates cached layouts on the next screen-layout update, including GUI-scale changes. This does not automatically relayout every frame. Per-recipe state belongs in the extension, not shared category fields.

## Method reference

All extension methods have defaults:

| Method | Default and purpose |
| --- | --- |
| `getScreenTitle()` | `null`; falls back to category title. Non-null titles use existing centering, truncation and full-title tooltip behavior |
| `updateScreenLayout(IGuiProperties, int availableSideHeight)` | No operation; base recipe GUI position and usable side height, excluding extension bounds |
| `getExtraGuiAreas()` | Empty list; occupied rectangles for overlay avoidance and screen hit-testing |
| `drawScreen(GuiGraphicsExtractor, int mouseX, int mouseY)` | No operation; draws panels and chrome |
| `drawScreenTooltips(GuiGraphicsExtractor, int mouseX, int mouseY)` | No operation; skipped while an interactive ingredient tooltip is visible |
| `getScreenSlotUnderMouse(double mouseX, double mouseY)` | `Optional.empty()`; supplies a slot for ingredient queries and interactive tooltips |
| `mouseClicked(double x, double y, int button)` | `false`; `true` consumes the real press and captures that button |
| `mouseDragged(double x, double y, int button, double dx, double dy)` | `false`; invoked for a captured button |
| `mouseReleased(double x, double y, int button)` | `false`; receives and ends that button's capture |
| `mouseScrolled(double x, double y, double scrollX, double scrollY)` | `false`; `true` consumes scrolling before ordinary page/category navigation |
| `cancelScreenInteraction()` | No operation; clears extension gesture state when a layout is replaced or the screen is closed/removed |

Layout callbacks and queries repeat; do not register more extensions or global ingredients from them. `IRecipeLayoutDrawable.getScreenExtensions()` creates recipe extras on first access and returns a read-only snapshot. Layout interfaces are implemented by JIV, not by plugins.

## Coordinates and pointer capture

| Callback / data | Coordinates |
| --- | --- |
| Base `IGuiProperties` | Absolute screen position of the recipe window, including header/navigation |
| Extra rectangles, rendering and slot queries | Absolute screen coordinates |
| Mouse callbacks | Relative to the recipe layout origin; may be outside its bounds |
| Drag `dx` / `dy` | Movement delta for this event |

Extensions perform their own gesture hit-testing. There is no additional `getArea()` translation. Absolute-position slots use `new RecipeSlotUnderMouse(slot, 0, 0)`; do not subtract another window or recipe offset.

A press callback is a real press before ordinary input handling, not the usual simulate-on-press/execute-on-release flow. Consuming a press captures that button through drag and release outside the preview. Captured drag/release remain consumed even when those callbacks return `false`; releasing another button leaves capture intact.

Replacing a layout, closing the screen or switching away cancels capture and calls `cancelScreenInteraction`. Reset your own gesture flags there. Cancellation is not a normal release and must not commit actions such as right-click selection. For rotation, consume a left press inside your preview, update its angle from drag deltas, clear the flag on left release, and also clear it on cancellation.

## Native panel example

`IGuiHelper.drawRecipeSidePanel(GuiGraphicsExtractor, Rect2i bounds, boolean rightSide)` draws a native frame and slot background. The right variant uses a dedicated frame texture with the same top-left lighting as the left panel. Positive dimensions must be at least 17 by 16 pixels; a non-positive dimension draws nothing; smaller positive dimensions throw `IllegalArgumentException`. The helper does not draw ingredients, supply tooltips or perform hit-testing.

The complete example below preserves stack counts, adds columns instead of scrolling, hides empty panels, and keeps a single candidate visible. Rebuild slots when your ingredient contents change and update positions from the layout callback. Occupied bounds reserve space; they do not clip panels or guarantee that arbitrary panel widths fit the screen.

```java
package examplemod.jiv;

import eakerzt.jiv.api.constants.VanillaTypes;
import eakerzt.jiv.api.gui.handlers.IGuiProperties;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotDrawable;
import eakerzt.jiv.api.gui.inputs.RecipeSlotUnderMouse;
import eakerzt.jiv.api.gui.widgets.IRecipeScreenExtension;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Source-build example: native frame, counted slots, column overflow, and ingredient lookup. */
public final class RecipePanelExtension implements IRecipeScreenExtension {
    private final IGuiHelper gui;
    private final List<IRecipeSlotDrawable> slots;
    private final boolean right;
    private final Component title;
    private Rect2i bounds = new Rect2i(0, 0, 0, 0);

    public RecipePanelExtension(IGuiHelper gui, List<ItemStack> contents, boolean right, Component title) {
        this.gui = gui;
        this.right = right;
        this.title = title;
        this.slots = contents.stream().map(stack -> {
            ItemStack copy = stack.copy();
            return gui.createRecipeSlotDrawable(RecipeIngredientRole.INPUT,
                acceptor -> acceptor.add(VanillaTypes.ITEM_STACK, copy), Set.of(), 0);
        }).toList();
    }

    @Override
    public Component getScreenTitle() { return title; }

    @Override
    public void updateScreenLayout(IGuiProperties area, int availableSideHeight) {
        if (slots.isEmpty()) {
            bounds = new Rect2i(0, 0, 0, 0);
            return;
        }
        int maxRows = Math.max(1, (availableSideHeight - 12) / 16);
        int columns = Math.ceilDiv(slots.size(), maxRows);
        int rows = Math.ceilDiv(slots.size(), columns);
        int width = 12 + columns * 16;
        int x = right ? area.guiRight() - 6 : area.guiLeft() - width + 6;
        bounds = new Rect2i(x, area.guiTop(), width, 12 + rows * 16);
        for (int i = 0; i < slots.size(); i++) {
            int column = right ? i / rows : columns - 1 - i / rows;
            slots.get(i).setPosition(x + 6 + column * 16, area.guiTop() + 6 + i % rows * 16);
        }
    }

    @Override
    public List<ScreenRectangle> getExtraGuiAreas() {
        if (slots.isEmpty()) return List.of();
        return List.of(new ScreenRectangle(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight()));
    }

    @Override
    public void drawScreen(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (slots.isEmpty()) return;
        gui.drawRecipeSidePanel(graphics, bounds, right);
        slots.forEach(slot -> slot.draw(graphics, slot.isMouseOver(mouseX, mouseY)));
    }

    @Override
    public Optional<RecipeSlotUnderMouse> getScreenSlotUnderMouse(double mouseX, double mouseY) {
        return slots.stream().filter(slot -> slot.isMouseOver(mouseX, mouseY))
            .findFirst().map(slot -> new RecipeSlotUnderMouse(slot, 0, 0));
    }

    @Override
    public void drawScreenTooltips(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        getScreenSlotUnderMouse(mouseX, mouseY)
            .ifPresent(hovered -> hovered.slot().drawTooltip(graphics, mouseX, mouseY));
    }
}
```

## Lifetime and scope

Extensions belong to a concrete layout; recipe switches and dimension changes may create new objects. Keep rotation, selection and capture state out of global singletons or shared category fields. A recipe view may implement both ordinary widget and screen-extension interfaces, registered separately.

These callbacks run only in the recipe screen, not in layouts embedded in bookmarks or item tooltips. Ordinary widgets continue to work there. Server-affecting buttons need your own network protocol and server-side validation.
