package eakerzt.jiv.gui.bookmarks;

import eakerzt.jiv.api.gui.IRecipeLayoutDrawable;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotView;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotsView;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.recipe.category.IRecipeCategory;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.transfer.RecipeTransferService;
import eakerzt.jiv.gui.overlay.elements.IElement;
import eakerzt.jiv.gui.overlay.elements.RecipeBookmarkElement;

import net.minecraft.resources.Identifier;

import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class RecipeBookmark<R, I> implements IBookmark {
	private final IElement<I> element;
	private final IRecipeCategory<R> recipeCategory;
	private final R recipe;
	private final Identifier recipeUid;
	private final ITypedIngredient<I> displayIngredient;
	private final boolean displayIsOutput;
	private boolean visible = true;
	private final RecipeTransferService recipeTransferService;
	private final int outputSlot;
	private final int outputChoice;

	@Nullable
	public static <T> RecipeBookmark<T, ?> create(
			IRecipeLayoutDrawable<T> recipeLayoutDrawable,
			IIngredientManager ingredientManager,
			RecipeTransferService recipeTransferService) {
		T recipe = recipeLayoutDrawable.getRecipe();
		IRecipeCategory<T> recipeCategory = recipeLayoutDrawable.getRecipeCategory();
		Identifier recipeUid = recipeCategory.getIdentifier(recipe);
		if (recipeUid == null) {
			return null;
		}

		IRecipeSlotsView recipeSlotsView = recipeLayoutDrawable.getRecipeSlotsView();
		{
			ITypedIngredient<?> output = findFirst(recipeSlotsView, RecipeIngredientRole.OUTPUT);
			if (output != null) {
				output = ingredientManager.normalizeTypedIngredient(output);
				return new RecipeBookmark<>(
						recipeCategory, recipe, recipeUid, output, true, recipeTransferService);
			}
		}

		{
			ITypedIngredient<?> input = findFirst(recipeSlotsView, RecipeIngredientRole.INPUT);
			if (input != null) {
				input = ingredientManager.normalizeTypedIngredient(input);
				return new RecipeBookmark<>(
						recipeCategory, recipe, recipeUid, input, false, recipeTransferService);
			}
		}

		return null;
	}

	@Nullable
	private static ITypedIngredient<?> findFirst(
			IRecipeSlotsView slotsView, RecipeIngredientRole role) {
		for (IRecipeSlotView slotView : slotsView.getSlotViews()) {
			if (slotView.getRole() != role) {
				continue;
			}
			Optional<ITypedIngredient<?>> ingredient =
					slotView.getDisplayedIngredient()
							.or(() -> slotView.getAllIngredients().findFirst());
			if (ingredient.isPresent()) {
				return ingredient.get();
			}
		}
		return null;
	}

	public RecipeBookmark(
			IRecipeCategory<R> recipeCategory,
			R recipe,
			Identifier recipeUid,
			ITypedIngredient<I> displayIngredient,
			boolean displayIsOutput,
			RecipeTransferService recipeTransferService) {
		this(
				recipeCategory,
				recipe,
				recipeUid,
				displayIngredient,
				displayIsOutput,
				recipeTransferService,
				-1,
				0);
	}

	private RecipeBookmark(
			IRecipeCategory<R> recipeCategory,
			R recipe,
			Identifier recipeUid,
			ITypedIngredient<I> displayIngredient,
			boolean displayIsOutput,
			RecipeTransferService recipeTransferService,
			int outputSlot,
			int outputChoice) {
		this.recipeTransferService = recipeTransferService;
		this.outputSlot = outputSlot;
		this.outputChoice = outputChoice;
		this.recipeCategory = recipeCategory;
		this.recipe = recipe;
		this.recipeUid = recipeUid;
		this.displayIngredient = displayIngredient;
		this.element = new RecipeBookmarkElement<>(this, recipeTransferService);
		this.displayIsOutput = displayIsOutput;
	}

	/** -1 retains every output; otherwise the original recipe slot and its chosen candidate. */
	public int getOutputSlot() {
		return outputSlot;
	}

	public int getOutputChoice() {
		return outputChoice;
	}

	public <O> RecipeBookmark<R, O> withOutputSelection(
			int slot, int choice, ITypedIngredient<O> output) {
		if (slot < -1 || choice < 0)
			throw new IllegalArgumentException("Invalid recipe output selection");
		return new RecipeBookmark<>(
				recipeCategory,
				recipe,
				recipeUid,
				output,
				true,
				recipeTransferService,
				slot,
				choice);
	}

	public @Nullable RecipeBookmark<R, ?> selectOutputs(
			IRecipeSlotsView slotsView, int hoveredSlot, boolean all, IIngredientManager manager) {
		var slots = slotsView.getSlotViews();
		IRecipeSlotView selected =
				hoveredSlot >= 0 && hoveredSlot < slots.size() ? slots.get(hoveredSlot) : null;
		if (all || selected == null || selected.getRole() != RecipeIngredientRole.OUTPUT) {
			selected =
					slots.stream()
							.filter(slot -> slot.getRole() == RecipeIngredientRole.OUTPUT)
							.filter(
									slot ->
											slot.getDisplayedIngredient().isPresent()
													|| slot.getAllIngredients()
															.findAny()
															.isPresent())
							.findFirst()
							.orElse(null);
		}
		if (selected == null) {
			// Recipes without outputs can still be recorded in all-output mode.
			return all
					? new RecipeBookmark<>(
							recipeCategory,
							recipe,
							recipeUid,
							displayIngredient,
							displayIsOutput,
							recipeTransferService)
					: null;
		}
		var selectedSlot = selected;
		var ingredient =
				selectedSlot
						.getDisplayedIngredient()
						.or(() -> selectedSlot.getAllIngredients().findFirst());
		if (ingredient.isEmpty()) return null;
		int choice = Math.max(0, selected.getAllIngredients().toList().indexOf(ingredient.get()));
		return withOutputSelection(
				all ? -1 : slots.indexOf(selected),
				all ? 0 : choice,
				manager.normalizeTypedIngredient(ingredient.get()));
	}

	@Override
	public BookmarkType getType() {
		return BookmarkType.RECIPE;
	}

	public IRecipeCategory<R> getRecipeCategory() {
		return recipeCategory;
	}

	public R getRecipe() {
		return recipe;
	}

	public ITypedIngredient<I> getDisplayIngredient() {
		return displayIngredient;
	}

	public boolean isDisplayIsOutput() {
		return displayIsOutput;
	}

	@Override
	public IElement<?> getElement() {
		return element;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean sameRecipe(RecipeBookmark<?, ?> other) {
		return recipeUid.equals(other.recipeUid)
				&& recipeCategory.getRecipeType().equals(other.recipeCategory.getRecipeType());
	}

	@Override
	public int hashCode() {
		return Objects.hash(recipeUid, recipeCategory.getRecipeType(), outputSlot, outputChoice);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RecipeBookmark<?, ?> recipeBookmark) {
			return recipeBookmark.recipeUid.equals(recipeUid)
					&& recipeCategory
							.getRecipeType()
							.equals(recipeBookmark.recipeCategory.getRecipeType())
					&& outputSlot == recipeBookmark.outputSlot
					&& outputChoice == recipeBookmark.outputChoice;
		}
		return false;
	}

	@Override
	public String toString() {
		return "RecipeBookmark{"
				+ "recipeCategory="
				+ recipeCategory.getRecipeType()
				+ ", recipe="
				+ recipe
				+ ", recipeUid="
				+ recipeUid
				+ ", displayIngredient="
				+ displayIngredient
				+ ", visible="
				+ visible
				+ '}';
	}

	public <T> boolean isRecipe(IRecipeType<T> otherType, T otherRecipe) {
		IRecipeType<R> recipeType = recipeCategory.getRecipeType();
		if (recipeType.equals(otherType)) {
			Class<? extends R> recipeClass = recipeType.getRecipeClass();
			if (recipeClass.isInstance(otherRecipe)) {
				R castRecipe = recipeClass.cast(otherRecipe);
				Identifier otherUid = recipeCategory.getIdentifier(castRecipe);
				return recipeUid.equals(otherUid);
			}
		}
		return false;
	}
}
