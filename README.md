# Just Items Viewer

**English** | [简体中文](README-zh-CN.md)

**Browse items, find recipes, and discover uses from your inventory.**

Just Items Viewer (**JIV**) is an item and recipe viewer for **Minecraft 26.1.2 on NeoForge**. Open your inventory or a crafting screen to browse items, search for materials, and follow recipes to see how items are made and what they can be used for.

| Minecraft | Mod Loader | Current Version |
| --- | --- | --- |
| 26.1.2 | NeoForge | 0.0.1-alpha-3 |

Release notes: [0.0.1-alpha-3](CHANGELOG.md). This version is published to [Maven Central](https://central.sonatype.com/artifact/io.github.eakerzt/jiv-26.1.2-neoforge/0.0.1-alpha-3).

## Features

- **Item browsing and search**: Browse items in a sidebar and filter by name, mod, or tag.
- **Recipes and uses**: See how to make an item and which recipes use it.
- **Item and recipe bookmarks**: Save items, fluids, or selected recipe outputs; organize workspaces and subgroups, reorder with live drag previews, and choose substitute ingredients.
- **Recipe chains and crafting trees**: Calculate material requirements from bookmarked recipes, inspect intermediate steps, and display crafting-medium/category badges.
- **Lookup history**: Revisit recently viewed items without searching again.
- **Recipe transfer**: Move ingredients from your inventory into recipe slots in supported containers.
- **Recipe display API**: Add recipe screen extensions, native side panels, fluid amount labels, and non-consumed/chance slot markers.
- **Built-in settings**: Customize item lists, search behavior, bookmarks, and tooltips.
- **Cheat mode**: Obtain items directly when you have the required permissions.

## Installation

1. Install **Minecraft 26.1.2** with **NeoForge 26.1.2.99 or a newer NeoForge release for Minecraft 26.1.2**.
2. Place `jiv-26.1.2-neoforge-0.0.1-alpha-3.jar` in your game instance's `mods` folder.
3. Launch the game and open your inventory.

Settings and search are included. **No separate MezzConfig, MezzConfigGUI, or search library installation is required.**

You can browse items and view recipes with JIV installed on the client. In multiplayer, features that require server support, such as recipe transfer, also require JIV on the server. Cheat mode follows the server's permission settings.

## Getting Started

Use these default shortcuts while a GUI is open. For recipes, uses, and bookmarks, hover over an item first.

| Action | Default Shortcut |
| --- | --- |
| View recipes | `R` |
| View uses | `U` |
| Add or remove an item bookmark | `A` |
| Show or hide JIV overlays | `Ctrl + O` |
| Focus the search bar | `Ctrl + F` |

You can change these shortcuts under **Options → Controls → Key Binds**. The bookmark modifier gestures documented below use the physical `Ctrl` key on macOS as well; some other configurable shortcuts use `Command`.

Type an item name in the search bar, or use a prefix to narrow your search:

| Example | Search Scope |
| --- | --- |
| `@minecraft` | Mod names or mod IDs |
| `#logs` | Item tags |
| `$fuel` | Tooltip text |

Search uses your current game language. You can also enable item ID, creative tab, and color searches in the settings.

## Bookmark Controls

Hover over a slot before using a bookmark shortcut. `A` below means the configured **Bookmark** key, whose default is A. Repeating the same shortcut removes that record; item bookmarks, single-output recipe bookmarks, and all-output recipe bookmarks are independent.

| Location / Action | Default Operation |
| --- | --- |
| Any ingredient slot: add/remove the item or fluid only, including recipe outputs | `A` |
| Recipe slot or recipe bookmark: add/remove a recipe with the hovered output only; over an input, select the recipe's first output | `Ctrl + A` |
| Recipe slot or recipe bookmark: add/remove a recipe with every output | `Ctrl + Shift + A` |
| Recipe screen bookmark button | Add/remove the complete recipe |
| Recipe bookmark output: open the saved recipe | Left-click / `R` |
| Any ingredient: view uses | Right-click / `U` |
| Recipe bookmark output: move the entire recipe, including between subgroups | `Ctrl + Left-drag` |
| Recipe bookmark input: reorder the merged input cells within that recipe | `Ctrl + Left-drag` onto another input of the same recipe |
| Independent item bookmark: reorder the bookmark, including between subgroups | `Ctrl + Left-drag` |
| Recipe bookmark: collapse/expand its inputs | `Alt + Left-click` |
| Output: adjust recipe batches; independent item: adjust amount | `Ctrl + Wheel`; add `Alt` for stack-size steps |
| Replaceable input: select the previous/next candidate | `Ctrl + Wheel` |
| Ingredient slot: copy its displayed name | `Ctrl + C` |
| Recipe bookmark: transfer once / as many times as possible | `Alt + Right-click` / `Ctrl + Right-click` (configurable transfer bindings may use Command on macOS) |
| Show/hide bookmarks | `B` |

`Ctrl + A` retains text selection when a search/text field has keyboard focus. With the mouse over an ordinary inventory/item-list slot, the recipe shortcuts do not choose an arbitrary recipe.

A single-output recipe records only that output for display **and calculation**. Other outputs do not supply a linked recipe or appear as leftovers. Use `Ctrl + Shift + A` when those outputs should participate. All input requirements remain intact. The full recipe is still available when opening the bookmark or transferring its ingredients.

Drag a recipe to the space below the final subgroup (or the bottom edge of its last row) to place it after the subgroup in the default group. Default-group recipes can appear before, between, or after subgroups. `Ctrl + Left-drag` a subgroup bracket moves all its bookmarks as one block, with live placeholders; release confirms and cancellation restores its position. Group settings and internal order are retained.

During sorting, the dragged input becomes a gray placeholder that moves to preview the insertion position; other inputs shift immediately. Dragging an output previews the entire recipe block and carries its visible cells together. Release to keep the last valid preview, even if the cursor has left the destination; cancel the drag to restore the original order. Input sorting keeps the viewport and all other recipes fixed. Preview changes are saved only after a successful drop.

Repeated equal inputs are merged, and moving a merged input moves all its source slots together without changing quantities or candidate choices. Inputs cannot be dragged into another recipe. Input order, chosen outputs, candidates, quantities, grouping and layout are saved. Existing recipe records retain all outputs when loaded.

### Modes, Hover and Candidate Lists

The icon beside the top-left navigation arrow shows the **default subgroup's** current layout. Click it to switch **item mode** (recipe outputs only) / **recipe mode** (outputs and inputs in recipe rows). Other subgroups keep their own layouts. Navigation and mode buttons use the same size; counters scale to fit narrow sidebars. This changes presentation, not what `A`, `Ctrl + A`, or `Ctrl + Shift + A` records.

In recipe mode, hovering any material colors its recipe's visible cells: blue for outputs, green for inputs, purple for leftover outputs. The hovered cell's normal highlight is layered above this background. Holding Shift is not required.

Replaceable inputs show `#` for a recognized tag, or `≡` for a candidate list. Hover to see the list; its current selection has a green border. Hold `Shift` to focus the tooltip, scroll its candidate list, and query individual candidates with left/right-click or `R`/`U`. Non-consumed inputs show the same green `∞` as recipe slots. `Shift` on a linked subgroup also shows aggregate required/output quantities.

### Subgroups and Pages

The seven-pixel gutter to the left of the slots controls subgroups within the current bookmark workspace. The top counter is `current workspace / total workspaces`; a second `current page / total pages` counter appears only when there is more than one content page.

| Location / Action | Operation |
| --- | --- |
| Top left/right arrows | Switch bookmark workspaces; advancing beyond the last nonempty workspace creates an empty one |
| Bookmark contents | Wheel to browse the current workspace's content pages / scroll rows |
| Drag a bookmark near a page edge | Flip/scroll through the current workspace while sorting |
| Mode icon | Switch the default subgroup's item/recipe layout |
| Group bracket: left-click | Switch that subgroup's item/recipe layout |
| Group bracket or center counter: right-click | Enable/disable recipe-chain calculation |
| Group bracket or center counter: `Alt + Left-click` | Collapse/expand the subgroup |
| Gutter: left-drag over rows | Create a subgroup or add rows to the starting subgroup |
| Gutter: right-drag over rows | Remove the selected rows from their subgroup |
| Group bracket: `Ctrl + Left-drag` | Move the subgroup |
| Group bracket or center counter: `Shift + A` without Ctrl/Alt | Delete that subgroup and its bookmarks |
| Group bracket or center counter: Show Recipes key (default `R`) | Open the crafting tree; follows the configured recipe key |
| Group bracket or center counter: `V` / `Shift + V` | Pull required materials / only missing materials from a supported container |

### Crafting Tree and Settings

Crafting-tree recipe nodes display the same crafting-medium/category badge as the bookmark sidebar, including intermediate recipes and collapsed branches; raw materials have no recipe badge.

In the crafting tree, left-drag pans, the wheel zooms, left-click on a node collapses/expands its branch, right-click opens its recipe, and `Ctrl + C` copies a node's ingredient name. Toolbar controls refresh inventory quantities (`I`), fit the tree (`F`), collapse branches, and show/hide statistics (`S`). Scroll over the statistics panel to browse it. Press `Esc` to return.

Bookmarks and lookup history are enabled by default; lookup history appears on the right. Change visibility and history placement in the configuration screen. Sorting can be disabled in configuration; drag delay and placement of new bookmarks are configurable.

Ordinary item bookmarks initially have no saved amount and display no count. Recipe shortcuts initially record one batch. Recipe quantities multiply consumed inputs and recorded outputs; non-consumed tools are not multiplied.

## Settings

Click the **settings button** beside the item list, or open JIV's configuration screen from the NeoForge mod list.

Adjust the list layout, item sorting, search options, bookmark display, and tooltip behavior. Configuration files are stored in your game instance's `config/jiv/` folder.

## Compatibility

JIV currently supports **Minecraft 26.1.2 and NeoForge**.

Recipes provided by other mods through the standard recipe system can be viewed in JIV. Custom recipe displays that depend on dedicated interfaces or JEI plugins require JIV integration. Existing JEI plugins cannot be loaded directly.

## Developer API

For mod integration, see the [English / Chinese developer Wiki](https://github.com/EakerZT/JustItemsViewer/wiki): plugin setup, recipes, ingredients, GUI integration, recipe transfer, runtime access, and the built-in configuration API. The [getting started guide](https://github.com/EakerZT/JustItemsViewer/wiki/Getting-Started-en) includes the Maven Central dependency, and [complete examples](https://github.com/EakerZT/JustItemsViewer/wiki/Examples-en) are available in both languages.

## Credits and License

JIV is maintained by **eakerzt** and is derived from [Just Enough Items](https://github.com/mezz/JustEnoughItems). Thanks to mezz and the upstream contributors for the item browsing, recipe lookup, and supporting functionality.

Bookmark grouping, sorting, recipe chains, and the crafting tree draw on [GTNH Not Enough Items](https://github.com/GTNewHorizons/NotEnoughItems). The crafting tree uses its original texture; attribution and applicable licenses are preserved in [NOTICE.txt](NOTICE.txt).

JIV is an independent fork. Original credits and licenses are preserved; see [LICENSE.txt](LICENSE.txt) and [NOTICE.txt](NOTICE.txt).
