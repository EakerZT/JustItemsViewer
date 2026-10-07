package eakerzt.jiv.gui.recipes;

import eakerzt.jiv.api.gui.builder.IIngredientAcceptor;
import eakerzt.jiv.api.recipe.IFocusGroup;
import eakerzt.jiv.api.recipe.category.IRecipeCategory;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import eakerzt.jiv.gui.bookmarks.BookmarkList;
import eakerzt.jiv.gui.recipes.lookups.IFocusedRecipes;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jspecify.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface IRecipeGuiLogic {

	String getPageString();

	boolean hasMultipleCategories();

	boolean hasAllCategories();

	boolean previousRecipeCategory();

	int getRecipesPerPage();

	boolean nextRecipeCategory();

	void setRecipeCategory(IRecipeCategory<?> category);

	boolean hasMultiplePages();

	void goToFirstPage();

	boolean previousPage();

	boolean nextPage();

	void tick();

	boolean showFocus(IFocusGroup focuses);

	boolean showRecipes(IFocusedRecipes<?> recipes, IFocusGroup focuses);

	boolean back();

	boolean forward();

	void clearHistory();

	boolean showAllRecipes();

	boolean showCategories(List<IRecipeType<?>> recipeTypes);

	IRecipeCategory<?> getSelectedRecipeCategory();

	@Unmodifiable
	List<IRecipeCategory<?>> getRecipeCategories();

	Stream<Consumer<IIngredientAcceptor<?>>> getCraftingStations();

	List<IRecipeLayoutWithButtons<?>> getVisibleRecipeLayoutsWithButtons(
		int availableHeight,
		int minRecipePadding,
		@Nullable AbstractContainerMenu container,
		BookmarkList bookmarkList,
		RecipesGui recipesGui
	);
}
