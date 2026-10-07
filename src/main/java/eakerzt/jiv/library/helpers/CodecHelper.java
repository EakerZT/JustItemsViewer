package eakerzt.jiv.library.helpers;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eakerzt.jiv.api.helpers.ICodecHelper;
import eakerzt.jiv.api.ingredients.IIngredientSupplier;
import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.IFocus;
import eakerzt.jiv.api.recipe.IFocusFactory;
import eakerzt.jiv.api.recipe.IRecipeManager;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.recipe.category.IRecipeCategory;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.codecs.EnumCodec;
import eakerzt.jiv.common.codecs.TupleCodec;
import eakerzt.jiv.common.codecs.TypedIngredientCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodecHelper implements ICodecHelper {
	private static final Codec<RecipeHolder<?>> RECIPE_HOLDER_CODEC = Codec.lazyInitialized(() -> {
		RecipeMap recipes = Internal.getClientSyncedRecipes();

		return Codec.either(
				ResourceKey.codec(Registries.RECIPE),
				TupleCodec.of(
					ResourceKey.codec(Registries.RECIPE),
					Recipe.CODEC
				)
			)
			.flatXmap(
				either -> {
					return either.map(
						recipeKey -> {
							RecipeHolder<?> recipeHolder = recipes.byKey(recipeKey);
							if (recipeHolder == null) {
								return DataResult.error(() -> "Could not find recipe for key: " + recipeKey);
							}
							return DataResult.success(recipeHolder);
						},
						pair -> {
							ResourceKey<Recipe<?>> recipeKey = pair.getFirst();
							Recipe<?> recipe = pair.getSecond();
							if (recipe == null) {
								return DataResult.error(() -> "Could not find recipe for key: " + recipeKey);
							}
							RecipeHolder<?> recipeHolder = new RecipeHolder<>(recipeKey, recipe);
							return DataResult.success(recipeHolder);
						}
					);
				},
				recipeHolder -> {
					ResourceKey<Recipe<?>> recipeKey = recipeHolder.id();
					RecipeHolder<?> found = recipes.byKey(recipeKey);
					if (recipeHolder.equals(found)) {
						return DataResult.success(Either.left(recipeKey));
					}
					Recipe<?> recipe = recipeHolder.value();
					return DataResult.success(Either.right(Pair.of(recipeKey, recipe)));
				}
			);
	});

	private final IIngredientManager ingredientManager;
	private final IFocusFactory focusFactory;
	private final Map<IRecipeType<?>, Codec<?>> defaultRecipeCodecs = new HashMap<>();
	private @Nullable Codec<IRecipeType<?>> recipeTypeCodec;

	public CodecHelper(IIngredientManager ingredientManager, IFocusFactory focusFactory) {
		this.ingredientManager = ingredientManager;
		this.focusFactory = focusFactory;
	}

	@Override
	public Codec<IIngredientType<?>> getIngredientTypeCodec() {
		return TypedIngredientCodecs.getIngredientTypeCodec(ingredientManager);
	}

	@Override
	public MapCodec<ITypedIngredient<?>> getTypedIngredientCodec() {
		return TypedIngredientCodecs.getIngredientCodec(ingredientManager);
	}

	@Override
	public <T> Codec<ITypedIngredient<T>> getTypedIngredientCodec(IIngredientType<T> ingredientType) {
		return TypedIngredientCodecs.getIngredientCodec(ingredientType, ingredientManager);
	}

	@Override
	public <T extends RecipeHolder<?>> Codec<T> getRecipeHolderCodec() {
		@SuppressWarnings("unchecked")
		Codec<T> recipeHolderCodec = (Codec<T>) RECIPE_HOLDER_CODEC;
		return recipeHolderCodec;
	}

	@Override
	public <T> Codec<T> getSlowRecipeCategoryCodec(IRecipeCategory<T> recipeCategory, IRecipeManager recipeManager) {
		IRecipeType<T> recipeType = recipeCategory.getRecipeType();
		@SuppressWarnings("unchecked")
		Codec<T> codec = (Codec<T>) defaultRecipeCodecs.get(recipeType);
		if (codec == null) {
			codec = createDefaultRecipeCategoryCodec(recipeManager, recipeCategory);
			defaultRecipeCodecs.put(recipeType, codec);
		}
		return codec;
	}

	private <T> Codec<T> createDefaultRecipeCategoryCodec(IRecipeManager recipeManager, IRecipeCategory<T> recipeCategory) {
		Codec<Data> dataCodec = RecordCodecBuilder.create((builder) -> {
			return builder.group(
					Identifier.CODEC.fieldOf("registryId")
						.forGetter(Data::registryId),
					getTypedIngredientCodec().codec().fieldOf("ingredient")
						.forGetter(Data::ingredient),
					EnumCodec.create(RecipeIngredientRole.class).fieldOf("ingredient_role")
						.forGetter(Data::ingredientRole)
				)
				.apply(builder, Data::new);
		});
		return dataCodec.flatXmap(
			data -> {
				Identifier registryName = data.registryId();
				ITypedIngredient<?> ingredient = data.ingredient();
				IFocus<?> focus = focusFactory.createFocus(data.ingredientRole(), ingredient);

				IRecipeType<T> recipeType = recipeCategory.getRecipeType();

				return recipeManager.createRecipeLookup(recipeType)
					.limitFocus(List.of(focus))
					.get()
					.filter(recipe -> registryName.equals(recipeCategory.getIdentifier(recipe)))
					.findFirst()
					.map(DataResult::success)
					.orElseGet(() -> DataResult.error(() -> "No recipe found for registry id: " + registryName));
			},
			recipe -> {
				Identifier registryId = recipeCategory.getIdentifier(recipe);
				if (registryId == null) {
					return DataResult.error(() -> "No registry id for recipe");
				}
				IIngredientSupplier ingredients = recipeManager.getRecipeIngredients(recipeCategory, recipe);
				List<ITypedIngredient<?>> outputs = ingredients.getIngredients(RecipeIngredientRole.OUTPUT);
				if (!outputs.isEmpty()) {
					Data result = new Data(registryId, outputs.getFirst(), RecipeIngredientRole.OUTPUT);
					return DataResult.success(result);
				}
				List<ITypedIngredient<?>> inputs = ingredients.getIngredients(RecipeIngredientRole.INPUT);
				if (!inputs.isEmpty()) {
					Data result = new Data(registryId, inputs.getFirst(), RecipeIngredientRole.INPUT);
					return DataResult.success(result);
				}
				return DataResult.error(() -> "No inputs or outputs for recipe");
			}
		);
	}

	private record Data(Identifier registryId, ITypedIngredient<?> ingredient, RecipeIngredientRole ingredientRole) {}

	@Override
	public Codec<IRecipeType<?>> getRecipeTypeCodec(IRecipeManager recipeManager) {
		if (recipeTypeCodec == null) {
			recipeTypeCodec = Identifier.CODEC.flatXmap(
				uid -> {
					return recipeManager.getRecipeType(uid)
						.map(DataResult::success)
						.orElseGet(() -> DataResult.error(() -> "Failed to find recipe type " + uid));
				},
				recipeType -> {
					Identifier uid = recipeType.getUid();
					return DataResult.success(uid);
				}
			);
		}
		return recipeTypeCodec;
	}
}
