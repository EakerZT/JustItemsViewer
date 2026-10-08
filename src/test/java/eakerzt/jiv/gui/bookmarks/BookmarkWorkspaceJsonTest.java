package eakerzt.jiv.gui.bookmarks;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import eakerzt.jiv.gui.overlay.elements.IElement;

import org.junit.jupiter.api.Test;

import java.util.List;

class BookmarkWorkspaceJsonTest {
	private static final Codec<IBookmark> CODEC =
			Codec.STRING.<IBookmark>xmap(FakeBookmark::new, b -> ((FakeBookmark) b).id);

	private record FakeBookmark(String id) implements IBookmark {
		@Override
		public BookmarkType getType() {
			return BookmarkType.INGREDIENT;
		}

		@Override
		public IElement<?> getElement() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isVisible() {
			return true;
		}

		@Override
		public void setVisible(boolean visible) {}
	}

	@Test
	void roundTripsIndependentGroupsAndNamespaces() {
		BookmarkPage first = new BookmarkPage(), second = new BookmarkPage();
		IBookmark a = new FakeBookmark("iron"), b = new FakeBookmark("iron");
		first.bookmarks.addAll(List.of(a, b));
		first.groups.put(1, new BookmarkGroup());
		first.groups.get(1).linked = true;
		first.groups.get(1).todo = true;
		first.groups.get(1).collapsed = true;
		first.state(a).multiplier = 4;
		first.state(b).multiplier = 12;
		first.state(b).group = 1;
		first.state(b).choices.put(2, 3);
		first.state(b).inputOrder.addAll(List.of(4, 1, 2));
		first.state(b).collapsed = true;
		second.bookmarks.add(new FakeBookmark("copper"));
		var workspace =
				BookmarkWorkspaceJson.decode(
						BookmarkWorkspaceJson.encode(
								List.of(first, second), 1, CODEC, JsonOps.INSTANCE),
						CODEC,
						JsonOps.INSTANCE);
		assertEquals(1, workspace.namespace());
		assertEquals(2, workspace.pages().size());
		var page = workspace.pages().getFirst();
		assertEquals(2, page.bookmarks.size());
		assertEquals(4, page.state(page.bookmarks.get(0)).multiplier);
		assertEquals(12, page.state(page.bookmarks.get(1)).multiplier);
		assertEquals(MapHolder.CHOICES, page.state(page.bookmarks.get(1)).choices);
		assertEquals(List.of(4, 1, 2), page.state(page.bookmarks.get(1)).inputOrder);
		assertTrue(page.state(page.bookmarks.get(1)).collapsed);
		assertTrue(page.groups.get(1).linked);
		assertTrue(page.groups.get(1).todo);
		assertTrue(page.groups.get(1).collapsed);
	}

	private static class MapHolder {
		static final java.util.Map<Integer, Integer> CHOICES = java.util.Map.of(2, 3);
	}

	@Test
	void migratesVersionThreeIntoDefaultGroupWithoutChangingOrder() {
		var workspace =
				BookmarkWorkspaceJson.decode(
						JsonParser.parseString("[{\"version\":3},\"iron\",\"copper\"]"),
						CODEC,
						JsonOps.INSTANCE);
		assertEquals(
				List.of(new FakeBookmark("iron"), new FakeBookmark("copper")),
				workspace.pages().getFirst().bookmarks);
		assertEquals(
				0,
				workspace
						.pages()
						.getFirst()
						.state(workspace.pages().getFirst().bookmarks.getFirst())
						.group);
	}

	@Test
	void unresolvedRecipeSurvivesSaveWhileOtherEntriesLoad() {
		JsonObject json =
				BookmarkWorkspaceJson.encode(
						List.of(new BookmarkPage()), 0, CODEC, JsonOps.INSTANCE);
		JsonArray bookmarks =
				json.getAsJsonArray("namespaces")
						.get(0)
						.getAsJsonObject()
						.getAsJsonArray("bookmarks");
		JsonObject bad = new JsonObject();
		bad.add("bookmark", new JsonObject());
		bad.addProperty("multiplier", 32);
		bookmarks.add(bad);
		JsonObject good = new JsonObject();
		good.addProperty("bookmark", "iron");
		bookmarks.add(good);
		var workspace = BookmarkWorkspaceJson.decode(json, CODEC, JsonOps.INSTANCE);
		assertEquals(1, workspace.pages().getFirst().bookmarks.size());
		assertEquals(1, workspace.pages().getFirst().unresolved.size());
		var saved =
				BookmarkWorkspaceJson.encode(
						workspace.pages(), workspace.namespace(), CODEC, JsonOps.INSTANCE);
		assertEquals(
				bad,
				saved.getAsJsonArray("namespaces")
						.get(0)
						.getAsJsonObject()
						.getAsJsonArray("bookmarks")
						.get(1));
	}

	@Test
	void rejectsUnknownVersionsInsteadOfSilentlyErasingData() {
		assertThrows(
				JsonParseException.class,
				() ->
						BookmarkWorkspaceJson.decode(
								JsonParser.parseString("{\"version\":99}"),
								CODEC,
								JsonOps.INSTANCE));
	}

	@Test
	void snapshotIsIndependentOfSubsequentEdits() {
		BookmarkPage page = new BookmarkPage();
		var bookmark = new FakeBookmark("iron");
		page.bookmarks.add(bookmark);
		page.state(bookmark).multiplier = 2;
		var snapshot = BookmarkWorkspaceJson.encode(List.of(page), 0, CODEC, JsonOps.INSTANCE);
		page.state(bookmark).multiplier = 99;
		var decoded = BookmarkWorkspaceJson.decode(snapshot, CODEC, JsonOps.INSTANCE);
		var savedPage = decoded.pages().getFirst();
		assertEquals(2, savedPage.state(savedPage.bookmarks.getFirst()).multiplier);
	}

	@Test
	void versionFourLoadsWithoutAnInputOrderAndUpgradesOnSave() {
		var json =
				BookmarkWorkspaceJson.encode(
						List.of(new BookmarkPage()), 0, CODEC, JsonOps.INSTANCE);
		json.addProperty("version", 4);
		var entries =
				json.getAsJsonArray("namespaces")
						.get(0)
						.getAsJsonObject()
						.getAsJsonArray("bookmarks");
		var entry = new JsonObject();
		entry.addProperty("bookmark", "iron");
		entries.add(entry);
		var loaded = BookmarkWorkspaceJson.decode(json, CODEC, JsonOps.INSTANCE);
		assertEquals(1, loaded.pages().getFirst().bookmarks.size());
		assertTrue(
				loaded.pages()
						.getFirst()
						.state(loaded.pages().getFirst().bookmarks.getFirst())
						.inputOrder
						.isEmpty());
		assertEquals(
				5,
				BookmarkWorkspaceJson.encode(loaded.pages(), 0, CODEC, JsonOps.INSTANCE)
						.get("version")
						.getAsInt());
	}
}
