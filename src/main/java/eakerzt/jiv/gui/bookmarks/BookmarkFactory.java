package eakerzt.jiv.gui.bookmarks;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import eakerzt.jiv.api.helpers.ICodecHelper;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.runtime.IIngredientManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;

public class BookmarkFactory {
	private final Codec<ITypedIngredient<?>> typedIngredientCodec;
	private final RegistryOps<JsonElement> serializationContext;
	private final IIngredientManager ingredientManager;

	public BookmarkFactory(ICodecHelper codecHelper, RegistryAccess registryAccess, IIngredientManager ingredientManager) {
		this.typedIngredientCodec = codecHelper.getTypedIngredientCodec().codec();
		this.serializationContext = registryAccess.createSerializationContext(JsonOps.INSTANCE);
		this.ingredientManager = ingredientManager;
	}

	public <T> IngredientBookmark<T> create(ITypedIngredient<T> typedIngredient) {
		typedIngredient = ingredientManager.normalizeTypedIngredient(typedIngredient);

		Object bookmarkUid = typedIngredientCodec.encodeStart(serializationContext, typedIngredient)
			.result()
			.orElse(JsonNull.INSTANCE)
			.toString(); // JsonElement is slow for hash/equals, so use the string representation as the uid

		return new IngredientBookmark<>(typedIngredient, bookmarkUid);
	}
}
