package eakerzt.jiv.gui.overlay.bookmarks;

import eakerzt.jiv.api.constants.VanillaTypes;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.gui.JivTooltip;
import eakerzt.jiv.config.gui.ConfigScreenBase;
import eakerzt.jiv.gui.bookmarks.*;
import eakerzt.jiv.gui.bookmarks.planning.RecipeChain;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import org.lwjgl.glfw.GLFW;

import java.util.*;

/** Selected-recipe graph with NEI-style pan/zoom, inventory snapshot and statistics. */
public final class BookmarkTreeScreen extends ConfigScreenBase {
	private final Screen parent;
	private final BookmarkList bookmarks;
	private final int group;
	private RecipeChain.Result calculation;
	private final Map<Object, Long> inventory = new LinkedHashMap<>();
	private final Map<Object, Node> nodes = new LinkedHashMap<>();
    private final Map<IBookmark, eakerzt.jiv.api.gui.drawable.IDrawable> recipeIcons = new IdentityHashMap<>();
	private boolean stats = true, inventoryEnabled;
	private float zoom = 1, panX = 24, panY = 42;
	private final Set<Object> collapsed = new HashSet<>();
	private int statsScroll;
	private static final net.minecraft.resources.Identifier TEXTURE =
			net.minecraft.resources.Identifier.fromNamespaceAndPath(
					"jiv", "textures/gui/gtnh-nei/craftingtree.png");
	private final Map<Object, List<Object>> edges = new LinkedHashMap<>();
	private int leafX;

	private record Node(
			IBookmark bookmark,
			ITypedIngredient<?> ingredient,
			int x,
			int y,
			long crafts,
			long amount,
			boolean leaf) {}

	public BookmarkTreeScreen(Screen parent, BookmarkList bookmarks, int group) {
		super(Component.translatable("jiv.bookmarks.tree.title"));
		this.parent = parent;
		this.bookmarks = bookmarks;
		this.group = group;
		rebuild();
	}

	private void rebuild() {
		calculation = bookmarks.calculateWithInventory(group, inventory);
		nodes.clear();
		edges.clear();
        recipeIcons.clear();
		leafX = 0;
		Set<Object> providers = new HashSet<>();
		calculation.dependencies().values().forEach(providers::addAll);
		for (var bookmark : bookmarks.getGroupBookmarks(group))
			if (calculation.crafts().containsKey(bookmark) && !providers.contains(bookmark)) {
				var data = bookmarks.data(bookmark).orElseThrow();
				var output =
						data
								.displaySlots(
										bookmarks.state(bookmark),
										Internal.getJivRuntime().getIngredientManager())
								.stream()
								.filter(
										display ->
												display.slot().role()
														== RecipeIngredientRole.OUTPUT)
								.findFirst();
				if (output.isPresent()) {
					var ingredient = output.get().ingredient();
					long crafts = calculation.crafts().getOrDefault(bookmark, 0L);
					addNode(
							bookmark,
							ingredient,
							0,
							crafts,
							RecipeChain.multiply(output.get().amount(), crafts),
							new HashSet<>());
					leafX += 24;
				}
			}
	}

	private Object addNode(
			IBookmark bookmark,
			ITypedIngredient<?> ingredient,
			int depth,
			long crafts,
			long amount,
			Set<IBookmark> path) {
		Object id = new Object();
        if (bookmark instanceof RecipeBookmark<?, ?>) {
            recipeIcons.computeIfAbsent(bookmark, owner -> owner.getElement().createRenderOverlay());
        }
		List<Object> children = new ArrayList<>();
		if (nodes.size() + edges.size() > 4096) return id;
		if (bookmark != null && !collapsed.contains(bookmark) && path.add(bookmark)) {
			var data = bookmarks.data(bookmark).orElseThrow();
			var manager = Internal.getJivRuntime().getIngredientManager();
			if (crafts > 0)
				for (var display : data.displaySlots(bookmarks.state(bookmark), manager))
					if (display.slot().role() == RecipeIngredientRole.INPUT) {
						var input = display.ingredient();
						var key = BookmarkRecipeData.key(input, manager);
						long needed =
								display.slot().reusable()
										? display.amount()
										: RecipeChain.multiply(display.amount(), crafts);
						IBookmark provider = null;
						long yield = 0;
						for (Object candidate :
								calculation.dependencies().getOrDefault(bookmark, Set.of())) {
							IBookmark recipe = (IBookmark) candidate;
							long output = 0;
							for (var out :
									bookmarks
											.data(recipe)
											.orElseThrow()
											.displaySlots(bookmarks.state(recipe), manager))
								if (out.slot().role() == RecipeIngredientRole.OUTPUT
										&& out.slot().chance() == 1
										&& BookmarkRecipeData.key(out.ingredient(), manager)
												.equals(key))
									output = RecipeChain.add(output, out.amount());
							if (output > yield) {
								provider = recipe;
								yield = output;
							}
						}
						long batches = provider == null ? 0 : RecipeChain.ceil(needed, yield);
						children.add(addNode(provider, input, depth + 1, batches, needed, path));
					}
			path.remove(bookmark);
		}
		int x;
		if (children.isEmpty()) {
			x = leafX;
			leafX += 24;
		} else {
			Node first = nodes.get(children.getFirst()), last = nodes.get(children.getLast());
			x = first == null || last == null ? leafX : (first.x() + last.x()) / 2;
		}
		nodes.put(
				id,
				new Node(bookmark, ingredient, x, depth * 32, crafts, amount, children.isEmpty()));
		edges.put(id, children);
		return id;
	}

	private int canvasWidth() {
		return Math.max(36, width - (stats ? Math.min(200, width / 2) : 0));
	}

	@Override
	public void extractRenderState(
			GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		extractTransparentBackground(graphics);
		graphics.fill(0, 0, width, 28, 0xee202020);
		graphics.text(font, title, width - font.width(title) - 8, 9, 0xffffffff, true);
		drawToolbar(graphics, mouseX, mouseY);
		String controls =
				"[I] "
						+ Component.translatable("jiv.bookmarks.tree.inventory").getString()
						+ "  [F] "
						+ Component.translatable("jiv.bookmarks.tree.fit").getString()
						+ "  [S] "
						+ Component.translatable("jiv.bookmarks.tree.stats").getString();
		graphics.text(font, controls, 8, height - 14, 0xffaaaaaa, true);
		graphics.enableScissor(0, 29, canvasWidth(), height - 20);
		var pose = graphics.pose();
		pose.pushMatrix();
		try {
			pose.translate(panX, panY);
			pose.scale(zoom, zoom);
			for (var entry : edges.entrySet()) {
				Node from = nodes.get(entry.getKey());
				if (from == null) continue;
				for (Object child : entry.getValue()) {
					Node to = nodes.get(child);
					if (to == null) continue;
					int x1 = from.x() + 9, x2 = to.x() + 9, middle = (from.y() + 18 + to.y()) / 2;
					graphics.fill(x1, from.y() + 18, x1 + 1, middle + 1, 0xff666666);
					graphics.fill(
							Math.min(x1, x2), middle, Math.max(x1, x2) + 1, middle + 1, 0xff666666);
					graphics.fill(x2, middle, x2 + 1, to.y(), 0xff666666);
				}
			}
			for (Node node : nodes.values()) {
				int u = inventoryEnabled && node.leaf() && node.amount() > 0 ? 92 : 68;
				graphics.blit(
						net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
						TEXTURE,
						node.x(),
						node.y(),
						u,
						2,
						18,
						18,
						256,
						256);
				renderIngredient(graphics, node.ingredient(), node.x() + 1, node.y() + 1);
                var recipeIcon = recipeIcons.get(node.bookmark());
                if (recipeIcon != null) recipeIcon.draw(graphics, node.x() + 1, node.y() + 1);
				smallLabel(
						graphics,
						BookmarkGroupController.compact(node.amount()),
						node.x() + 18,
						node.y() + 12,
						true);
				if (node.crafts() > 1)
					smallLabel(
							graphics,
							"x" + BookmarkGroupController.compact(node.crafts()),
							node.x(),
							node.y(),
							false);
				if (node.bookmark() != null && collapsed.contains(node.bookmark())) {
					graphics.fill(
							node.x() + 6, node.y() + 21, node.x() + 12, node.y() + 22, 0xffffffff);
					graphics.fill(
							node.x() + 9, node.y() + 18, node.x() + 10, node.y() + 25, 0xffffffff);
				}
			}
		} finally {
			pose.popMatrix();
			graphics.disableScissor();
		}
		if (nodes.isEmpty())
			graphics.text(
					font,
					Component.translatable("jiv.bookmarks.tree.empty"),
					20,
					45,
					0xffaaaaaa,
					true);
		if (stats) drawStats(graphics);
		if (calculation.cycle())
			graphics.text(
					font, Component.translatable("jiv.bookmarks.cycle"), 8, 30, 0xffffcc55, true);
		Node hovered = nodeAt(mouseX, mouseY);
		if (hovered != null) {
			JivTooltip tooltip = new JivTooltip();
			tooltip.add(Component.literal(displayName(hovered.ingredient())));
            if (hovered.bookmark() instanceof RecipeBookmark<?, ?> recipe) tooltip.add(recipe.getRecipeCategory().getTitle());
			tooltip.add(Component.translatable("jiv.bookmarks.tree.crafts", hovered.crafts()));
			tooltip.add(Component.translatable("jiv.bookmarks.amount", hovered.amount()));
			tooltip.add(Component.translatable("jiv.bookmarks.tree.controls"));
			tooltip.draw(graphics, mouseX, mouseY);
		}
	}

	private void drawToolbar(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		int[] icons = {56, 38, collapsed.isEmpty() ? 128 : 92, 2};
		for (int i = 0; i < icons.length; i++) {
			int x = 8 + i * 20;
			boolean hovered = mouseX >= x && mouseX < x + 16 && mouseY >= 5 && mouseY < 21;
			graphics.fill(x, 5, x + 16, 21, hovered ? 0xff888888 : 0xff555555);
			graphics.fill(x, 5, x + 16, 6, 0xffaaaaaa);
			graphics.fill(x, 5, x + 1, 21, 0xffaaaaaa);
			graphics.fill(x, 20, x + 16, 21, 0xff222222);
			graphics.fill(x + 15, 5, x + 16, 21, 0xff222222);
			graphics.blit(
					net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
					TEXTURE,
					x + 4,
					9,
					icons[i],
					66,
					8,
					8,
					16,
					16,
					256,
					256);
		}
	}

	private static void smallLabel(
			GuiGraphicsExtractor graphics, String text, int x, int y, boolean right) {
		var font = Minecraft.getInstance().font;
		var pose = graphics.pose();
		pose.pushMatrix();
		try {
			pose.translate(x, y);
			pose.scale(0.6f, 0.6f);
			graphics.text(font, text, right ? -font.width(text) : 0, 0, 0xffffffff, true);
		} finally {
			pose.popMatrix();
		}
	}

	private void drawStats(GuiGraphicsExtractor graphics) {
		int x = canvasWidth() + 8, y = 40 - statsScroll;
		graphics.fill(canvasWidth(), 29, width, height - 20, 0xee202020);
		graphics.enableScissor(canvasWidth(), 29, width, height - 20);
		try {
			var materials = bookmarks.materials(group);
			var sections =
					List.of(calculation.inputs(), calculation.outputs(), calculation.remainders());
			var titles =
					List.of(
							"jiv.bookmarks.tree.inputs",
							"jiv.bookmarks.tree.outputs",
							"jiv.bookmarks.tree.remainders");
			for (int section = 0; section < sections.size(); section++) {
				graphics.text(
						font, Component.translatable(titles.get(section)), x, y, 0xff66ccff, true);
				y += 16;
				for (var entry : sections.get(section).entrySet()) {
					var ingredient = materials.get(entry.getKey());
					if (ingredient == null) continue;
					renderIngredient(graphics, ingredient, x, y);
					graphics.text(
							font,
							font.plainSubstrByWidth(displayName(ingredient), width - x - 24),
							x + 20,
							y,
							0xffffffff,
							true);
					graphics.text(
							font,
							Long.toString(entry.getValue()),
							x + 20,
							y + 10,
							0xffaaaaaa,
							true);
					y += 25;
				}
				y += 8;
			}
			if (calculation.uncertain())
				graphics.text(
						font,
						Component.translatable("jiv.bookmarks.uncertain"),
						x,
						y,
						0xffffcc55,
						true);
		} finally {
			graphics.disableScissor();
		}
	}

	private Node nodeAt(double x, double y) {
		if (x >= canvasWidth() || y < 29 || y >= height - 20) return null;
		double localX = (x - panX) / zoom, localY = (y - panY) / zoom;
		return nodes.values().stream()
				.filter(
						n ->
								localX >= n.x()
										&& localX < n.x() + 18
										&& localY >= n.y()
										&& localY < n.y() + 18)
				.findFirst()
				.orElse(null);
	}

	private static <T> String displayName(ITypedIngredient<T> ingredient) {
		return Internal.getJivRuntime()
				.getIngredientManager()
				.getIngredientHelper(ingredient.getType())
				.getDisplayName(ingredient.getIngredient());
	}

	private static <T> void renderIngredient(
			GuiGraphicsExtractor graphics, ITypedIngredient<T> ingredient, int x, int y) {
		var manager = Internal.getJivRuntime().getIngredientManager();
		var normalized = manager.normalizeTypedIngredient(ingredient);
		manager.getIngredientRenderer(ingredient.getType())
				.render(graphics, normalized.getIngredient(), x, y);
	}

	@Override
	public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
		if (x < canvasWidth()) {
			panX += dx;
			panY += dy;
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(double x, double y, double dx, double dy) {
		if (x >= canvasWidth()) {
			statsScroll = Math.max(0, statsScroll - (int) (dy * 20));
			return true;
		}
		float next = Math.clamp(zoom * (float) Math.pow(1.15, dy), 0.2f, 3f);
		panX = (float) (x - (x - panX) * next / zoom);
		panY = (float) (y - (y - panY) * next / zoom);
		zoom = next;
		return true;
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		if (y >= 5 && y < 21 && x >= 8 && x < 88 && button == 0) {
			int action = (int) (x - 8) / 20;
			if (action == 0) keyPressed(GLFW.GLFW_KEY_I, 0, 0);
			else if (action == 1) keyPressed(GLFW.GLFW_KEY_F, 0, 0);
			else if (action == 2) {
				if (collapsed.isEmpty()) collapsed.addAll(bookmarks.getGroupBookmarks(group));
				else collapsed.clear();
				rebuild();
			} else keyPressed(GLFW.GLFW_KEY_S, 0, 0);
			return true;
		}
		Node node = nodeAt(x, y);
		if (node == null) return true;
		if (button == 0 && node.bookmark() != null) {
			if (!collapsed.add(node.bookmark())) collapsed.remove(node.bookmark());
			rebuild();
			return true;
		}
		if (button == 1 && node.bookmark() instanceof RecipeBookmark<?, ?> recipe) {
			showRecipe(recipe);
			return true;
		}
		return true;
	}

	@Override
	public boolean keyPressed(int key, int scan, int modifiers) {
		if (key == GLFW.GLFW_KEY_C && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
			Node node = nodeAt(eakerzt.jiv.common.input.MouseUtil.getX(), eakerzt.jiv.common.input.MouseUtil.getY());
			if (node != null) {
				Minecraft.getInstance().keyboardHandler.setClipboard(displayName(node.ingredient()));
				return true;
			}
		}
		if (key == GLFW.GLFW_KEY_I) {
			inventoryEnabled = !inventoryEnabled;
			inventory.clear();
			if (inventoryEnabled && Minecraft.getInstance().player != null) {
				var manager = Internal.getJivRuntime().getIngredientManager();
				var source = Minecraft.getInstance().player.getInventory();
				for (int i = 0; i < source.getContainerSize(); i++) {
					var stack = source.getItem(i);
					if (!stack.isEmpty())
						manager.createTypedIngredient(VanillaTypes.ITEM_STACK, stack, false)
								.ifPresent(
										ingredient ->
												inventory.merge(
														BookmarkRecipeData.key(ingredient, manager),
														(long) stack.getCount(),
														RecipeChain::add));
				}
			}
			rebuild();
			return true;
		}
		if (key == GLFW.GLFW_KEY_S) {
			stats = !stats;
			return true;
		}
		if (key == GLFW.GLFW_KEY_F) {
			int right = nodes.values().stream().mapToInt(n -> n.x() + 18).max().orElse(104),
					bottom = nodes.values().stream().mapToInt(n -> n.y() + 18).max().orElse(32);
			zoom =
					Math.min(
							1,
							Math.min(
									(float) Math.max(1, canvasWidth() - 48) / right,
									(float) Math.max(1, height - 80) / bottom));
			panX = 24;
			panY = 42;
			return true;
		}
		return super.keyPressed(key, scan, modifiers);
	}

	private static <R> void showRecipe(RecipeBookmark<R, ?> recipe) {
		Internal.getJivRuntime()
				.getRecipesGui()
				.showRecipes(recipe.getRecipeCategory(), List.of(recipe.getRecipe()), List.of());
	}

	@Override
	public void onClose() {
		Minecraft.getInstance().setScreen(parent);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
