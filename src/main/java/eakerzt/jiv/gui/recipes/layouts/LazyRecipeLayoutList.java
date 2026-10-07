package eakerzt.jiv.gui.recipes.layouts;

import eakerzt.jiv.api.gui.IRecipeLayoutDrawable;
import eakerzt.jiv.api.recipe.IFocusGroup;
import eakerzt.jiv.api.recipe.IRecipeManager;
import eakerzt.jiv.api.recipe.advanced.IRecipeButtonControllerFactory;
import eakerzt.jiv.api.recipe.category.IRecipeCategory;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.api.runtime.IJivRuntime;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.config.RecipeSorterStage;
import eakerzt.jiv.common.transfer.RecipeTransferService;
import eakerzt.jiv.gui.bookmarks.BookmarkList;
import eakerzt.jiv.gui.bookmarks.RecipeBookmark;
import eakerzt.jiv.gui.recipes.IRecipeLayoutWithButtons;
import eakerzt.jiv.gui.recipes.RecipeLayoutWithButtons;
import eakerzt.jiv.gui.recipes.RecipeSortUtil;
import eakerzt.jiv.gui.recipes.RecipesGui;
import eakerzt.jiv.gui.recipes.lookups.IFocusedRecipes;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class LazyRecipeLayoutList<T> implements IRecipeLayoutList {
	private final IRecipeManager recipeManager;
	private final RecipeTransferService recipeTransferService;
	private final IRecipeCategory<T> recipeCategory;
	private final RecipesGui recipesGui;
	private final IFocusGroup focusGroup;
	private final List<IRecipeLayoutWithButtons<?>> results;
	private final List<IRecipeLayoutWithButtons<?>> craftMissing;
	private final Iterator<T> unsortedIterator;
	private final int size;

	private final boolean matchingCraftable;
	private final BookmarkList bookmarkList;

	public LazyRecipeLayoutList(
		Set<RecipeSorterStage> recipeSorterStages,
		@Nullable AbstractContainerMenu container,
		IFocusedRecipes<T> selectedRecipes,
		BookmarkList bookmarkList,
		IRecipeManager recipeManager,
		RecipeTransferService recipeTransferService,
		RecipesGui recipesGui,
		IFocusGroup focusGroup
	) {
		this.bookmarkList = bookmarkList;
		boolean matchingBookmarks = recipeSorterStages.contains(RecipeSorterStage.BOOKMARKED);
		boolean matchingCraftable = recipeSorterStages.contains(RecipeSorterStage.CRAFTABLE);
		this.recipeManager = recipeManager;
		this.recipeTransferService = recipeTransferService;
		this.recipesGui = recipesGui;
		this.focusGroup = focusGroup;
		this.results = new ArrayList<>();
		this.craftMissing = new ArrayList<>();
		this.recipeCategory = selectedRecipes.getRecipeCategory();

		List<T> recipes = selectedRecipes.getRecipes();
		this.size = recipes.size();

		if (matchingCraftable && container != null) {
			this.matchingCraftable = recipeTransferService.hasRecipeTransferHandler(container, recipeCategory);
		} else {
			this.matchingCraftable = false;
		}

		if (matchingBookmarks) {
			// if bookmarks go first, start by grabbing all the bookmarked elements, it's relatively cheap
			IRecipeType<T> recipeType = recipeCategory.getRecipeType();
			List<IRecipeButtonControllerFactory> recipeButtonControllerFactories = recipeManager.getRecipeButtonControllerFactories();

			recipes = new ArrayList<>(recipes);
			Iterator<T> iterator = recipes.iterator();
			while (iterator.hasNext()) {
				T recipe = iterator.next();
				RecipeBookmark<T, ?> recipeBookmark = bookmarkList.getMatchingBookmark(recipeType, recipe);
				if (recipeBookmark != null) {
					IRecipeLayoutDrawable<T> recipeLayout = recipeManager.createRecipeLayoutDrawableOrShowError(recipeCategory, recipe, focusGroup);
					IRecipeLayoutWithButtons<T> recipeLayoutWithButtons = RecipeLayoutWithButtons.create(recipeLayout, recipeBookmark, bookmarkList, recipesGui, recipeTransferService, recipeButtonControllerFactories);
					results.add(recipeLayoutWithButtons);
					iterator.remove();
				}
			}
		}

		this.unsortedIterator = recipes.iterator();
	}

	private IRecipeLayoutDrawable<T> createRecipeLayout(T recipe) {
		return recipeManager.createRecipeLayoutDrawableOrShowError(recipeCategory, recipe, focusGroup);
	}

	private IRecipeLayoutWithButtons<T> createRecipeLayoutWithButtons(
		IRecipeLayoutDrawable<T> recipeLayoutDrawable,
		IIngredientManager ingredientManager,
		List<IRecipeButtonControllerFactory> recipeButtonControllerFactories
	) {
		RecipeBookmark<T, ?> recipeBookmark = RecipeBookmark.create(recipeLayoutDrawable, ingredientManager, recipeTransferService);
		return RecipeLayoutWithButtons.create(recipeLayoutDrawable, recipeBookmark, bookmarkList, recipesGui, recipeTransferService, recipeButtonControllerFactories);
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public List<IRecipeLayoutWithButtons<?>> subList(int from, int to) {
		ensureResults(to - 1);
		return results.subList(from, to);
	}

	private void ensureResults(int index) {
		AbstractContainerMenu container = recipesGui.getParentContainerMenu();
		while (index >= results.size()) {
			if (!calculateNextResult()) {
				return;
			}
		}
	}

	@Override
	public Optional<IRecipeLayoutWithButtons<?>> findFirst() {
		ensureResults(0);
		if (results.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(results.getFirst());
	}

	@Override
	public void tick() {
		calculateNextResult();
	}

	private boolean calculateNextResult() {
		IJivRuntime jivRuntime = Internal.getJivRuntime();
		IIngredientManager ingredientManager = jivRuntime.getIngredientManager();
		List<IRecipeButtonControllerFactory> recipeButtonControllerFactories = recipeManager.getRecipeButtonControllerFactories();

		while (unsortedIterator.hasNext()) {
			T recipe = unsortedIterator.next();
			IRecipeLayoutDrawable<T> recipeLayout = createRecipeLayout(recipe);
			IRecipeLayoutWithButtons<T> recipeLayoutWithButtons = createRecipeLayoutWithButtons(recipeLayout, ingredientManager, recipeButtonControllerFactories);

			if (matchingCraftable) {
				// if craftables go first, look for a 100% craftable element
				int missingCountHint = recipeLayoutWithButtons.getMissingCountHint();
				if (missingCountHint == 0) {
					results.add(recipeLayoutWithButtons);
					return true;
				} else {
					craftMissing.add(recipeLayoutWithButtons);
				}
			} else {
				results.add(recipeLayoutWithButtons);
				return true;
			}
		}

		// from here we're finished with calculating all the transfer handlers,
		// just sort and add everything left to the results
		if (!craftMissing.isEmpty()) {
			craftMissing.sort(RecipeSortUtil.getComparator());
			results.addAll(craftMissing);
			craftMissing.clear();
			return true;
		}

		return false;
	}
}
