package eakerzt.jiv.gui.bookmarks;

import com.mojang.serialization.Codec;
import eakerzt.jiv.api.helpers.*;
import eakerzt.jiv.api.recipe.*;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.gui.config.IBookmarkConfig;
import eakerzt.jiv.gui.overlay.elements.IElement;
import eakerzt.jiv.test.lib.TestClientConfig;
import net.minecraft.core.RegistryAccess;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BookmarkListTest {
	@Test void topNavigationSwitchesWorkspacesAndDisplaysBothCounters() {
		BookmarkList list = list();
		list.toggleInGroup(new FakeBookmark("iron"), 0, 4);
		int[] materialPage = {2}, resets = {0};
		var pages = new eakerzt.jiv.gui.input.IPaged() {
			public boolean nextPage() { materialPage[0]++; return true; }
			public boolean previousPage() { materialPage[0]--; return true; }
			public boolean hasNext() { return true; }
			public boolean hasPrevious() { return true; }
			public int getPageCount() { return list.page().bookmarks.isEmpty() ? 1 : 5; }
			public int getPageNumber() { return materialPage[0]; }
		};
		var navigation = new BookmarkWorkspaceNavigation(list, pages, () -> { materialPage[0] = 0; resets[0]++; });
		assertEquals("1/1 3/5", navigation.label());
		assertTrue(navigation.hasNext());
		assertFalse(navigation.hasPrevious());
		assertTrue(navigation.nextPage());
		assertEquals("2/2", navigation.label());
		assertTrue(navigation.previousPage());
		assertEquals("1/2 1/5", navigation.label());
		assertEquals(2, resets[0]);
		assertEquals(4, list.state(list.page().bookmarks.getFirst()).multiplier);
	}
    @Test void dragPreviewMovesImmediatelyAndCancelRestoresState() {
        BookmarkList list = list();
        IBookmark a = new FakeBookmark("a"), b = new FakeBookmark("b"), c = new FakeBookmark("c");
        list.setWorkspace(List.of(new BookmarkPage()), 0);
        list.page().bookmarks.addAll(List.of(a,b,c));
        list.state(a).group = 0; list.state(b).group = 1; list.state(c).group = 1;
        list.state(a).inputOrder.addAll(List.of(2,0,1));
        list.beginDrag(a, -1);
        list.moveBookmark(a, 2);
        assertEquals(List.of(b,c,a), list.page().bookmarks);
        assertEquals(1, list.state(a).group);
        list.state(a).inputOrder.clear();
        list.finishDrag(false);
        assertEquals(List.of(a,b,c), list.page().bookmarks);
        assertEquals(0, list.state(a).group);
        assertEquals(List.of(2,0,1), list.state(a).inputOrder);
    }

    @Test void dragCommitRetainsPreviewAndDuplicateDestinationIsRejected() {
        BookmarkList list = list();
        IBookmark a = new FakeBookmark("a"), b = new FakeBookmark("b"), duplicate = new FakeBookmark("a");
        list.page().bookmarks.addAll(List.of(a,b,duplicate));
        list.state(a).group = 0; list.state(b).group = 1; list.state(duplicate).group = 1;
        list.beginDrag(a, -1);
        list.moveBookmark(a, 1);
        assertEquals(List.of(a,b,duplicate), list.page().bookmarks);
        list.moveBookmark(b, 0);
        list.finishDrag(true);
        assertEquals(List.of(b,a,duplicate), list.page().bookmarks);
        assertEquals(0, list.state(b).group);
    }

    @Test void previewDoesNotSaveAndSuccessfulDropSavesOnce() {
        int[] saves = {0};
        IBookmarkConfig config = new IBookmarkConfig() {
            public void saveBookmarks(IRecipeManager recipes, IFocusFactory focuses, IGuiHelper gui, IIngredientManager ingredients, RegistryAccess registry, ICodecHelper codec, List<IBookmark> bookmarks, Codec<IBookmark> bookmarkCodec) { saves[0]++; }
            public void loadBookmarks(IRecipeManager recipes, IFocusFactory focuses, IGuiHelper gui, IIngredientManager ingredients, RegistryAccess registry, BookmarkList list, ICodecHelper codec, Codec<IBookmark> bookmarkCodec) {}
        };
        BookmarkList list = new BookmarkList(null,null,null,null,config,new TestClientConfig(false),null,null,null,null);
        IBookmark a = new FakeBookmark("a"), b = new FakeBookmark("b");
        list.page().bookmarks.addAll(List.of(a,b));
        list.beginDrag(a, -1);
        list.moveBookmark(a, 1);
        assertEquals(0, saves[0]);
        list.finishDrag(false);
        assertEquals(0, saves[0]);
        list.beginDrag(a, -1);
        list.moveBookmark(a, 1);
        list.finishDrag(true);
        assertEquals(1, saves[0]);
        list.finishDrag(false);
        assertEquals(List.of(b,a), list.page().bookmarks);
    }

    @Test void subgroupSortingWorksInBothDirectionsWithoutMerging() {
        BookmarkList list = list();
        IBookmark a = new FakeBookmark("a"), b = new FakeBookmark("b"), c = new FakeBookmark("c"), d = new FakeBookmark("d");
        list.page().bookmarks.addAll(List.of(a,b,c,d));
        list.page().groups.put(1, new BookmarkGroup());
        list.page().groups.put(2, new BookmarkGroup());
        list.state(a).group = list.state(b).group = 1;
        list.state(c).group = list.state(d).group = 2;
        list.beginGroupDrag(1);
        assertTrue(list.moveGroupRelative(1,c,true));
        assertEquals(List.of(c,d,a,b), list.page().bookmarks);
        list.finishDrag(true);
        list.beginGroupDrag(1);
        assertTrue(list.moveGroupRelative(1,d,false));
        assertEquals(List.of(a,b,c,d), list.page().bookmarks);
        list.finishDrag(true);
        assertEquals(1,list.state(a).group);
        assertEquals(1,list.state(b).group);
        assertEquals(2,list.state(c).group);
        assertEquals(2,list.state(d).group);
    }

    private record FakeBookmark(String id) implements IBookmark {
        @Override public BookmarkType getType(){return BookmarkType.INGREDIENT;}
        @Override public IElement<?> getElement(){throw new UnsupportedOperationException();}
        @Override public boolean isVisible(){return true;}
        @Override public void setVisible(boolean visible){}
    }
    private static final IBookmarkConfig CONFIG=new IBookmarkConfig() {
        public void saveBookmarks(IRecipeManager recipeManager,IFocusFactory focuses,IGuiHelper gui,IIngredientManager ingredients,RegistryAccess registry,ICodecHelper codec,List<IBookmark> bookmarks,Codec<IBookmark> bookmarkCodec) {}
        public void loadBookmarks(IRecipeManager recipeManager,IFocusFactory focuses,IGuiHelper gui,IIngredientManager ingredients,RegistryAccess registry,BookmarkList list,ICodecHelper codec,Codec<IBookmark> bookmarkCodec) {}
    };
    private static BookmarkList list() {return new BookmarkList(null,null,null,null,CONFIG,new TestClientConfig(false),null,null,null,null);}
    @Test void repeatedBookmarkTogglesOffAndBackOn() {
        BookmarkList list=list();list.toggleInGroup(new FakeBookmark("iron"),0,0);assertTrue(list.contains(new FakeBookmark("iron")));
        list.toggleInGroup(new FakeBookmark("iron"),0,0);assertTrue(list.page().bookmarks.isEmpty());
        list.toggleInGroup(new FakeBookmark("iron"),0,64);assertEquals(64,list.state(list.page().bookmarks.getFirst()).multiplier);
    }
    @Test void removingOneGroupDoesNotRemoveEqualBookmarkInAnotherGroup() {
        BookmarkList list=list();IBookmark defaultBookmark=new FakeBookmark("iron"),grouped=new FakeBookmark("iron");
        list.toggleInGroup(defaultBookmark,0,4);list.toggleInGroup(grouped,1,8);assertEquals(2,list.page().bookmarks.size());
        list.remove(grouped);assertEquals(1,list.page().bookmarks.size());assertSame(defaultBookmark,list.page().bookmarks.getFirst());assertEquals(4,list.state(defaultBookmark).multiplier);
    }
    @Test void mergingGroupsCombinesAmountsAndDeduplicatesOnlyWithinTarget() {
        BookmarkList list=list();IBookmark first=new FakeBookmark("iron"),second=new FakeBookmark("iron");
        list.toggleInGroup(first,0,4);list.toggleInGroup(second,1,8);list.groupBookmarks(List.of(second),0);
        assertEquals(1,list.page().bookmarks.size());assertEquals(12,list.state(first).multiplier);
    }
    @Test void namespacesKeepIndependentListsAndReturnToPreviousContents() {
        BookmarkList list=list();IBookmark first=new FakeBookmark("iron");list.toggleInGroup(first,0,4);
        list.changeNamespace(1);assertEquals(1,list.getNamespace());assertTrue(list.page().bookmarks.isEmpty());assertFalse(list.isEmpty());
        list.toggleInGroup(new FakeBookmark("copper"),0,8);list.changeNamespace(-1);assertSame(first,list.page().bookmarks.getFirst());assertEquals(2,list.getPages().size());
    }
    @Test void quantityDecrementsClampAtZeroAndIncrementSaturates() {
        BookmarkList list=list();IBookmark bookmark=new FakeBookmark("iron");list.toggleInGroup(bookmark,0,1);
        list.changeQuantity(bookmark,-64);assertEquals(0,list.state(bookmark).multiplier);
        list.changeQuantity(bookmark,Long.MAX_VALUE);list.changeQuantity(bookmark,64);assertEquals(Long.MAX_VALUE,list.state(bookmark).multiplier);
    }
}
