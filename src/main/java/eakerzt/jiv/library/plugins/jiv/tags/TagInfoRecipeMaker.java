package eakerzt.jiv.library.plugins.jiv.tags;

import eakerzt.jiv.api.constants.Tags;
import eakerzt.jiv.api.ingredients.IIngredientHelper;
import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import eakerzt.jiv.api.registration.IRecipeRegistration;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.platform.IPlatformRenderHelper;
import eakerzt.jiv.common.platform.Services;
import eakerzt.jiv.common.util.RegistryUtil;
import eakerzt.jiv.common.ingredients.TypedIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public record TagInfoRecipeMaker<B, I>(
	IIngredientType<I> ingredientType,
	IRecipeType<ITagInfoRecipe> recipeType,
	Function<B, I> baseToIngredient,
	ResourceKey<? extends Registry<B>> registryKey
) {
	private static final Logger LOGGER = LogManager.getLogger();

	public void addRecipes(IRecipeRegistration registration) {
		IIngredientManager ingredientManager = registration.getIngredientManager();
		List<ITagInfoRecipe> recipes = createTagInfoRecipes(ingredientType, registryKey, baseToIngredient, ingredientManager);
		registration.addRecipes(recipeType, recipes);
	}

	private static <B, I> List<ITagInfoRecipe> createTagInfoRecipes(IIngredientType<I> ingredientType, ResourceKey<? extends Registry<B>> registryKey, Function<B, I> baseToIngredient, IIngredientManager ingredientManager) {
		Registry<B> registry = RegistryUtil.getRegistry(registryKey);
		return registry
			.getTags()
			.map(HolderSet.Named::key)
			.<ITagInfoRecipe>mapMulti((tagKey, acceptor) -> {
				if (tagKey.location().getPath().equals(Tags.HIDDEN_FROM_RECIPE_VIEWERS.getPath())) {
					return;
				}
				List<ITypedIngredient<I>> ingredients = getIngredients(registry, tagKey, ingredientType, baseToIngredient, ingredientManager);
				if (!ingredients.isEmpty()) {
					acceptor.accept(new TagInfoRecipe<>(tagKey, ingredients));
				} else if (LOGGER.isDebugEnabled()) {
					IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
					Component tagName = renderHelper.getName(tagKey);
					LOGGER.debug("No valid ingredients found for {} tag: {} ({})", registryKey.identifier(), tagName.getString(), tagKey.location());
				}
			})
			.toList();
	}

	private static <B, I> List<ITypedIngredient<I>> getIngredients(Registry<B> registry, TagKey<B> tagKey, IIngredientType<I> ingredientType, Function<B, I> baseToIngredient, IIngredientManager ingredientManager) {
		List<ITypedIngredient<I>> ingredients = new ArrayList<>();
		IIngredientHelper<I> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		for (Holder<B> i : registry.getTagOrEmpty(tagKey)) {
			B value = i.value();
			I ingredient = baseToIngredient.apply(value);
			ITypedIngredient<I> typedIngredient = TypedIngredient.createAndFilterInvalid(ingredientManager, ingredientType, ingredient, false);
			if (typedIngredient != null && !ingredientHelper.isHiddenFromRecipeViewersByTags(typedIngredient.getIngredient())) {
				ingredients.add(typedIngredient);
			}
		}

		return ingredients;
	}
}
