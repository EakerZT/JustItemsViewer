package eakerzt.jiv.gui.bookmarks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import eakerzt.jiv.api.helpers.ICodecHelper;
import eakerzt.jiv.api.ingredients.IIngredientSupplier;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.IRecipeManager;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.recipe.category.IRecipeCategory;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.codecs.EnumCodec;
import eakerzt.jiv.common.transfer.RecipeTransferService;

import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

public final class BookmarkCodec {

	private BookmarkCodec() {}

	public static MapCodec<IBookmark> create(
			ICodecHelper codecHelper,
			IIngredientManager ingredientManager,
			IRecipeManager recipeManager,
			RecipeTransferService recipeTransferService,
			BookmarkFactory bookmarkFactory) {
		MapCodec<? extends IngredientBookmark<?>> ingredientBookmarkCodec =
				createIngredientBookmarkCodec(codecHelper, bookmarkFactory);
		MapCodec<? extends RecipeBookmark<?, ?>> recipeBookmarkCodec =
				createRecipeBookmarkCodec(
						codecHelper, ingredientManager, recipeManager, recipeTransferService);

		return EnumCodec.create(BookmarkType.class)
				.dispatchMap(
						"bookmarkType",
						IBookmark::getType,
						type ->
								switch (type) {
									case INGREDIENT -> ingredientBookmarkCodec;
									case RECIPE -> recipeBookmarkCodec;
								});
	}

	private static MapCodec<? extends IngredientBookmark<?>> createIngredientBookmarkCodec(
			ICodecHelper codecHelper, BookmarkFactory bookmarkFactory) {
		return codecHelper
				.getTypedIngredientCodec()
				.xmap(bookmarkFactory::create, IngredientBookmark::getIngredient);
	}

	private static MapCodec<? extends RecipeBookmark<?, ?>> createRecipeBookmarkCodec(
			ICodecHelper codecHelper,
			IIngredientManager ingredientManager,
			IRecipeManager recipeManager,
			RecipeTransferService recipeTransferService) {
		MapCodec<RecipeBookmark<?, ?>> recipeCodec =
				codecHelper
						.getRecipeTypeCodec(recipeManager)
						.dispatchMap(
								"recipeType",
								bookmark -> bookmark.getRecipeCategory().getRecipeType(),
								recipeType -> {
									IRecipeCategory<?> recipeCategory =
											recipeManager.getRecipeCategory(recipeType);
									return createRecipeBookmarkCodec(
													recipeCategory,
													codecHelper,
													recipeManager,
													ingredientManager,
													recipeTransferService)
											.fieldOf("recipe");
								});
		return RecordCodecBuilder.<RecipeBookmark<?, ?>>mapCodec(
				instance ->
						instance.group(
										recipeCodec.forGetter(bookmark -> bookmark),
										Codec.intRange(-1, Integer.MAX_VALUE)
												.optionalFieldOf("outputSlot", -1)
												.forGetter(RecipeBookmark::getOutputSlot),
										Codec.intRange(0, Integer.MAX_VALUE)
												.optionalFieldOf("outputChoice", 0)
												.forGetter(RecipeBookmark::getOutputChoice),
										codecHelper
												.getTypedIngredientCodec()
												.codec()
												.optionalFieldOf("selectedOutput")
												.forGetter(
														bookmark ->
																bookmark.getOutputSlot() < 0
																		? Optional.empty()
																		: Optional.of(
																				bookmark
																						.getDisplayIngredient())))
								.apply(
										instance,
										(bookmark, slot, choice, output) -> {
											if (slot < 0) return bookmark;
											if (output.isEmpty())
												throw new IllegalArgumentException(
														"Missing selected recipe output");
											return bookmark.withOutputSelection(
													slot, choice, output.get());
										}));
	}

	private static <R> Codec<? extends RecipeBookmark<R, ?>> createRecipeBookmarkCodec(
			IRecipeCategory<R> recipeCategory,
			ICodecHelper codecHelper,
			IRecipeManager recipeManager,
			IIngredientManager ingredientManager,
			RecipeTransferService recipeTransferService) {
		return recipeCategory
				.getCodec(codecHelper, recipeManager)
				.flatXmap(
						recipe -> {
							Identifier recipeUid = recipeCategory.getIdentifier(recipe);
							if (recipeUid == null) {
								return DataResult.error(() -> "Recipe has no registry name");
							}
							IIngredientSupplier ingredients =
									recipeManager.getRecipeIngredients(recipeCategory, recipe);

							boolean displayIsOutput;
							ITypedIngredient<?> displayIngredient;

							List<ITypedIngredient<?>> outputs =
									ingredients.getIngredients(RecipeIngredientRole.OUTPUT);
							if (!outputs.isEmpty()) {
								displayIngredient = outputs.getFirst();
								displayIsOutput = true;
							} else {
								List<ITypedIngredient<?>> inputs =
										ingredients.getIngredients(RecipeIngredientRole.INPUT);
								if (inputs.isEmpty()) {
									return DataResult.error(
											() -> "Recipe has no inputs or outputs");
								}
								displayIngredient = inputs.getFirst();
								displayIsOutput = false;
							}

							displayIngredient =
									ingredientManager.normalizeTypedIngredient(displayIngredient);
							RecipeBookmark<R, ?> bookmark =
									new RecipeBookmark<>(
											recipeCategory,
											recipe,
											recipeUid,
											displayIngredient,
											displayIsOutput,
											recipeTransferService);
							return DataResult.success(bookmark);
						},
						bookmark -> {
							R recipe = bookmark.getRecipe();
							return DataResult.success(recipe);
						});
	}
}
