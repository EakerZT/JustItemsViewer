package eakerzt.jiv.library.plugins.jiv;

import eakerzt.jiv.api.IModPlugin;
import eakerzt.jiv.api.JivPlugin;
import eakerzt.jiv.api.constants.ModIds;
import eakerzt.jiv.api.constants.VanillaTypes;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.api.helpers.IJivHelpers;
import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.ingredients.IIngredientTypeWithSubtypes;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import eakerzt.jiv.api.registration.IRecipeCategoryRegistration;
import eakerzt.jiv.api.registration.IRecipeRegistration;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.config.IClientConfig;
import eakerzt.jiv.common.config.IClientConfigs;
import eakerzt.jiv.common.gui.textures.Textures;
import eakerzt.jiv.common.recipes.TagRecipeUtil;
import eakerzt.jiv.common.util.RegistryUtil;
import eakerzt.jiv.library.plugins.jiv.info.IngredientInfoRecipeCategory;
import eakerzt.jiv.library.plugins.jiv.tags.ITagInfoRecipe;
import eakerzt.jiv.library.plugins.jiv.tags.TagInfoRecipeCategory;
import eakerzt.jiv.library.plugins.jiv.tags.TagInfoRecipeMaker;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@JivPlugin
public class JivInternalPlugin implements IModPlugin {
	private final List<TagInfoRecipeMaker<?, ?>> tagInfoRecipeMakers = new ArrayList<>();

	@Override
	public Identifier getPluginUid() {
		return Identifier.fromNamespaceAndPath(ModIds.JIV_ID, "internal");
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		IJivHelpers jivHelpers = registration.getJivHelpers();
		IIngredientManager ingredientManager = jivHelpers.getIngredientManager();
		Textures textures = Internal.getTextures();

		registration.addRecipeCategories(new IngredientInfoRecipeCategory(textures));

		tagInfoRecipeMakers.clear();
		IClientConfigs jivClientConfigs = Internal.getClientConfigs();
		IClientConfig clientConfig = jivClientConfigs.getClientConfig();
		if (clientConfig.showTagRecipesEnabled().get()) {
			RegistryUtil.getRegistryAccess()
				.registries()
				.forEach(entry -> {
					Registry<?> registry = entry.value();
					createAndRegisterTagCategory(registration, tagInfoRecipeMakers, ingredientManager, registry);
				});
		}
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		IClientConfigs jivClientConfigs = Internal.getClientConfigs();
		IClientConfig clientConfig = jivClientConfigs.getClientConfig();
		if (clientConfig.showTagRecipesEnabled().get()) {
			for (TagInfoRecipeMaker<?, ?> data : tagInfoRecipeMakers) {
				data.addRecipes(registration);
			}
		}
		tagInfoRecipeMakers.clear();
	}

	private static <B> void createAndRegisterTagCategory(
		IRecipeCategoryRegistration registration,
		List<TagInfoRecipeMaker<?, ?>> tagInfoRecipeMakers,
		IIngredientManager ingredientManager,
		Registry<B> registry
	) {
		registry.getAny()
			.ifPresent(holder -> {
				IJivHelpers jivHelpers = registration.getJivHelpers();
				IGuiHelper guiHelper = jivHelpers.getGuiHelper();

				B ingredient = holder.value();

				IIngredientType<B> type = ingredientManager.getIngredientTypeChecked(ingredient).orElse(null);
				if (type != null) {
					Identifier id = registry.key().identifier();
					IRecipeType<ITagInfoRecipe> recipeType = createTagInfoRecipeType(id);
					registration.addRecipeCategories(
						new TagInfoRecipeCategory<>(guiHelper, recipeType, id)
					);
					tagInfoRecipeMakers.add(new TagInfoRecipeMaker<>(type, recipeType, Function.identity(), registry.key()));
					return;
				}

				IIngredientTypeWithSubtypes<B, Object> typeWithSubtypes = ingredientManager.getIngredientTypeWithSubtypesFromBase(ingredient).orElse(null);
				if (typeWithSubtypes != null) {
					if (createAndRegisterTagCategory(registration, tagInfoRecipeMakers, registry, ingredient, typeWithSubtypes)) {
						return;
					}
				}

				if (ingredient instanceof ItemLike) {
					@SuppressWarnings("unchecked")
					Registry<? extends ItemLike> itemLikeRegistry = (Registry<? extends ItemLike>) registry;
					if (createAndRegisterItemLikeTagCategory(registration, tagInfoRecipeMakers, itemLikeRegistry)) {
						return;
					}
				}
			});
	}

	private static IRecipeType<ITagInfoRecipe> createTagInfoRecipeType(Identifier id) {
		Identifier recipeTypeUid = TagRecipeUtil.getRecipeTypeUid(id);
		return IRecipeType.create(recipeTypeUid, ITagInfoRecipe.class);
	}

	private static <B, I> boolean createAndRegisterTagCategory(
		IRecipeCategoryRegistration registration,
		List<TagInfoRecipeMaker<?, ?>> tagInfoRecipeMakers,
		Registry<B> registry,
		B baseIngredient,
		IIngredientTypeWithSubtypes<B, I> knownType
	) {
		IJivHelpers jivHelpers = registration.getJivHelpers();
		IGuiHelper guiHelper = jivHelpers.getGuiHelper();
		try {
			knownType.getDefaultIngredient(baseIngredient);
		} catch (UnsupportedOperationException ignored) {
			// this method is optional and may not be supported
			return false;
		}
		Identifier id = registry.key().identifier();

		IRecipeType<ITagInfoRecipe> recipeType = createTagInfoRecipeType(id);

		registration.addRecipeCategories(
			new TagInfoRecipeCategory<>(guiHelper, recipeType, id)
		);
		tagInfoRecipeMakers.add(new TagInfoRecipeMaker<>(knownType, recipeType, knownType::getDefaultIngredient, registry.key()));
		return true;
	}

	private static <B extends ItemLike> boolean createAndRegisterItemLikeTagCategory(
		IRecipeCategoryRegistration registration,
		List<TagInfoRecipeMaker<?, ?>> tagInfoRecipeMakers,
		Registry<B> registry
	) {
		IJivHelpers jivHelpers = registration.getJivHelpers();
		IGuiHelper guiHelper = jivHelpers.getGuiHelper();

		ResourceKey<? extends Registry<B>> registryKey = registry.key();
		Identifier id = registryKey.identifier();
		IRecipeType<ITagInfoRecipe> recipeType = createTagInfoRecipeType(id);
		registration.addRecipeCategories(
			new TagInfoRecipeCategory<>(guiHelper, recipeType, id)
		);
		tagInfoRecipeMakers.add(new TagInfoRecipeMaker<>(
			VanillaTypes.ITEM_STACK,
			recipeType,
			i -> i.asItem().getDefaultInstance(),
			registryKey
		));
		return true;
	}
}
