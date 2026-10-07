package eakerzt.jiv.neoforge.platform;

import eakerzt.jiv.api.constants.VanillaTypes;
import eakerzt.jiv.api.ingredients.IIngredientHelper;
import eakerzt.jiv.api.recipe.category.extensions.vanilla.brewing.IExtendableBrewingRecipeCategory;
import eakerzt.jiv.api.recipe.vanilla.IJivBrewingRecipe;
import eakerzt.jiv.api.recipe.vanilla.IVanillaRecipeFactory;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.platform.IPlatformBrewingHelper;
import eakerzt.jiv.common.recipes.BrewingExtensionHelper;
import eakerzt.jiv.library.util.BrewingRecipeMakerCommon;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BrewingHelper implements IPlatformBrewingHelper {
	@Override
	public void registerCategoryExtensions(
		IExtendableBrewingRecipeCategory brewingCategory,
		IIngredientManager ingredientManager
	) {
		IIngredientHelper<ItemStack> itemStackHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM_STACK);
		brewingCategory.addExtension(BrewingRecipe.class, new BrewingRecipeCategoryExtension(itemStackHelper));
	}

	@Override
	public List<IJivBrewingRecipe> getBrewingRecipes(
		IIngredientManager ingredientManager,
		IVanillaRecipeFactory vanillaRecipeFactory,
		PotionBrewing potionBrewing,
		ContextMap contextMap,
		BrewingExtensionHelper brewingExtensionHelper
	) {
		Set<IJivBrewingRecipe> recipes = BrewingRecipeMakerCommon.getVanillaBrewingRecipes(
			vanillaRecipeFactory,
			ingredientManager,
			potionBrewing,
			contextMap
		);
		recipes.addAll(
			brewingExtensionHelper.getBrewingRecipes(
				potionBrewing.getRecipes(),
				vanillaRecipeFactory,
				contextMap
			)
		);
		return new ArrayList<>(recipes);
	}
}
