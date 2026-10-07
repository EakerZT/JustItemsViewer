package eakerzt.jiv.api.helpers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.IRecipeManager;
import eakerzt.jiv.api.recipe.category.IRecipeCategory;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.ApiStatus;

/**
 * Helper for getting codecs related to JIV and recipes.
 *
 * @since 19.9.0
 */
@ApiStatus.NonExtendable
public interface ICodecHelper {
	/**
	 * @return a codec for {@link IIngredientType}.
	 *
	 * @since 19.9.0
	 */
	Codec<IIngredientType<?>> getIngredientTypeCodec();

	/**
	 * @return a codec for {@link IRecipeType}.
	 *
	 * @since 19.9.0
	 */
	Codec<IRecipeType<?>> getRecipeTypeCodec(IRecipeManager recipeManager);

	/**
	 * @return a codec for any {@link ITypedIngredient}.
	 *
	 * @since 19.9.0
	 */
	MapCodec<ITypedIngredient<?>> getTypedIngredientCodec();

	/**
	 * @return a codec that can only work for one type of {@link ITypedIngredient}.
	 *
	 * @since 19.9.0
	 */
	<T> Codec<ITypedIngredient<T>> getTypedIngredientCodec(IIngredientType<T> ingredientType);

	/**
	 * Gets a codec that uses {@link RecipeHolder} ids and looks them up with the vanilla RecipeManager.
	 * If the recipe is not stored in the RecipeManager, this will use the recipe codec from the RecipeManager.
	 *
	 * @return a codec for any {@link RecipeHolder}.
	 *
	 * @since 19.9.0
	 */
	<T extends RecipeHolder<?>> Codec<T> getRecipeHolderCodec();

	/**
	 * @return a codec for recipes in a given {@link IRecipeCategory}.
	 *
	 * This is generally inefficient, and requires searching JIV for the recipe based on {@link IRecipeCategory#getIdentifier}.
	 * You should prefer using a codec that relies on a fast registry or other methods of finding the recipes efficiently.
	 *
	 * @since 19.9.0
	 */
	<T> Codec<T> getSlowRecipeCategoryCodec(IRecipeCategory<T> recipeCategory, IRecipeManager recipeManager);
}
