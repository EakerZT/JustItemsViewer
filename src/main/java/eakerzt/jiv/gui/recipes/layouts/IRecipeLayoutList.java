package eakerzt.jiv.gui.recipes.layouts;

import eakerzt.jiv.api.recipe.IFocusGroup;
import eakerzt.jiv.api.recipe.IRecipeManager;
import eakerzt.jiv.common.config.RecipeSorterStage;
import eakerzt.jiv.common.transfer.RecipeTransferService;
import eakerzt.jiv.gui.bookmarks.BookmarkList;
import eakerzt.jiv.gui.recipes.IRecipeLayoutWithButtons;
import eakerzt.jiv.gui.recipes.RecipesGui;
import eakerzt.jiv.gui.recipes.lookups.IFocusedRecipes;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IRecipeLayoutList {
	static IRecipeLayoutList create(
		Set<RecipeSorterStage> recipeSorterStages,
		@Nullable AbstractContainerMenu container,
		IFocusedRecipes<?> selectedRecipes,
		IFocusGroup focusGroup,
		BookmarkList bookmarkList,
		IRecipeManager recipeManager,
		RecipeTransferService recipeTransferService,
		RecipesGui recipesGui
	) {
		return new LazyRecipeLayoutList<>(
			recipeSorterStages,
			container,
			selectedRecipes,
			bookmarkList,
			recipeManager,
			recipeTransferService,
			recipesGui,
			focusGroup
		);
	}

	int size();

	List<IRecipeLayoutWithButtons<?>> subList(int from, int to);

	Optional<IRecipeLayoutWithButtons<?>> findFirst();

	void tick();
}
