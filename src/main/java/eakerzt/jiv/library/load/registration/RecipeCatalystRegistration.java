package eakerzt.jiv.library.load.registration;

import com.google.common.collect.ImmutableListMultimap;
import eakerzt.jiv.api.constants.VanillaTypes;
import eakerzt.jiv.api.gui.builder.IIngredientAcceptor;
import eakerzt.jiv.api.helpers.IJivHelpers;
import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import eakerzt.jiv.api.registration.IRecipeCatalystRegistration;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.collect.ListMultiMap;
import eakerzt.jiv.common.ingredients.TypedIngredient;
import eakerzt.jiv.common.util.ErrorUtil;
import eakerzt.jiv.library.ingredients.IIngredientManagerInternal;
import eakerzt.jiv.library.ingredients.SimpleIngredientAcceptor;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.function.Consumer;

public class RecipeCatalystRegistration implements IRecipeCatalystRegistration {
	private final ListMultiMap<IRecipeType<?>, Consumer<IIngredientAcceptor<?>>> craftingStations = new ListMultiMap<>();
	private final IIngredientManagerInternal ingredientManager;
	private final IJivHelpers jivHelpers;
	private final ContextMap contextMap;

	public RecipeCatalystRegistration(
		IIngredientManagerInternal ingredientManager,
		IJivHelpers jivHelpers,
		ContextMap contextMap
	) {
		this.ingredientManager = ingredientManager;
		this.jivHelpers = jivHelpers;
		this.contextMap = contextMap;
	}

	@Override
	public IIngredientManager getIngredientManager() {
		return ingredientManager;
	}

	@Override
	public IJivHelpers getJivHelpers() {
		return jivHelpers;
	}

	@Override
	public <T> void addCraftingStations(IRecipeType<?> recipeType, IIngredientType<T> ingredientType, List<T> ingredients) {
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(ingredients, "ingredients");

		for (T ingredient : ingredients) {
			ITypedIngredient<T> typedIngredient = TypedIngredient.createAndFilterInvalid(this.ingredientManager, ingredientType, ingredient, true);
			if (typedIngredient == null) {
				throw new IllegalArgumentException("Recipe catalyst must be a valid ingredient");
			}
			addCraftingStation(recipeType, typedIngredient);
		}
	}

	@Override
	public void addCraftingStation(IRecipeType<?> recipeType, SlotDisplay slotDisplay) {
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.checkNotNull(slotDisplay, "slotDisplay");

		addCraftingStation(recipeType, acceptor -> acceptor.add(slotDisplay));
	}

	@Override
	public void addCraftingStation(IRecipeType<?> recipeType, ItemLike... ingredients) {
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.checkNotNull(ingredients, "ingredients");

		for (ItemLike itemLike : ingredients) {
			ItemStack itemStack = itemLike.asItem().getDefaultInstance();
			ITypedIngredient<ItemStack> typedIngredient = TypedIngredient.createAndFilterInvalid(this.ingredientManager, VanillaTypes.ITEM_STACK, itemStack, true);
			if (typedIngredient == null) {
				throw new IllegalArgumentException("Recipe catalyst must be a valid ingredient");
			}
			addCraftingStation(recipeType, typedIngredient);
		}
	}

	@Override
	public <T> void addCraftingStation(IRecipeType<?> recipeType, IIngredientType<T> ingredientType, T ingredient) {
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(ingredient, "ingredient");

		ITypedIngredient<T> typedIngredient = TypedIngredient.createAndFilterInvalid(this.ingredientManager, ingredientType, ingredient, true);
		if (typedIngredient == null) {
			throw new IllegalArgumentException("Recipe catalyst must be a valid ingredient");
		}
		addCraftingStation(recipeType, typedIngredient);
	}

	private void addCraftingStation(IRecipeType<?> recipeType, ITypedIngredient<?> ingredient) {
		addCraftingStation(recipeType, acceptor -> acceptor.add(ingredient));
	}

	private void addCraftingStation(IRecipeType<?> recipeType, Consumer<IIngredientAcceptor<?>> craftingStation) {
		SimpleIngredientAcceptor acceptor = new SimpleIngredientAcceptor(ingredientManager, contextMap, RecipeIngredientRole.CRAFTING_STATION);
		craftingStation.accept(acceptor);
		if (!acceptor.getAllSlotIngredients().isEmpty()) {
			this.craftingStations.put(recipeType, craftingStation);
		}
	}

	public ImmutableListMultimap<IRecipeType<?>, Consumer<IIngredientAcceptor<?>>> getCraftingStations() {
		return craftingStations.toImmutable();
	}
}
