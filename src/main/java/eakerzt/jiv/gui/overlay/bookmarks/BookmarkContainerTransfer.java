package eakerzt.jiv.gui.overlay.bookmarks;

import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.gui.bookmarks.BookmarkList;
import eakerzt.jiv.gui.bookmarks.BookmarkRecipeData;
import eakerzt.jiv.gui.bookmarks.planning.RecipeChain;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/** NEI-style pull through vanilla storage menus; custom storage requires an integration. */
final class BookmarkContainerTransfer {
	private BookmarkContainerTransfer() {}

	static boolean pull(BookmarkList bookmarks, int group, boolean missingOnly) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null
				|| minecraft.gameMode == null
				|| !(minecraft.screen instanceof AbstractContainerScreen<?> screen)) return false;
		var menu = screen.getMenu();
		if (!(menu instanceof ChestMenu
						|| menu instanceof ShulkerBoxMenu
						|| menu instanceof HopperMenu)
				|| !menu.getCarried().isEmpty()) return false;
		var manager = Internal.getJivRuntime().getIngredientManager();
		Map<Object, Long> requested = new LinkedHashMap<>();
		Map<Object, ITypedIngredient<?>> materials = bookmarks.materials(group);
		bookmarks
				.calculation(group)
				.ifPresent(
						result -> {
							result.inputs()
									.forEach(
											(key, amount) ->
													requested.merge(key, amount, RecipeChain::add));
							if (!missingOnly) {
								result.outputs()
										.forEach(
												(key, amount) ->
														requested.merge(
																key, amount, RecipeChain::add));
								result.remainders()
										.forEach(
												(key, amount) ->
														requested.merge(
																key, amount, RecipeChain::add));
							}
						});
		for (var bookmark : bookmarks.getGroupBookmarks(group))
			if (bookmark.getType() == eakerzt.jiv.gui.bookmarks.BookmarkType.INGREDIENT) {
				var ingredient = bookmark.getElement().getTypedIngredient();
				var key = BookmarkRecipeData.key(ingredient, manager);
				requested.merge(key, bookmarks.state(bookmark).multiplier, RecipeChain::add);
				materials.put(key, ingredient);
			}
		List<Slot> inventory =
				menu.slots.stream()
						.filter(s -> s.container == minecraft.player.getInventory())
						.toList();
		boolean moved = false;
		for (var entry : requested.entrySet()) {
			var ingredient = materials.get(entry.getKey());
			if (ingredient == null) continue;
			var item = ingredient.getItemStack().orElse(ItemStack.EMPTY);
			if (item.isEmpty()) continue;
			long needed = entry.getValue();
			if (missingOnly)
				for (var slot : inventory)
					if (ItemStack.isSameItemSameComponents(item, slot.getItem()))
						needed = Math.max(0, needed - slot.getItem().getCount());
			for (var source : menu.slots) {
				if (needed == 0) break;
				if (source.container == minecraft.player.getInventory()
						|| !source.mayPickup(minecraft.player)) continue;
				ItemStack stack = source.getItem();
				if (stack.isEmpty() || !ItemStack.isSameItemSameComponents(item, stack)) continue;
				long capacity = 0;
				for (var target : inventory)
					if (target.mayPlace(stack)
							&& (target.getItem().isEmpty()
									|| ItemStack.isSameItemSameComponents(target.getItem(), stack)))
						capacity +=
								Math.max(
										0,
										target.getMaxStackSize(stack)
												- target.getItem().getCount());
				int take = (int) Math.min(Math.min(needed, stack.getCount()), capacity);
				if (take == 0) continue;
				if (take == stack.getCount()) {
					minecraft.gameMode.handleContainerInput(
							menu.containerId,
							source.index,
							0,
							ContainerInput.QUICK_MOVE,
							minecraft.player);
				} else {
					minecraft.gameMode.handleContainerInput(
							menu.containerId,
							source.index,
							0,
							ContainerInput.PICKUP,
							minecraft.player);
					int remaining = take;
					for (var target : inventory) {
						ItemStack carried = menu.getCarried();
						if (remaining == 0 || carried.isEmpty()) break;
						if (!target.mayPlace(carried)
								|| !target.getItem().isEmpty()
										&& !ItemStack.isSameItemSameComponents(
												target.getItem(), carried)) continue;
						int available =
								Math.max(
										0,
										target.getMaxStackSize(carried)
												- target.getItem().getCount());
						int put = Math.min(remaining, available);
						if (put == carried.getCount() || put == available)
							minecraft.gameMode.handleContainerInput(
									menu.containerId,
									target.index,
									0,
									ContainerInput.PICKUP,
									minecraft.player);
						else
							for (int i = 0; i < put; i++)
								minecraft.gameMode.handleContainerInput(
										menu.containerId,
										target.index,
										1,
										ContainerInput.PICKUP,
										minecraft.player);
						remaining -= put;
					}
					if (!menu.getCarried().isEmpty())
						minecraft.gameMode.handleContainerInput(
								menu.containerId,
								source.index,
								0,
								ContainerInput.PICKUP,
								minecraft.player);
				}
				needed -= take;
				moved = true;
				if (!menu.getCarried().isEmpty()) return moved;
			}
		}
		return moved;
	}
}
