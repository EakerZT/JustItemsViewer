# Just Items Viewer

**Browse items, find recipes, and discover uses from your inventory.**

Just Items Viewer (**JIV**) is an item and recipe viewer for **Minecraft 26.1.2 on NeoForge**. Open your inventory or a crafting screen to browse items, search for materials, and follow recipes to see how items are made and what they can be used for.

| Minecraft | Mod Loader | Current Version |
| --- | --- | --- |
| 26.1.2 | NeoForge | 0.0.1-alpha-1 |

## Features

- **Item browsing and search**: Browse items in a sidebar and filter by name, mod, or tag.
- **Recipes and uses**: See how to make an item and which recipes use it.
- **Item and recipe bookmarks**: Save frequently used items and recipes for quick access.
- **Lookup history**: Revisit recently viewed items without searching again.
- **Recipe transfer**: Move ingredients from your inventory into recipe slots in supported containers.
- **Built-in settings**: Customize item lists, search behavior, bookmarks, and tooltips.
- **Cheat mode**: Obtain items directly when you have the required permissions.

## Installation

1. Install **Minecraft 26.1.2** with **NeoForge 26.1.2.99 or a newer NeoForge release for Minecraft 26.1.2**.
2. Place `jiv-26.1.2-neoforge-0.0.1-alpha-1.jar` in your game instance's `mods` folder.
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

You can change these shortcuts under **Options → Controls → Key Binds**. On macOS, use `Command` instead of `Ctrl`.

Type an item name in the search bar, or use a prefix to narrow your search:

| Example | Search Scope |
| --- | --- |
| `@minecraft` | Mod names or mod IDs |
| `#logs` | Item tags |
| `$fuel` | Tooltip text |

Search uses your current game language. You can also enable item ID, creative tab, and color searches in the settings.

## Settings

Click the **settings button** beside the item list, or open JIV's configuration screen from the NeoForge mod list.

Adjust the list layout, item sorting, search options, bookmark display, and tooltip behavior. Configuration files are stored in your game instance's `config/jiv/` folder.

## Compatibility

JIV currently supports **Minecraft 26.1.2 and NeoForge**.

Recipes provided by other mods through the standard recipe system can be viewed in JIV. Custom recipe displays that depend on dedicated interfaces or JEI plugins require JIV integration. Existing JEI plugins cannot be loaded directly.

## Credits and License

JIV is maintained by **eakerzt** and is derived from [Just Enough Items](https://github.com/mezz/JustEnoughItems). Thanks to mezz and the upstream contributors for the item browsing, recipe lookup, and supporting functionality.

JIV is an independent fork. Original credits and licenses are preserved; see [LICENSE.txt](LICENSE.txt) and [NOTICE.txt](NOTICE.txt).
