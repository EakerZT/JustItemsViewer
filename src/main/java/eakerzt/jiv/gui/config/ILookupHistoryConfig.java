package eakerzt.jiv.gui.config;

import com.mojang.serialization.Codec;
import eakerzt.jiv.api.helpers.ICodecHelper;
import eakerzt.jiv.api.recipe.IRecipeManager;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.gui.bookmarks.IBookmark;
import net.minecraft.core.RegistryAccess;

import java.util.List;

public interface ILookupHistoryConfig {
	void save(IRecipeManager recipeManager, IIngredientManager ingredientManager, RegistryAccess registryAccess, ICodecHelper codecHelper, List<IBookmark> bookmarks, Codec<IBookmark> bookmarkCodec);

	List<IBookmark> load(IRecipeManager recipeManager, IIngredientManager ingredientManager, RegistryAccess registryAccess, ICodecHelper codecHelper, Codec<IBookmark> bookmarkCodec);
}
