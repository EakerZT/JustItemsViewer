package eakerzt.jiv.gui.input.handlers;

import eakerzt.jiv.api.gui.handlers.IGuiProperties;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.input.IInternalKeyMappings;
import eakerzt.jiv.common.input.IUserInputHandler;
import eakerzt.jiv.common.input.UserInput;
import eakerzt.jiv.common.input.handlers.SameElementInputHandler;
import eakerzt.jiv.gui.bookmarks.BookmarkCell;
import eakerzt.jiv.gui.bookmarks.BookmarkList;
import eakerzt.jiv.gui.bookmarks.RecipeBookmark;
import eakerzt.jiv.gui.input.CombinedRecipeFocusSource;
import eakerzt.jiv.gui.overlay.bookmarks.BookmarkOverlay;
import eakerzt.jiv.gui.overlay.bookmarks.BookmarkPreviewTooltipController;
import eakerzt.jiv.gui.recipes.RecipesGui;

import net.minecraft.client.gui.screens.Screen;

import org.lwjgl.glfw.GLFW;

import java.util.Optional;

public class BookmarkInputHandler implements IUserInputHandler {
	private final CombinedRecipeFocusSource focusSource;
	private final BookmarkList bookmarkList;
	private final BookmarkOverlay bookmarkOverlay;
	private final BookmarkPreviewTooltipController preview;
	private final RecipesGui recipesGui;

	public BookmarkInputHandler(
			CombinedRecipeFocusSource focusSource,
			BookmarkList bookmarkList,
			BookmarkOverlay bookmarkOverlay,
			BookmarkPreviewTooltipController preview,
			RecipesGui recipesGui) {
		this.focusSource = focusSource;
		this.bookmarkList = bookmarkList;
		this.bookmarkOverlay = bookmarkOverlay;
		this.preview = preview;
		this.recipesGui = recipesGui;
	}

	enum Action {
		NONE,
		INGREDIENT,
		SINGLE_OUTPUT,
		ALL_OUTPUTS
	}

	static Action action(int modifiers) {
		if ((modifiers & (GLFW.GLFW_MOD_ALT | GLFW.GLFW_MOD_SUPER)) != 0) return Action.NONE;
		if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0)
			return (modifiers & GLFW.GLFW_MOD_SHIFT) != 0
					? Action.ALL_OUTPUTS
					: Action.SINGLE_OUTPUT;
		return (modifiers & GLFW.GLFW_MOD_SHIFT) == 0 ? Action.INGREDIENT : Action.NONE;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(
			Screen screen, IGuiProperties properties, UserInput input, IInternalKeyMappings keys) {
		if (!keys.getBookmark().isActiveAndMatchesAllowingExtraModifiers(input.getKey()))
			return Optional.empty();
		Action action = action(input.getModifiers());
		if (action == Action.NONE) return Optional.empty();
		if (action != Action.INGREDIENT)
			return handleRecipeBookmark(input, keys, action == Action.ALL_OUTPUTS);
		var layout = recipesGui.getRecipeLayoutUnderMouse(input.getMouseX(), input.getMouseY());
		if (layout.isPresent() && layout.get().getRecipeLayout()
				.getSlotUnderMouse(input.getMouseX(), input.getMouseY())
				.filter(slot -> slot.slot().getRole() == RecipeIngredientRole.OUTPUT).isPresent())
			return handleRecipeBookmark(input, keys, false);
		return focusSource
				.getIngredientUnderMouse(input, keys)
				.findFirst()
				.map(
						clicked -> {
							if (!input.isSimulate())
								bookmarkList.onElementBookmarked(
										clicked.getElement(), input, bookmarkOverlay);
							return new SameElementInputHandler(this, clicked::isMouseOver);
						});
	}

	private Optional<IUserInputHandler> handleRecipeBookmark(
			UserInput input, IInternalKeyMappings keys, boolean all) {
		double x = input.getMouseX(), y = input.getMouseY();
		var pinned = preview.getRecipeSourceUnderMouse(x, y);
		if (pinned.isPresent()) {
			var source = pinned.get();
			var selected = selectBookmark(source.bookmark(), source.slot(), all);
			if (selected == null) return Optional.empty();
			if (!input.isSimulate())
				bookmarkList.toggleInGroup(
						selected, 0, 1);
			return Optional.of(new SameElementInputHandler(this, preview::isMouseOver));
		}
		// A hovered bookmark material retains its owning recipe, including input slots.
		var clicked = focusSource.getIngredientUnderMouse(input, keys).findFirst();
		if (clicked.isPresent()) {
			var element = clicked.get().getElement();
			var owner = element.getBookmark().filter(RecipeBookmark.class::isInstance);
			if (owner.isPresent()) {
				int slot = element instanceof BookmarkCell<?> cell ? cell.slot : -1;
				var selected = selectBookmark((RecipeBookmark<?, ?>) owner.get(), slot, all);
				if (selected == null) return Optional.empty();
				if (!input.isSimulate())
					bookmarkList.toggleInGroup(selected, 0, 1);
				return Optional.of(new SameElementInputHandler(this, clicked.get()::isMouseOver));
			}
		}
		if (preview.isMouseOver(x, y)) return Optional.empty();
		var underMouse = recipesGui.getRecipeLayoutUnderMouse(x, y);
		if (underMouse.isEmpty()) return Optional.empty();
		var layout = underMouse.get().getRecipeLayout();
		var slot = layout.getSlotUnderMouse(x, y);
		if (slot.isEmpty()
				|| (slot.get().slot().getRole() != RecipeIngredientRole.INPUT
						&& slot.get().slot().getRole() != RecipeIngredientRole.OUTPUT))
			return Optional.empty();
		var bookmark = underMouse.get().getRecipeBookmark();
		if (bookmark == null) return Optional.empty();
		int index = layout.getRecipeSlotsView().getSlotViews().indexOf(slot.get().slot());
		var selected =
				bookmark.selectOutputs(
						layout.getRecipeSlotsView(),
						index,
						all,
						Internal.getJivRuntime().getIngredientManager());
		if (selected == null) return Optional.empty();
		if (!input.isSimulate()) bookmarkList.toggleInGroup(selected, 0, 1);
		return Optional.of(new SameElementInputHandler(this, slot.get()::isMouseOver));
	}

	private <R> RecipeBookmark<R, ?> selectBookmark(
			RecipeBookmark<R, ?> bookmark, int slot, boolean all) {
		var runtime = Internal.getJivRuntime();
		return runtime.getRecipeManager()
				.createRecipeLayoutDrawable(
						bookmark.getRecipeCategory(),
						bookmark.getRecipe(),
						runtime.getJivHelpers().getFocusFactory().getEmptyFocusGroup())
				.map(
						layout ->
								bookmark.selectOutputs(
										layout.getRecipeSlotsView(),
										slot,
										all,
										runtime.getIngredientManager()))
				.orElse(null);
	}
}
