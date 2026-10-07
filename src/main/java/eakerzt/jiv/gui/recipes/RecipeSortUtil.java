package eakerzt.jiv.gui.recipes;

import eakerzt.jiv.api.gui.IRecipeLayoutDrawable;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotView;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotsView;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.recipe.category.IRecipeCategory;
import eakerzt.jiv.common.transfer.RecipeTransferService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.Comparator;
import java.util.List;

public class RecipeSortUtil {
	private static final Comparator<IRecipeLayoutWithButtons<?>> COMPARATOR = createComparator();

	public static List<IRecipeCategory<?>> sortRecipeCategories(
		List<IRecipeCategory<?>> recipeCategories,
		RecipeTransferService recipeTransferService
	) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null) {
			return recipeCategories;
		}
		AbstractContainerMenu openContainer = player.containerMenu;
		//noinspection ConstantConditions
		if (openContainer == null) {
			return recipeCategories;
		}

		Comparator<IRecipeCategory<?>> comparator = Comparator.comparing((IRecipeCategory<?> r) -> {
				return recipeTransferService.hasRecipeTransferHandler(openContainer, r);
			})
			.reversed();

		return recipeCategories.stream()
			.sorted(comparator)
			.toList();
	}

	public static Comparator<IRecipeLayoutWithButtons<?>> getComparator() {
		return COMPARATOR;
	}

	private static Comparator<IRecipeLayoutWithButtons<?>> createComparator() {
		return Comparator.comparingInt(r -> {
			IRecipeLayoutDrawable<?> recipeLayout = r.getRecipeLayout();

			int missingCount = r.getMissingCountHint();
			if (missingCount == -1) {
				return 0;
			}

			IRecipeSlotsView recipeSlotsView = recipeLayout.getRecipeSlotsView();
			int ingredientCount = inputCount(recipeSlotsView);
			if (ingredientCount == 0) {
				return 0;
			}

			int matchCount = ingredientCount - missingCount;
			int matchPercent = 100 * matchCount / ingredientCount;
			return -matchPercent;
		});
	}

	private static int inputCount(IRecipeSlotsView recipeSlotsView) {
		int count = 0;
		for (IRecipeSlotView i : recipeSlotsView.getSlotViews()) {
			if (i.getRole() == RecipeIngredientRole.INPUT && !i.isEmpty()) {
				count++;
			}
		}
		return count;
	}
}
