package eakerzt.jiv.gui.overlay.bookmarks;

import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.gui.drawable.IDrawable;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.util.SafeIngredientUtil;
import eakerzt.jiv.gui.bookmarks.BookmarkList;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Full icon grids with quantities; the saved ingredients remain unchanged. */
public final class BookmarkGroupTooltipComponent implements ClientTooltipComponent, TooltipComponent {
	private static final int CELL_SIZE = 18;
	private static final int MAX_COLUMNS = 11;
	private record Entry(ITypedIngredient<?> ingredient, long amount, int color, @Nullable IDrawable icon) {}
	private record Section(Component title, int color, List<Entry> entries) {}
	private final List<Section> sections = new ArrayList<>();
	private final IIngredientManager manager;
	private final int width;
	private final int height;
	private final int columns;

	public BookmarkGroupTooltipComponent(BookmarkList.GroupSummary summary, boolean inventory,
			IIngredientManager manager, Font font, int screenWidth) {
		this.manager = manager;
		this.columns = Math.max(1, Math.min(MAX_COLUMNS, (screenWidth - 32) / CELL_SIZE));
		var outputs = entries(summary.amounts().calculation().outputs(), summary.materials(), 0xffffffff);
		outputs.addAll(entries(summary.amounts().calculation().remainders(), summary.materials(), 0xffffff55));
		outputs.addAll(entries(summary.containers(), summary.materials(), 0xffffff55));
		add("outputs", 0xff55ffff, outputs);
		if (inventory) {
			var needed = summary.craftingRequirements().stream()
					.map(requirement -> new Entry(manager.normalizeTypedIngredient(requirement.ingredient()),
							requirement.amount(), 0xffffffff, requirement.icon()))
					.sorted(Comparator.comparing(entry -> name(entry.ingredient()))).toList();
			add("crafting_needed", 0xff5555ff, needed);
		}
		var materials = entries(inventory ? summary.amounts().missing() : summary.amounts().required(),
				summary.materials(), inventory ? 0xffff5555 : 0xffffffff);
		if (inventory) materials.addAll(entries(summary.amounts().covered(), summary.materials(), 0xff55ff55));
		boolean complete = summary.amounts().missing().isEmpty();
		String requiredTitle = inventory ? (complete ? "required_complete" : "required_incomplete") : "required";
		int requiredColor = inventory ? (complete ? 0xff55ff55 : 0xffff5555) : 0xffffff55;
		if (inventory && materials.isEmpty() && !summary.amounts().required().isEmpty())
			sections.add(new Section(Component.translatable("jiv.bookmarks.summary." + requiredTitle), requiredColor, List.of()));
		else add(requiredTitle, requiredColor, materials);
		int totalHeight = 0;
		int maximumWidth = 0;
		for (var section : sections) {
			int count = section.entries().size();
			int rows = (count + columns - 1) / columns;
			totalHeight += font.lineHeight + 4 + rows * CELL_SIZE;
			maximumWidth = Math.max(maximumWidth, Math.max(font.width(section.title()), Math.min(count, columns) * CELL_SIZE));
		}
		this.width = maximumWidth;
		this.height = totalHeight;
	}

	private void add(String title, int color, List<Entry> entries) {
		if (!entries.isEmpty()) sections.add(new Section(
				Component.translatable("jiv.bookmarks.summary." + title), color, List.copyOf(entries)));
	}

	private List<Entry> entries(Map<Object, Long> amounts, Map<Object, ITypedIngredient<?>> ingredients, int color) {
		var entries = new ArrayList<Entry>();
		amounts.forEach((key, amount) -> {
			var ingredient = ingredients.get(key);
			if (ingredient != null && amount > 0)
				entries.add(new Entry(manager.normalizeTypedIngredient(ingredient), amount, color, null));
		});
		entries.sort(Comparator.comparing(entry -> name(entry.ingredient())));
		return entries;
	}

	private <T> String name(ITypedIngredient<T> ingredient) {
		return manager.getIngredientHelper(ingredient.getType()).getDisplayName(ingredient.getIngredient());
	}

	@Override public int getWidth(Font font) { return width; }
	@Override public int getHeight(Font font) { return height; }

	@Override
	public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
		for (var section : sections) {
			graphics.text(font, section.title(), x, y, section.color(), false);
			y += font.lineHeight + 2;
			for (int i = 0; i < section.entries().size(); i++) {
				var entry = section.entries().get(i);
				int cellX = x + (i % columns) * CELL_SIZE;
				int cellY = y + (i / columns) * CELL_SIZE;
				render(graphics, entry.ingredient(), cellX + 1, cellY + 1);
				if (entry.icon() != null) entry.icon().draw(graphics, cellX + 1, cellY + 1);
				String amount = BookmarkGroupController.compact(entry.amount());
				if (entry.ingredient().getType().equals(eakerzt.jiv.common.platform.Services.PLATFORM.getFluidHelper().getFluidIngredientType())) amount += " mB";
				drawAmount(graphics, font, amount, cellX, cellY, entry.color());
			}
			y += ((section.entries().size() + columns - 1) / columns) * CELL_SIZE + 2;
		}
	}

	private static void drawAmount(GuiGraphicsExtractor graphics, Font font, String amount, int x, int y, int color) {
		float scale = Math.min(0.75f, 16f / Math.max(1, font.width(amount)));
		var pose = graphics.pose();
		pose.pushMatrix();
		try {
			pose.translate(x + 17 - font.width(amount) * scale, y + 17 - font.lineHeight * scale);
			pose.scale(scale, scale);
			graphics.text(font, amount, 0, 0, color, true);
		} finally { pose.popMatrix(); }
	}

	private <T> void render(GuiGraphicsExtractor graphics, ITypedIngredient<T> ingredient, int x, int y) {
		var renderer = manager.getIngredientRenderer(ingredient.getType());
		var pose = graphics.pose();
		pose.pushMatrix();
		try {
			pose.translate(x, y);
			pose.scale(16f / Math.max(1, renderer.getWidth()), 16f / Math.max(1, renderer.getHeight()));
			SafeIngredientUtil.render(graphics, renderer, ingredient, 0, 0);
		} finally { pose.popMatrix(); }
	}
}
