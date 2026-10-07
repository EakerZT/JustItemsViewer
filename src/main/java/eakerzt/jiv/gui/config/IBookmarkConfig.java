package eakerzt.jiv.gui.config;

import com.mojang.serialization.Codec;
import eakerzt.jiv.api.helpers.ICodecHelper;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.api.recipe.IFocusFactory;
import eakerzt.jiv.api.recipe.IRecipeManager;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.gui.bookmarks.BookmarkList;
import eakerzt.jiv.gui.bookmarks.IBookmark;
import net.minecraft.core.RegistryAccess;

import java.util.List;

public interface IBookmarkConfig {
	void saveBookmarks(IRecipeManager recipeManager, IFocusFactory focusFactory, IGuiHelper guiHelper, IIngredientManager ingredientManager, RegistryAccess registryAccess, ICodecHelper codecHelper, List<IBookmark> bookmarks, Codec<IBookmark> bookmarkCodec);

	void loadBookmarks(IRecipeManager recipeManager, IFocusFactory focusFactory, IGuiHelper guiHelper, IIngredientManager ingredientManager, RegistryAccess registryAccess, BookmarkList bookmarkList, ICodecHelper codecHelper, Codec<IBookmark> bookmarkCodec);
}
