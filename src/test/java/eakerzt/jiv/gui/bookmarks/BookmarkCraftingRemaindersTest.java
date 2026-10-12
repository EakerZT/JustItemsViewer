package eakerzt.jiv.gui.bookmarks;

import eakerzt.jiv.api.constants.VanillaTypes;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.common.ingredients.TypedIngredient;
import net.minecraft.core.NonNullList;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class BookmarkCraftingRemaindersTest {
	@BeforeAll static void bootstrap() { Bootstrap.bootStrap(); }

	private static ItemStack stack(net.minecraft.world.item.Item item, int count) {
		return new ItemStack(net.minecraft.core.Holder.direct(item,
				net.minecraft.core.component.DataComponentMap.builder()
						.set(net.minecraft.core.component.DataComponents.MAX_STACK_SIZE, 64).build()), count);
	}

	private static ItemStack milkStack() { return stack(Items.MILK_BUCKET, 1); }

	private static NonNullList<ItemStack> buckets(CraftingInput input) {
		var returned = NonNullList.withSize(input.size(), ItemStack.EMPTY);
		for (int i = 0; i < input.size(); i++)
			if (!input.getItem(i).isEmpty()) returned.set(i, stack(Items.BUCKET, 1));
		return returned;
	}

	private static BookmarkRecipeData.Slot milk(int index, boolean reusable) {
		return new BookmarkRecipeData.Slot(index, RecipeIngredientRole.INPUT,
				List.of(TypedIngredient.createUnvalidated(VanillaTypes.ITEM_STACK, milkStack())),
				reusable, 1);
	}

	private static CraftingRecipe recipe(Function<CraftingInput, NonNullList<ItemStack>> returns) {
		return (CraftingRecipe) Proxy.newProxyInstance(CraftingRecipe.class.getClassLoader(),
				new Class<?>[]{CraftingRecipe.class}, (proxy, method, args) -> {
					if (method.getName().equals("getRemainingItems")) return returns.apply((CraftingInput) args[0]);
					throw new UnsupportedOperationException(method.getName());
				});
	}

	@Test void countsEveryReturnedBucketAndPreservesGridHolesWhenTrimming() {
		var data = new BookmarkRecipeData(List.of(milk(2, false), milk(4, false)), Map.of(2, 4, 4, 6));
		var result = data.craftingRemainders(recipe(input -> {
			assertEquals(2, input.width()); assertEquals(2, input.height());
			assertTrue(input.getItem(0).isEmpty());
			assertTrue(input.getItem(1).is(Items.MILK_BUCKET));
			assertTrue(input.getItem(2).is(Items.MILK_BUCKET));
			return buckets(input);
		}), new BookmarkState());
		assertEquals(2, result.size());
		assertTrue(result.get(2).is(Items.BUCKET));
		assertTrue(result.get(4).is(Items.BUCKET));
	}

	@Test void removedAndNonConsumedInputsDoNotAddReturnedContainers() {
		var data = new BookmarkRecipeData(List.of(milk(0, false), milk(1, true)), Map.of(0, 0, 1, 1));
		var state = new BookmarkState(); state.removedSlots.add(0);
		assertTrue(data.craftingRemainders(recipe(BookmarkCraftingRemaindersTest::buckets), state).isEmpty());
	}

	@Test void usesRecipeOverrideInsteadOfAssumingTheItemsDefaultBucket() {
		var data = new BookmarkRecipeData(List.of(milk(0, false)), Map.of(0, 0));
		var returned = stack(Items.GLASS_BOTTLE, 2);
		var result = data.craftingRemainders(recipe(input -> {
			var values = NonNullList.withSize(input.size(), ItemStack.EMPTY);
			values.set(0, returned);
			return values;
		}), new BookmarkState());
		assertTrue(result.get(0).is(Items.GLASS_BOTTLE));
		assertEquals(2, result.get(0).getCount());
		assertNotSame(returned, result.get(0));
	}
}
