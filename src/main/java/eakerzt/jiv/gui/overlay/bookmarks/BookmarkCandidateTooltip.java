package eakerzt.jiv.gui.overlay.bookmarks;

import eakerzt.jiv.api.gui.handlers.IGuiProperties;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.gui.JivTooltip;
import eakerzt.jiv.common.input.*;
import eakerzt.jiv.common.util.SafeIngredientUtil;
import eakerzt.jiv.gui.bookmarks.BookmarkCell;
import eakerzt.jiv.gui.input.IClickableIngredientInternal;
import eakerzt.jiv.gui.recipes.InteractiveIngredientGridTooltipComponent;
import eakerzt.jiv.gui.recipes.PinnedTooltipRenderer;
import eakerzt.jiv.gui.util.FocusUtil;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/** The same ingredient grid as recipe tooltips, pinned while the details key is held. */
final class BookmarkCandidateTooltip implements IGuiInputLayer {
	private final BookmarkOverlay.PreviewSource source;
	private final BookmarkCell<?> cell;
	private final InteractiveIngredientGridTooltipComponent grid;
	private final PinnedTooltipRenderer renderer;

	BookmarkCandidateTooltip(
			BookmarkOverlay.PreviewSource source, BookmarkCell<?> cell, int x, int y) {
		this.source = source;
		this.cell = cell;
		var runtime = Internal.getJivRuntime();
		grid =
				new InteractiveIngredientGridTooltipComponent(
						runtime.getJivHelpers().getGuiHelper(),
						cell.candidates().stream()
								.<eakerzt.jiv.api.ingredients.ITypedIngredient<?>>map(
										runtime.getIngredientManager()::normalizeTypedIngredient)
								.toList());
		grid.setSelectedIndex(cell.selectedCandidateIndex());
		renderer = new PinnedTooltipRenderer(x, y);
	}

	java.util.Optional<BookmarkPreviewTooltipController.RecipeSource> recipeSource(
			double x, double y) {
		return cell.bookmark instanceof eakerzt.jiv.gui.bookmarks.RecipeBookmark<?, ?> recipe
						&& grid.getTypedIngredientUnderMouse(x, y).isPresent()
				? java.util.Optional.of(
						new BookmarkPreviewTooltipController.RecipeSource(recipe, cell.slot))
				: java.util.Optional.empty();
	}

	boolean isSourceVisible() {
		return source.isPresentAndVisible();
	}

	@Override
	public boolean isMouseOver(double x, double y) {
		return renderer.isMouseOver(x, y);
	}

	Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double x, double y) {
		return grid.getIngredientUnderMouse(x, y);
	}

	@Override
	public void draw(GuiGraphicsExtractor graphics, int x, int y) {
		var ingredient = cell.getTypedIngredient();
		JivTooltip tooltip = new JivTooltip();
		addIngredient(tooltip, ingredient);
		tooltip.add(
				net.minecraft.network.chat.Component.translatable("jiv.bookmarks.controls.choice"));
		tooltip.add(grid);
		grid.setMousePosition(x, y);
		renderer.draw(graphics, tooltip);
		grid.getTypedIngredientUnderMouse(x, y)
				.ifPresent(
						candidate -> {
							JivTooltip detail = new JivTooltip();
							addIngredient(detail, candidate);
							graphics.nextStratum();
							detail.draw(graphics, x, y);
						});
	}

	private static <T> void addIngredient(
			JivTooltip tooltip, eakerzt.jiv.api.ingredients.ITypedIngredient<T> ingredient) {
		var manager = Internal.getJivRuntime().getIngredientManager();
		SafeIngredientUtil.getRichTooltip(
				tooltip, manager, manager.getIngredientRenderer(ingredient.getType()), ingredient);
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(
			Screen screen, IGuiProperties properties, UserInput input, IInternalKeyMappings keys) {
		if (input.isMouseButton(0) && grid.isDraggingScrollbar()) {
			if (!input.isSimulate()) grid.stopScrollbarDrag();
			return Optional.of(this);
		}
		if (input.isMouseButton(0)
				&& input.isSimulate()
				&& grid.startScrollbarDrag(input.getMouseX(), input.getMouseY()))
			return Optional.of(this);
		if (!input.isMouseButton(0) && !input.isMouseButton(1)) return Optional.empty();
		var clicked = getIngredientUnderMouse(input.getMouseX(), input.getMouseY()).findFirst();
		if (clicked.isEmpty()) return Optional.empty();
		if (!input.isSimulate()) {
			var runtime = Internal.getJivRuntime();
			var focus =
					new FocusUtil(
							runtime.getJivHelpers().getFocusFactory(),
							Internal.getClientConfigs().getClientConfig(),
							runtime.getIngredientManager());
			clicked.get()
					.show(
							runtime.getRecipesGui(),
							focus,
							input.isMouseButton(0)
									? List.of(RecipeIngredientRole.OUTPUT)
									: List.of(
											RecipeIngredientRole.INPUT,
											RecipeIngredientRole.CRAFTING_STATION));
		}
		return Optional.of(this);
	}

	@Override
	public Optional<IUserInputHandler> handleMouseScrolled(
			double x, double y, double dx, double dy) {
		return isMouseOver(x, y) && grid.mouseScrolled(dy) ? Optional.of(this) : Optional.empty();
	}

	@Override
	public Optional<IUserInputHandler> handleMouseDragged(
			double x,
			double y,
			com.mojang.blaze3d.platform.InputConstants.Key key,
			double dx,
			double dy) {
		return grid.mouseDragged(y) ? Optional.of(this) : Optional.empty();
	}

	@Override
	public void unfocus() {
		grid.stopScrollbarDrag();
	}
}
