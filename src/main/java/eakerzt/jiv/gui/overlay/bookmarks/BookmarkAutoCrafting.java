package eakerzt.jiv.gui.overlay.bookmarks;

import eakerzt.jiv.api.constants.VanillaTypes;
import eakerzt.jiv.api.gui.IRecipeLayoutDrawable;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.recipe.transfer.RecipeTransferResult;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.network.packets.PacketBookmarkCraft;
import eakerzt.jiv.common.network.packets.PacketBookmarkCraftResult;
import eakerzt.jiv.common.network.packets.PacketRecipeTransferWithResult;
import eakerzt.jiv.common.network.packets.PacketRecipeTransferResult;
import eakerzt.jiv.common.transfer.RecipeTransferService;
import eakerzt.jiv.gui.bookmarks.*;
import eakerzt.jiv.gui.bookmarks.planning.BookmarkCraftPlan;
import eakerzt.jiv.gui.bookmarks.planning.RecipeChain;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Serial, server-acknowledged one-batch crafts in a vanilla crafting menu. */
public final class BookmarkAutoCrafting {
	private static @Nullable BookmarkAutoCrafting active;
	private record Step(RecipeBookmark<?, ?> bookmark, BookmarkRecipeData data, BookmarkState state, long batches) {}
	private enum Phase { FILL, WAIT_FILL, TAKE, WAIT_TAKE }
	private final AbstractContainerScreen<?> screen;
	private final List<Step> steps;
	private final RecipeTransferService transfers;
	private final Map<Object, Long> reservedTargets;
	private int waitingTicks, requestId, transferId;
	private final Map<Step, Long> remaining = new LinkedHashMap<>();
	private @Nullable Step currentStep;
	private Phase phase = Phase.FILL;

	private BookmarkAutoCrafting(AbstractContainerScreen<?> screen, List<Step> steps, Map<Object, Long> reservedTargets) {
		this.screen = screen;
		this.steps = steps;
		steps.forEach(step -> remaining.put(step, step.batches()));
		this.transfers = new RecipeTransferService(Internal.getJivRuntime().getRecipeTransferManager());
		this.reservedTargets = Map.copyOf(reservedTargets);
	}

	public static void toggle(BookmarkList bookmarks, int group, BookmarkCraftPlan.Mode mode) {
		if (active != null) { active.stop("stopped"); return; }
		var minecraft = Minecraft.getInstance();
		if (minecraft.player == null || !(minecraft.screen instanceof AbstractContainerScreen<?> container)
				|| !(container.getMenu() instanceof CraftingMenu || container.getMenu() instanceof InventoryMenu)) {
			message("workbench", false); return;
		}
		if (!Internal.getServerConnection().canSendPacket(PacketBookmarkCraft.TYPE)
				|| !Internal.getServerConnection().canSendPacket(PacketRecipeTransferWithResult.TYPE)) {
			message("server", false); return;
		}
		if (!container.getMenu().getCarried().isEmpty()) { message("cursor", false); return; }
		var runtime = Internal.getJivRuntime();
		var manager = runtime.getIngredientManager();
		var recipes = new ArrayList<RecipeChain.Recipe>();
		var targets = new LinkedHashMap<Object, Long>();
		var snapshots = new LinkedHashMap<Object, Step>();
		for (var bookmark : bookmarks.getGroupBookmarks(group)) {
			if (!(bookmark instanceof RecipeBookmark<?, ?> recipe)) {
				if (bookmark.getType() == BookmarkType.INGREDIENT)
					targets.merge(BookmarkRecipeData.key(bookmark.getElement().getTypedIngredient(), manager),
							Math.max(1, bookmarks.state(bookmark).multiplier), RecipeChain::add);
				continue;
			}
			var data = BookmarkRecipeData.create(recipe, runtime.getRecipeManager(), runtime.getJivHelpers().getFocusFactory());
			var state = bookmarks.state(bookmark).copy();
			recipes.add(data.asRecipe(bookmark, state, manager));
			snapshots.put(bookmark, new Step(recipe, data, state, 0));
		}
		var inventory = inventory(container);
		var settings = bookmarks.page().groups.get(group);
		targets.forEach((key, amount) -> inventory.computeIfPresent(key, (ignored, stock) -> Math.max(0, stock - amount)));
		var plan = BookmarkCraftPlan.calculate(recipes, settings != null && settings.linked, inventory, mode);
		if (plan.cycle() || plan.overflow() || plan.uncertain()) { message("invalid", false); return; }
		var steps = new ArrayList<Step>();
		for (var id : BookmarkCraftPlan.executionOrder(plan)) {
			var snapshot = snapshots.get(id);
			if (!(snapshot.bookmark().getRecipe() instanceof RecipeHolder<?> holder)
					|| !(holder.value() instanceof CraftingRecipe)
					|| snapshot.data().slots().stream().anyMatch(slot -> slot.role() == RecipeIngredientRole.INPUT
							&& snapshot.state().removedSlots.contains(slot.index()))) {
				message("invalid", false); return;
			}
			steps.add(new Step(snapshot.bookmark(), snapshot.data(), snapshot.state(), plan.crafts().get(id)));
		}
		if (steps.isEmpty()) { message("complete", true); return; }
		active = new BookmarkAutoCrafting(container, List.copyOf(steps), targets);
		message("started", true);
	}

	public static void tick() {
		var task = active;
		if (task == null) return;
		var minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.screen != task.screen
				|| minecraft.player.containerMenu != task.screen.getMenu()) { task.stop("stopped"); return; }
		if (++task.waitingTicks > 100) { task.stop("timeout"); return; }
		if (!task.screen.getMenu().getCarried().isEmpty()) { task.stop("cursor"); return; }
		if (task.phase == Phase.FILL) task.fill();
		else if (task.phase == Phase.TAKE && task.currentStep != null) task.take(task.currentStep);
	}

	private void fill() {
		for (var step : steps) {
			if (remaining.get(step) > 0 && fillRecipe(step.bookmark(), step)) return;
			if (active != this) return;
		}
		stop("missing");
	}

	private <R> boolean fillRecipe(RecipeBookmark<R, ?> bookmark, Step step) {
		var runtime = Internal.getJivRuntime();
		var stock = inventory(screen);
		reservedTargets.forEach((key, amount) -> stock.computeIfPresent(key, (ignored, available) -> Math.max(0, available - amount)));
		var inputs = new LinkedHashMap<Object, Long>();
		for (var slot : step.data().slots()) {
			if (slot.role() != RecipeIngredientRole.INPUT) continue;
			var ingredient = slot.selected(step.state());
			inputs.merge(BookmarkRecipeData.key(ingredient, runtime.getIngredientManager()),
					BookmarkRecipeData.amount(ingredient, runtime.getIngredientManager()), RecipeChain::add);
		}
		if (inputs.entrySet().stream().anyMatch(entry -> stock.getOrDefault(entry.getKey(), 0L) < entry.getValue())) return false;
		IRecipeLayoutDrawable<R> layout = runtime.getRecipeManager().createRecipeLayoutDrawable(
				bookmark.getRecipeCategory(), bookmark.getRecipe(), runtime.getJivHelpers().getFocusFactory().getEmptyFocusGroup())
				.orElse(null);
		if (layout == null) { stop("invalid"); return true; }
		var slots = new BookmarkTransferSlots(layout.getRecipeSlotsView(), step.data(), step.state());
		if (transfers.getTransferRecipeError(screen, layout, Minecraft.getInstance().player, slots).isPresent()) return false;
		currentStep = step;
		phase = Phase.WAIT_FILL;
		waitingTicks = 0;
		var error = transfers.transferRecipe(screen, layout, Minecraft.getInstance().player, false, true, slots,
				id -> transferId = id, result -> {
			if (active != this) return;
			transferId = 0;
			if (result == RecipeTransferResult.SUCCESS) { phase = Phase.TAKE; waitingTicks = 0; }
			else stop("failed");
		});
		if (error.isPresent()) stop("failed");
		return true;
	}

	private void take(Step step) {
		var output = step.data().slots().stream().filter(slot -> slot.role() == RecipeIngredientRole.OUTPUT)
				.map(slot -> slot.selected(step.state()).getItemStack().orElse(ItemStack.EMPTY))
				.filter(stack -> !stack.isEmpty()).findFirst().orElse(ItemStack.EMPTY);
		if (output.isEmpty()) { stop("invalid"); return; }
		phase = Phase.WAIT_TAKE;
		waitingTicks = 0;
		requestId = PacketBookmarkCraftResult.register(success -> {
			if (active != this) return;
			requestId = 0;
			if (!success) { stop("failed"); return; }
			remaining.computeIfPresent(step, (ignored, batches) -> batches - 1);
			if (remaining.values().stream().allMatch(batches -> batches == 0)) { stop("complete"); return; }
			phase = Phase.FILL;
			waitingTicks = 0;
		});
		Internal.getServerConnection().sendPacketToServer(new PacketBookmarkCraft(screen.getMenu().containerId, requestId, output));
	}

	private void stop(String reason) {
		if (transferId != 0) PacketRecipeTransferResult.cancelPendingRecipeTransfer(transferId);
		if (requestId != 0) PacketBookmarkCraftResult.cancel(requestId);
		if (active == this) active = null;
		message(reason, reason.equals("complete"));
	}

	private static void message(String reason, boolean success) {
		var player = Minecraft.getInstance().player;
		if (player != null) Minecraft.getInstance().gui.setOverlayMessage(Component.translatable("jiv.bookmarks.craft." + reason)
				.withStyle(success ? ChatFormatting.GREEN : ChatFormatting.RED), false);
	}

	private static Map<Object, Long> inventory(AbstractContainerScreen<?> screen) {
		var minecraft = Minecraft.getInstance();
		var manager = Internal.getJivRuntime().getIngredientManager();
		Map<Object, Long> inventory = new LinkedHashMap<>();
		for (var slot : screen.getMenu().slots) {
			if (slot instanceof ResultSlot || !slot.mayPickup(minecraft.player)) continue;
			if (slot.container == minecraft.player.getInventory()
					&& (slot.getContainerSlot() < 0 || slot.getContainerSlot() >= 36)) continue;
			ItemStack stack = slot.getItem();
			if (!stack.isEmpty()) manager.createTypedIngredient(VanillaTypes.ITEM_STACK, stack, false)
					.ifPresent(ingredient -> inventory.merge(BookmarkRecipeData.key(ingredient, manager),
							(long) stack.getCount(), RecipeChain::add));
		}
		return inventory;
	}
}
