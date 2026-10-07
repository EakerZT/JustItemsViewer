package eakerzt.jiv.library.recipes;

import eakerzt.jiv.api.constants.VanillaTypes;
import eakerzt.jiv.api.gui.builder.IIngredientAcceptor;
import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.ICraftingStationLookup;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CraftingStationLookup implements ICraftingStationLookup {
	private final IRecipeType<?> recipeType;
	private final RecipeManagerInternal recipeManager;
	private boolean includeHidden;

	public CraftingStationLookup(IRecipeType<?> recipeType, RecipeManagerInternal recipeManager) {
		this.recipeType = recipeType;
		this.recipeManager = recipeManager;
	}

	@Override
	public CraftingStationLookup includeHidden() {
		this.includeHidden = true;
		return this;
	}

	@Override
	public Stream<ITypedIngredient<?>> get() {
		return recipeManager.getCraftingStations(recipeType, true)
			.flatMap(craftingStation -> recipeManager.getCraftingStationIngredients(craftingStation, includeHidden));
	}

	@Override
	public Stream<Consumer<IIngredientAcceptor<?>>> getGroups() {
		return recipeManager.getCraftingStations(recipeType, includeHidden);
	}

	@Override
	public <V> Stream<V> get(IIngredientType<V> ingredientType) {
		return get()
			.map(i -> i.getIngredient(ingredientType))
			.flatMap(Optional::stream);
	}

	@Override
	public Stream<ItemStack> getItemStack() {
		return get(VanillaTypes.ITEM_STACK);
	}
}
