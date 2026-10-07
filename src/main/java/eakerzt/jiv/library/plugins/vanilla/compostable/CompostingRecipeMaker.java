package eakerzt.jiv.library.plugins.vanilla.compostable;

import eakerzt.jiv.api.constants.ModIds;
import eakerzt.jiv.api.constants.VanillaTypes;
import eakerzt.jiv.api.ingredients.IIngredientHelper;
import eakerzt.jiv.api.recipe.vanilla.IJivCompostingRecipe;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.platform.IPlatformIngredientHelper;
import eakerzt.jiv.common.platform.Services;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class CompostingRecipeMaker {
	public static List<IJivCompostingRecipe> getRecipes(IIngredientManager ingredientManager) {
		Collection<ItemStack> allIngredients = ingredientManager.getAllItemStacks();
		IIngredientHelper<ItemStack> ingredientHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM_STACK);
		IPlatformIngredientHelper platformIngredientHelper = Services.PLATFORM.getIngredientHelper();

		return allIngredients.stream()
			.<IJivCompostingRecipe>mapMulti((itemStack, consumer) -> {
				float compostValue = platformIngredientHelper.getCompostValue(itemStack);
				if (compostValue > 0) {
					Identifier ingredientUid = ingredientHelper.getIdentifier(itemStack);
					String ingredientUidPath = ingredientUid.getPath();
					Identifier recipeUid = Identifier.fromNamespaceAndPath(ModIds.JIV_ID, ingredientUidPath);
					CompostingRecipe recipe = new CompostingRecipe(itemStack, compostValue, recipeUid);
					consumer.accept(recipe);
				}
			})
			.sorted(Comparator.comparingDouble(IJivCompostingRecipe::getChance))
			.toList();
	}
}
