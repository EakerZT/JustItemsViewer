package eakerzt.jiv.gui.bookmarks;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;

import java.util.ArrayList;
import java.util.List;

/** Versioned workspace; unresolved entries survive missing mods or recipes. */
public final class BookmarkWorkspaceJson {
	private BookmarkWorkspaceJson() {}

	public record Workspace(List<BookmarkPage> pages, int namespace) {}

	public static JsonObject encode(
			List<BookmarkPage> pages,
			int namespace,
			Codec<IBookmark> codec,
			DynamicOps<JsonElement> ops) {
		JsonObject root = new JsonObject();
		root.addProperty("version", 5);
		root.addProperty("namespace", namespace);
		JsonArray namespaces = new JsonArray();
		root.add("namespaces", namespaces);
		for (BookmarkPage page : pages) {
			JsonObject target = new JsonObject();
			namespaces.add(target);
			JsonArray groups = new JsonArray();
			target.add("groups", groups);
			page.groups.forEach(
					(id, group) -> {
						JsonObject json = new JsonObject();
						json.addProperty("id", id);
						json.addProperty("todo", group.todo);
						json.addProperty("linked", group.linked);
						json.addProperty("collapsed", group.collapsed);
						groups.add(json);
					});
			JsonArray entries = new JsonArray();
			target.add("bookmarks", entries);
			for (IBookmark bookmark : page.bookmarks) {
				JsonObject entry = new JsonObject();
				var state = page.state(bookmark);
				entry.add("bookmark", codec.encodeStart(ops, bookmark).getOrThrow());
				entry.addProperty("group", state.group);
				entry.addProperty("multiplier", state.multiplier);
				entry.addProperty("collapsed", state.collapsed);
				JsonObject choices = new JsonObject();
				state.choices.forEach(
						(slot, choice) -> choices.addProperty(slot.toString(), choice));
				entry.add("choices", choices);
				JsonArray inputOrder = new JsonArray();
				state.inputOrder.forEach(inputOrder::add);
				entry.add("inputOrder", inputOrder);
				entries.add(entry);
			}
			page.unresolved.forEach(entry -> entries.add(entry.deepCopy()));
		}
		return root;
	}

	public static Workspace decode(
			JsonElement root, Codec<IBookmark> codec, DynamicOps<JsonElement> ops) {
		List<BookmarkPage> pages = new ArrayList<>();
		int selected = 0;
		if (root.isJsonArray()) {
			JsonArray array = root.getAsJsonArray();
			if (array.isEmpty() || array.get(0).getAsJsonObject().get("version").getAsInt() != 3)
				throw new JsonParseException("Unsupported legacy bookmark version");
			BookmarkPage page = new BookmarkPage();
			pages.add(page);
			for (int i = 1; i < array.size(); i++) {
				JsonObject entry = new JsonObject();
				entry.add("bookmark", array.get(i));
				readEntry(page, entry, codec, ops);
			}
		} else {
			JsonObject json = root.getAsJsonObject();
			if (json.get("version").getAsInt() != 4 && json.get("version").getAsInt() != 5)
				throw new JsonParseException("Unsupported bookmark workspace version");
			selected = json.get("namespace").getAsInt();
			for (JsonElement namespace : json.getAsJsonArray("namespaces")) {
				BookmarkPage page = new BookmarkPage();
				pages.add(page);
				JsonObject data = namespace.getAsJsonObject();
				for (JsonElement element : data.getAsJsonArray("groups")) {
					JsonObject groupJson = element.getAsJsonObject();
					int id = groupJson.get("id").getAsInt();
					if (id < 0) throw new JsonParseException("Negative group id");
					BookmarkGroup group = new BookmarkGroup();
					group.todo = groupJson.get("todo").getAsBoolean();
					group.linked = groupJson.get("linked").getAsBoolean();
					group.collapsed = groupJson.get("collapsed").getAsBoolean();
					page.groups.put(id, group);
				}
				for (JsonElement entry : data.getAsJsonArray("bookmarks"))
					readEntry(page, entry, codec, ops);
			}
		}
		if (pages.isEmpty()) pages.add(new BookmarkPage());
		return new Workspace(List.copyOf(pages), Math.clamp(selected, 0, pages.size() - 1));
	}

	private static void readEntry(
			BookmarkPage page,
			JsonElement element,
			Codec<IBookmark> codec,
			DynamicOps<JsonElement> ops) {
		try {
			JsonObject entry = element.getAsJsonObject();
			IBookmark bookmark = codec.parse(ops, entry.get("bookmark")).getOrThrow();
			BookmarkState state = new BookmarkState();
			state.group = entry.has("group") ? entry.get("group").getAsInt() : 0;
			state.multiplier = entry.has("multiplier") ? entry.get("multiplier").getAsLong() : 0;
			if (state.group < 0 || state.multiplier < 0)
				throw new JsonParseException("Invalid bookmark state");
			state.collapsed = entry.has("collapsed") && entry.get("collapsed").getAsBoolean();
			if (entry.has("choices"))
				entry.getAsJsonObject("choices")
						.entrySet()
						.forEach(
								e -> {
									int slot = Integer.parseInt(e.getKey()),
											choice = e.getValue().getAsInt();
									if (slot < 0 || choice < 0)
										throw new JsonParseException("Invalid slot choice");
									state.choices.put(slot, choice);
								});
			if (entry.has("inputOrder")) {
				for (JsonElement input : entry.getAsJsonArray("inputOrder")) {
					int index = input.getAsInt();
					if (index < 0 || state.inputOrder.contains(index))
						throw new JsonParseException("Invalid input order");
					state.inputOrder.add(index);
				}
			}
			if (page.bookmarks.stream()
					.anyMatch(b -> page.state(b).group == state.group && b.equals(bookmark)))
				return;
			page.groups.computeIfAbsent(state.group, ignored -> new BookmarkGroup());
			page.bookmarks.add(bookmark);
			page.states.put(bookmark, state);
		} catch (RuntimeException error) {
			page.unresolved.add(element.deepCopy());
		}
	}
}
