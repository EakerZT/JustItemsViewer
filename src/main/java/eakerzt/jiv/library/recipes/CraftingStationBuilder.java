package eakerzt.jiv.library.recipes;

import com.google.common.collect.ImmutableListMultimap;
import eakerzt.jiv.api.gui.builder.IIngredientAcceptor;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.category.IRecipeCategory;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import eakerzt.jiv.library.recipes.collect.RecipeIngredientRoleMap;

import java.util.List;
import java.util.function.Function;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CraftingStationBuilder {
	private final ImmutableListMultimap.Builder<IRecipeCategory<?>, Consumer<IIngredientAcceptor<?>>> craftingStationsBuilder = ImmutableListMultimap.builder();
	private final RecipeIngredientRoleMap craftingStationMap;

	public CraftingStationBuilder(RecipeIngredientRoleMap craftingStationMap) {
		this.craftingStationMap = craftingStationMap;
	}

	public void addCategoryCatalysts(IRecipeCategory<?> recipeCategory, List<ITypedIngredient<?>> categoryCatalystIngredients) {
		for (ITypedIngredient<?> craftingStation : categoryCatalystIngredients) {
			craftingStationsBuilder.put(recipeCategory, acceptor -> acceptor.add(craftingStation));
			addCategoryCraftingStation(craftingStation, recipeCategory);
		}
	}

	public void addCategoryCraftingStations(
		IRecipeCategory<?> recipeCategory,
		List<Consumer<IIngredientAcceptor<?>>> craftingStations,
		Function<Consumer<IIngredientAcceptor<?>>, Stream<ITypedIngredient<?>>> resolver
	) {
		craftingStationsBuilder.putAll(recipeCategory, craftingStations);
		craftingStations.stream()
			.flatMap(resolver)
			.forEach(craftingStation -> addCategoryCraftingStation(craftingStation, recipeCategory));
	}

	private <T> void addCategoryCraftingStation(ITypedIngredient<T> craftingStation, IRecipeCategory<?> recipeCategory) {
		IRecipeType<?> recipeType = recipeCategory.getRecipeType();
		craftingStationMap.addCraftingStationForCategory(recipeType, craftingStation);
	}

	public ImmutableListMultimap<IRecipeCategory<?>, Consumer<IIngredientAcceptor<?>>> build() {
		return craftingStationsBuilder.build();
	}
}
