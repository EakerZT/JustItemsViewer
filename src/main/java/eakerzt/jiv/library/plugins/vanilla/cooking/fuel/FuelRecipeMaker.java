package eakerzt.jiv.library.plugins.vanilla.cooking.fuel;

import eakerzt.jiv.api.recipe.vanilla.IJivFuelingRecipe;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.platform.IPlatformItemStackHelper;
import eakerzt.jiv.common.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class FuelRecipeMaker {

	private FuelRecipeMaker() {
	}

	public static List<IJivFuelingRecipe> getFuelRecipes(IIngredientManager ingredientManager, RecipeType<?> recipeType) {
		ClientLevel level = Objects.requireNonNull(Minecraft.getInstance().level);

		IPlatformItemStackHelper itemStackHelper = Services.PLATFORM.getItemStackHelper();
		return ingredientManager.getAllItemStacks().stream()
			.<IJivFuelingRecipe>mapMulti((stack, consumer) -> {
				int burnTime = itemStackHelper.getBurnTime(stack, recipeType, level.fuelValues());
				if (burnTime > 0) {
					consumer.accept(new FuelingRecipe(List.of(stack), burnTime));
				}
			})
			.sorted(Comparator.comparingInt(IJivFuelingRecipe::getBurnTime))
			.toList();
	}
}
