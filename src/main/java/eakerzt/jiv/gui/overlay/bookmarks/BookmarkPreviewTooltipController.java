package eakerzt.jiv.gui.overlay.bookmarks;

import eakerzt.jiv.api.gui.handlers.IGuiProperties;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.input.IGuiInputLayer;
import eakerzt.jiv.common.input.IInternalKeyMappings;
import eakerzt.jiv.common.input.IUserInputHandler;
import eakerzt.jiv.common.input.UserInput;
import eakerzt.jiv.common.transfer.RecipeTransferService;
import eakerzt.jiv.gui.input.IClickableIngredientInternal;
import eakerzt.jiv.gui.input.IDraggableIngredientInternal;
import eakerzt.jiv.gui.input.IPinnedTooltipHolder;
import eakerzt.jiv.gui.input.IRecipeFocusSource;
import eakerzt.jiv.gui.input.PinnedTooltipManager;
import eakerzt.jiv.gui.overlay.elements.RecipeBookmarkElement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.stream.Stream;

public class BookmarkPreviewTooltipController
		implements IGuiInputLayer, IPinnedTooltipHolder, IRecipeFocusSource {
	private final BookmarkOverlay bookmarkOverlay;
	private final RecipeTransferService recipeTransferService;
	private @Nullable BookmarkPreviewTooltip activeTooltip;
	private @Nullable BookmarkCandidateTooltip candidateTooltip;
	private @Nullable Screen lastScreen;

	public BookmarkPreviewTooltipController(
			BookmarkOverlay bookmarkOverlay, RecipeTransferService recipeTransferService) {
		this.bookmarkOverlay = bookmarkOverlay;
		this.recipeTransferService = recipeTransferService;
	}

	public record RecipeSource(eakerzt.jiv.gui.bookmarks.RecipeBookmark<?, ?> bookmark, int slot) {}

	public Optional<RecipeSource> getRecipeSourceUnderMouse(double x, double y) {
		if (candidateTooltip != null) return candidateTooltip.recipeSource(x, y);
		return activeTooltip == null ? Optional.empty() : activeTooltip.recipeSource(x, y);
	}

	public boolean isVisible() {
		return this.activeTooltip != null || candidateTooltip != null;
	}

	boolean isActive(BookmarkPreviewTooltip tooltip) {
		return this.activeTooltip == tooltip;
	}

	@Override
	public void hide() {
		if (candidateTooltip != null) {
			candidateTooltip.unfocus();
			candidateTooltip = null;
			PinnedTooltipManager.closed(this);
		}
		if (this.activeTooltip != null) {
			this.activeTooltip = null;
			PinnedTooltipManager.closed(this);
		}
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		if (candidateTooltip != null) return candidateTooltip.isMouseOver(mouseX, mouseY);
		BookmarkPreviewTooltip activeTooltip = this.activeTooltip;
		return activeTooltip != null && activeTooltip.isMouseOver(mouseX, mouseY);
	}

	@Override
	public void draw(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (candidateTooltip != null) {
			PinnedTooltipManager.draw(
					this, () -> candidateTooltip.draw(guiGraphics, mouseX, mouseY));
			return;
		}
		BookmarkPreviewTooltip activeTooltip = this.activeTooltip;
		if (activeTooltip != null) {
			PinnedTooltipManager.draw(this, () -> activeTooltip.draw(guiGraphics, mouseX, mouseY));
		}
	}

	@Override
	public void update(double mouseX, double mouseY) {
		if (!Internal.getKeyMappings().getPauseRecipeCycling().isDown()
				|| !bookmarkOverlay.isListDisplayed()) {
			hide();
			return;
		}
		Minecraft minecraft = Minecraft.getInstance();
		Screen screen = minecraft.screen;
		if (screen != this.lastScreen) {
			this.lastScreen = screen;
			hide();
			return;
		}
		if (candidateTooltip != null) {
			if (!candidateTooltip.isSourceVisible()) hide();
			return;
		}
		BookmarkPreviewTooltip activeTooltip = this.activeTooltip;
		if (activeTooltip == null) {
			open(mouseX, mouseY);
		} else if (!activeTooltip.isSourceVisible()) {
			hide();
		} else {
			activeTooltip.update();
		}
	}

	private void open(double mouseX, double mouseY) {
		var candidateSource =
				bookmarkOverlay
						.getPreviewSourcesUnderMouse(mouseX, mouseY)
						.filter(
								source ->
										source.ingredient().getElement()
														instanceof
														eakerzt.jiv.gui.bookmarks.BookmarkCell<?>
																cell
												&& cell.hasCandidates())
						.findFirst();
		if (candidateSource.isPresent()) {
			hide();
			var source = candidateSource.get();
			candidateTooltip =
					new BookmarkCandidateTooltip(
							source,
							(eakerzt.jiv.gui.bookmarks.BookmarkCell<?>)
									source.ingredient().getElement(),
							(int) mouseX,
							(int) mouseY);
			PinnedTooltipManager.opened(this);
			return;
		}
		bookmarkOverlay
				.getPreviewSourcesUnderMouse(mouseX, mouseY)
				.<BookmarkPreviewTooltip>mapMulti(
						(source, consumer) -> {
							var sourceElement = source.ingredient().getElement();
							if (sourceElement
											instanceof
											eakerzt.jiv.gui.bookmarks.BookmarkCell<?> cell
									&& cell.role
											== eakerzt.jiv.api.recipe.RecipeIngredientRole.OUTPUT
									&& cell.bookmark != null)
								sourceElement = cell.bookmark.getElement();
							if (sourceElement instanceof RecipeBookmarkElement<?, ?> element) {
								element.getInteractivePreview()
										.map(
												component ->
														new BookmarkPreviewTooltip(
																this,
																element,
																source::isPresentAndVisible,
																component,
																recipeTransferService,
																(int) mouseX,
																(int) mouseY))
										.ifPresent(consumer);
							}
						})
				.findFirst()
				.ifPresent(
						tooltip -> {
							hide();
							this.activeTooltip = tooltip;
							PinnedTooltipManager.opened(this);
						});
	}

	@Override
	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(
			double mouseX, double mouseY) {
		if (candidateTooltip != null)
			return candidateTooltip.getIngredientUnderMouse(mouseX, mouseY);
		BookmarkPreviewTooltip activeTooltip = this.activeTooltip;
		if (activeTooltip == null) {
			return Stream.empty();
		}
		return activeTooltip.getIngredientUnderMouse(mouseX, mouseY);
	}

	@Override
	public Stream<IDraggableIngredientInternal<?>> getDraggableIngredientUnderMouse(
			double mouseX, double mouseY) {
		return Stream.empty();
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(
			Screen screen,
			IGuiProperties guiProperties,
			UserInput input,
			IInternalKeyMappings keyBindings) {
		if (candidateTooltip != null)
			return candidateTooltip.handleUserInput(screen, guiProperties, input, keyBindings);
		BookmarkPreviewTooltip activeTooltip = this.activeTooltip;
		if (activeTooltip == null) {
			return Optional.empty();
		}
		return activeTooltip.handleUserInput(screen, guiProperties, input, keyBindings);
	}

	@Override
	public Optional<IUserInputHandler> handleMouseScrolled(
			double x, double y, double dx, double dy) {
		return candidateTooltip == null
				? Optional.empty()
				: candidateTooltip.handleMouseScrolled(x, y, dx, dy);
	}

	@Override
	public Optional<IUserInputHandler> handleMouseDragged(
			double x,
			double y,
			com.mojang.blaze3d.platform.InputConstants.Key key,
			double dx,
			double dy) {
		return candidateTooltip == null
				? Optional.empty()
				: candidateTooltip.handleMouseDragged(x, y, key, dx, dy);
	}

	@Override
	public void unfocus() {
		if (candidateTooltip != null) candidateTooltip.unfocus();
	}
}
