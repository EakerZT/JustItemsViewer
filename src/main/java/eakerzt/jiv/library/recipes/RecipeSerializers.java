package eakerzt.jiv.library.recipes;

import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;

import org.jspecify.annotations.Nullable;
import java.util.function.Supplier;

public class RecipeSerializers {
	private static @Nullable RecipeSerializers INSTANCE;

	private final Supplier<RecipeSerializer<? extends CraftingRecipe>> jivShapedRecipeSerializer;
	private final Supplier<RecipeSerializer<? extends SmeltingRecipe>> jivSmeltingRecipeSerializer;

	public static void register(
		Supplier<RecipeSerializer<? extends CraftingRecipe>> jivShapedRecipeSerializer,
		Supplier<RecipeSerializer<? extends SmeltingRecipe>> jivSmeltingRecipeSerializer
	) {
		INSTANCE = new RecipeSerializers(jivShapedRecipeSerializer, jivSmeltingRecipeSerializer);
	}

	private RecipeSerializers(
		Supplier<RecipeSerializer<? extends CraftingRecipe>> jivShapedRecipeSerializer,
		Supplier<RecipeSerializer<? extends SmeltingRecipe>> jivSmeltingRecipeSerializer
	) {
		this.jivShapedRecipeSerializer = jivShapedRecipeSerializer;
		this.jivSmeltingRecipeSerializer = jivSmeltingRecipeSerializer;
	}

	public static RecipeSerializer<? extends CraftingRecipe> getJivShapedRecipeSerializer() {
		if (INSTANCE == null) {
			throw new IllegalStateException("Recipe serializer not yet initialized");
		}
		return INSTANCE.jivShapedRecipeSerializer.get();
	}

	public static RecipeSerializer<? extends SmeltingRecipe> getJivSmeltingRecipeSerializer() {
		if (INSTANCE == null) {
			throw new IllegalStateException("Recipe serializer not yet initialized");
		}
		return INSTANCE.jivSmeltingRecipeSerializer.get();
	}
}
