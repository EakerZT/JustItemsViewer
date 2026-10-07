package eakerzt.jiv.library.gui.recipes;

import eakerzt.jiv.api.gui.IRecipeLayoutDrawable;
import eakerzt.jiv.api.gui.drawable.IDrawable;
import eakerzt.jiv.api.gui.drawable.IDrawableAnimated;
import eakerzt.jiv.api.gui.drawable.IDrawableStatic;
import eakerzt.jiv.api.gui.drawable.IScalableDrawable;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotDrawable;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotDrawablesView;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotView;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotsView;
import eakerzt.jiv.api.gui.inputs.IJivGuiEventListener;
import eakerzt.jiv.api.gui.inputs.IJivInputHandler;
import eakerzt.jiv.api.gui.inputs.RecipeSlotUnderMouse;
import eakerzt.jiv.api.gui.widgets.IDrawableWidget;
import eakerzt.jiv.api.gui.widgets.IRecipeExtrasBuilder;
import eakerzt.jiv.api.gui.widgets.IRecipeWidget;
import eakerzt.jiv.api.gui.widgets.IScrollBoxWidget;
import eakerzt.jiv.api.gui.widgets.IScrollGridWidget;
import eakerzt.jiv.api.gui.widgets.ISlottedRecipeWidget;
import eakerzt.jiv.api.gui.widgets.ITextWidget;
import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.recipe.IFocusGroup;
import eakerzt.jiv.api.recipe.category.IRecipeCategory;
import eakerzt.jiv.api.recipe.category.extensions.IRecipeCategoryDecorator;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.gui.JivTooltip;
import eakerzt.jiv.common.gui.elements.DrawableAnimated;
import eakerzt.jiv.common.gui.elements.DrawableBlank;
import eakerzt.jiv.common.gui.elements.DrawableCombined;
import eakerzt.jiv.common.gui.elements.DrawableRecipeWidget;
import eakerzt.jiv.common.gui.elements.TextWidget;
import eakerzt.jiv.common.gui.textures.Textures;
import eakerzt.jiv.common.util.ErrorUtil;
import eakerzt.jiv.common.util.ImmutablePoint2i;
import eakerzt.jiv.common.util.ImmutableRect2i;
import eakerzt.jiv.common.util.MathUtil;
import eakerzt.jiv.common.util.LimitedLogger;
import eakerzt.jiv.library.gui.ingredients.CycleTicker;
import eakerzt.jiv.library.gui.ingredients.RecipeSlot;
import eakerzt.jiv.library.gui.recipes.layout.builder.RecipeLayoutBuilder;
import eakerzt.jiv.library.ingredients.IIngredientManagerInternal;
import eakerzt.jiv.library.gui.widgets.ScrollBoxRecipeWidget;
import eakerzt.jiv.library.gui.widgets.ScrollGridRecipeWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.context.ContextMap;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RecipeLayout<R> implements IRecipeLayoutDrawable<R>, IRecipeExtrasBuilder {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final LimitedLogger LIMITED_LOGGER = new LimitedLogger(LOGGER, Duration.ofSeconds(10));

	public static final int RECIPE_BUTTON_SIZE = 13;
	public static final int RECIPE_BUTTON_SPACING = 2;

	private final IRecipeCategory<R> recipeCategory;
	private final Collection<IRecipeCategoryDecorator<R>> recipeCategoryDecorators;
	/**
	 * Slots handled by the recipe category directly.
	 */
	private final List<IRecipeSlotDrawable> slots;
	/**
	 * All slots, including slots handled by the recipe category and widgets.
	 */
	private final IRecipeSlotsView recipeSlotsView;
	private final List<ISlottedRecipeWidget> slottedWidgets;
	private final CycleTicker cycleTicker;
	private final IFocusGroup focuses;
	private final List<IRecipeWidget> allWidgets;
	private final List<eakerzt.jiv.api.gui.widgets.IRecipeScreenExtension> screenExtensions = new ArrayList<>();
	private final R recipe;
	private final IScalableDrawable recipeBackground;
	private final int recipeBorderPadding;
	private final ImmutableRect2i recipeTransferButtonArea;
	private final @Nullable ShapelessIcon shapelessIcon;
	private final RecipeLayoutInputHandler<R> inputHandler;
	private boolean extrasCreated = false;
	private boolean displayedIngredientsUpdatePending;

	private ImmutableRect2i area;

	public static <T> Optional<IRecipeLayoutDrawable<T>> create(
		IRecipeCategory<T> recipeCategory,
		Collection<IRecipeCategoryDecorator<T>> decorators,
		T recipe,
		IFocusGroup focuses,
		IIngredientManagerInternal ingredientManager,
		IScalableDrawable recipeBackground,
		int recipeBorderPadding,
		ContextMap contextMap
	) {
		RecipeLayoutBuilder<T> builder = new RecipeLayoutBuilder<>(recipeCategory, recipe, ingredientManager, contextMap);
		try {
			recipeCategory.setRecipe(builder, recipe, focuses);
			return builder.buildRecipeLayout(focuses, decorators, recipeBackground, recipeBorderPadding)
				.<IRecipeLayoutDrawable<T>>map(layout -> layout);
		} catch (RuntimeException | LinkageError e) {
			String recipeInfo = ErrorUtil.getRecipeInfo(recipeCategory, recipe);
			LOGGER.error("Recipe crashed during Recipe Layout creation:\n{}", recipeInfo, e);
		}
		return Optional.empty();
	}

	public RecipeLayout(
		IRecipeCategory<R> recipeCategory,
		Collection<IRecipeCategoryDecorator<R>> recipeCategoryDecorators,
		R recipe,
		IScalableDrawable recipeBackground,
		int recipeBorderPadding,
		@Nullable ShapelessIcon shapelessIcon,
		ImmutablePoint2i recipeTransferButtonPos,
		List<RecipeSlot> slots,
		CycleTicker cycleTicker,
		IFocusGroup focuses
	) {
		this.recipeCategory = recipeCategory;
		this.recipeCategoryDecorators = recipeCategoryDecorators;
		this.slottedWidgets = new ArrayList<>();
		this.allWidgets = new ArrayList<>();
		this.cycleTicker = cycleTicker;
		this.focuses = focuses;
		this.inputHandler = new RecipeLayoutInputHandler<>(this);

		this.slots = new ArrayList<>(slots);
		this.recipeSlotsView = new RecipeSlotsView(List.copyOf(this.slots));
		this.recipeBorderPadding = recipeBorderPadding;
		this.area = new ImmutableRect2i(
			0,
			0,
			recipeCategory.getWidth(),
			recipeCategory.getHeight()
		);

		this.recipeTransferButtonArea = new ImmutableRect2i(
			recipeTransferButtonPos.x(),
			recipeTransferButtonPos.y(),
			RECIPE_BUTTON_SIZE,
			RECIPE_BUTTON_SIZE
		);

		this.recipe = recipe;
		this.recipeBackground = recipeBackground;
		this.shapelessIcon = shapelessIcon;

		for (RecipeSlot slot : slots) {
			slot.setDisplayOverridesChangedListener(this::onDisplayOverridesChanged);
		}
		updateDisplayedIngredients(false);
	}

	public void ensureRecipeExtrasAreCreated() {
		if (!extrasCreated) {
			extrasCreated = true;
			recipeCategory.createRecipeExtras(this, recipe, focuses);
		}
	}

	@Override
	public void addScreenExtension(eakerzt.jiv.api.gui.widgets.IRecipeScreenExtension extension) {
		this.screenExtensions.add(java.util.Objects.requireNonNull(extension));
	}

	@Override
	public List<eakerzt.jiv.api.gui.widgets.IRecipeScreenExtension> getScreenExtensions() {
		ensureRecipeExtrasAreCreated();
		return List.copyOf(screenExtensions);
	}

	@Override
	public void setPosition(int posX, int posY) {
		area = area.setPosition(posX, posY);
	}

	@Override
	public void drawRecipe(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		ensureRecipeExtrasAreCreated();

		recipeBackground.draw(guiGraphics, getRectWithBorder());

		final double recipeMouseX = mouseX - area.getX();
		final double recipeMouseY = mouseY - area.getY();

		IRecipeSlotsView recipeCategorySlotsView = () -> Collections.unmodifiableList(slots);
		RecipeSlotUnderMouse hoveredSlotResult = getSlotUnderMouse(mouseX, mouseY).orElse(null);

		var poseStack = guiGraphics.pose();
		poseStack.pushMatrix();
		{
			poseStack.translate(area.getX(), area.getY());

			// defensive push/pop to protect against recipe categories changing the last pose
			poseStack.pushMatrix();
			{
				recipeCategory.draw(recipe, recipeCategorySlotsView, guiGraphics, recipeMouseX, recipeMouseY);
				for (IRecipeSlotDrawable slot : slots) {
					boolean hovered = hoveredSlotResult != null && hoveredSlotResult.slot() == slot;
					slot.draw(guiGraphics, hovered);
				}
				RecipeWidgetRenderer.forEachWidget(allWidgets, recipeMouseX, recipeMouseY, (widget, position, relativeMouseX, relativeMouseY) -> {
					poseStack.pushMatrix();
					try {
						poseStack.translate(position.x(), position.y());
						widget.drawWidget(guiGraphics, relativeMouseX, relativeMouseY);
					} finally {
						poseStack.popMatrix();
					}
				});
			}
			poseStack.popMatrix();

			for (IRecipeCategoryDecorator<R> decorator : recipeCategoryDecorators) {
				// defensive push/pop to protect against recipe category decorators changing the last pose
				poseStack.pushMatrix();
				{
					decorator.draw(recipe, recipeCategory, recipeCategorySlotsView, guiGraphics, recipeMouseX, recipeMouseY);
				}
				poseStack.popMatrix();
			}

			if (shapelessIcon != null) {
				shapelessIcon.draw(guiGraphics);
			}
		}
		poseStack.popMatrix();
	}

	@Override
	public void drawOverlays(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		ensureRecipeExtrasAreCreated();

		final int recipeMouseX = mouseX - area.getX();
		final int recipeMouseY = mouseY - area.getY();

		IRecipeSlotsView recipeCategorySlotsView = () -> Collections.unmodifiableList(slots);
		RecipeSlotUnderMouse hoveredSlotResult = getSlotUnderMouse(mouseX, mouseY).orElse(null);

		var poseStack = guiGraphics.pose();
		if (hoveredSlotResult != null) {
			IRecipeSlotDrawable hoveredSlot = hoveredSlotResult.slot();
			hoveredSlot.drawTooltip(guiGraphics, mouseX, mouseY);
		} else if (isMouseOver(mouseX, mouseY)) {
			JivTooltip tooltip = new JivTooltip();
			try {
				recipeCategory.getTooltip(tooltip, recipe, recipeCategorySlotsView, recipeMouseX, recipeMouseY);
				for (IRecipeCategoryDecorator<R> decorator : recipeCategoryDecorators) {
					decorator.decorateTooltips(tooltip, recipe, recipeCategory, recipeCategorySlotsView, recipeMouseX, recipeMouseY);
				}
			} catch (RuntimeException e) {
				LIMITED_LOGGER.log(
					Level.ERROR,
					"recipe.category.tooltip.crash",
					logger -> {
						logger.error(
							"Error while getting tooltip from recipe:\n{}",
							ErrorUtil.getRecipeInfo(recipeCategory, recipe),
							e
						);
					}
				);
			}

			RecipeWidgetTooltipDispatcher.addWidgetTooltips(
				tooltip,
				allWidgets,
				recipeMouseX,
				recipeMouseY,
				area.width(),
				area.height()
			);

			if (tooltip.isEmpty() && shapelessIcon != null) {
				if (shapelessIcon.isMouseOver(recipeMouseX, recipeMouseY)) {
					shapelessIcon.addTooltip(tooltip);
				}
			}
			tooltip.draw(guiGraphics, mouseX, mouseY);
		}
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return MathUtil.contains(area, mouseX, mouseY);
	}

	@Override
	public Rect2i getRect() {
		return area.toMutable();
	}

	@Override
	public Rect2i getRectWithBorder() {
		return area.expandBy(recipeBorderPadding).toMutable();
	}

	@Override
	public <T> Optional<T> getIngredientUnderMouse(int mouseX, int mouseY, IIngredientType<T> ingredientType) {
		return getSlotUnderMouse(mouseX, mouseY)
			.map(RecipeSlotUnderMouse::slot)
			.flatMap(slot -> slot.getDisplayedIngredient(ingredientType));
	}

	@Override
	public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY) {
		ensureRecipeExtrasAreCreated();
		final double recipeMouseX = mouseX - area.getX();
		final double recipeMouseY = mouseY - area.getY();

		for (ISlottedRecipeWidget widget : slottedWidgets) {
			ScreenPosition position = widget.getPosition();
			double relativeMouseX = recipeMouseX - position.x();
			double relativeMouseY = recipeMouseY - position.y();
			Optional<RecipeSlotUnderMouse> slotResult = widget.getSlotUnderMouse(relativeMouseX, relativeMouseY);
			if (slotResult.isPresent()) {
				return slotResult
					.map(slot -> slot.addOffset(area.x(), area.y()));
			}
		}
		for (IRecipeSlotDrawable slot : slots) {
			if (slot.isMouseOver(recipeMouseX, recipeMouseY)) {
				return Optional.of(new RecipeSlotUnderMouse(slot, area.getScreenPosition()));
			}
		}
		return Optional.empty();
	}

	@Override
	public IRecipeCategory<R> getRecipeCategory() {
		return recipeCategory;
	}

	@Override
	public Rect2i getSideButtonArea(int buttonIndex) {
		Rect2i buttonArea = recipeTransferButtonArea.toMutable();
		if (buttonIndex > 0) {
			int maxRows = (getRectWithBorder().getHeight() + RECIPE_BUTTON_SPACING) / (buttonArea.getHeight() + RECIPE_BUTTON_SPACING);
			int xIndex = buttonIndex / maxRows;
			int yIndex = buttonIndex % maxRows;
			int xOffset = xIndex * (buttonArea.getWidth() + RECIPE_BUTTON_SPACING);
			int yOffset = yIndex * (buttonArea.getHeight() + RECIPE_BUTTON_SPACING);

			buttonArea.setX(buttonArea.getX() + xOffset);
			buttonArea.setY(buttonArea.getY() - yOffset);
		}
		return buttonArea;
	}

	@Override
	public IRecipeSlotsView getRecipeSlotsView() {
		return recipeSlotsView;
	}

	@Override
	public IRecipeSlotDrawablesView getRecipeSlots() {
		ensureRecipeExtrasAreCreated();
		return () -> Collections.unmodifiableList(slots);
	}

	@Override
	public R getRecipe() {
		return recipe;
	}

	@Override
	public IJivInputHandler getInputHandler() {
		return inputHandler;
	}

	@Override
	public void tick() {
		ensureRecipeExtrasAreCreated();
		for (IRecipeWidget widget : allWidgets) {
			widget.tick();
		}
		boolean ingredientsCycled = cycleTicker.tick();
		if (ingredientsCycled || displayedIngredientsUpdatePending) {
			updateDisplayedIngredients(ingredientsCycled);
		}
	}

	private void onDisplayOverridesChanged() {
		displayedIngredientsUpdatePending = true;
	}

	private void updateDisplayedIngredients(boolean clearDisplayOverrides) {
		try {
			if (clearDisplayOverrides) {
				for (IRecipeSlotDrawable slot : slots) {
					slot.clearDisplayOverrides();
				}
			}
			recipeCategory.onDisplayedIngredientsUpdate(
				recipe,
				Collections.unmodifiableList(slots),
				focuses
			);
		} finally {
			// Ignore notifications caused by the category's own update to avoid a reentrant update loop.
			displayedIngredientsUpdatePending = false;
		}
	}

	@Override
	public IDrawableWidget addDrawableWidget(IDrawable drawable) {
		ErrorUtil.checkNotNull(drawable, "drawable");
		DrawableRecipeWidget widget = new DrawableRecipeWidget(drawable);
		addWidget(widget);
		return widget;
	}

	@Override
	public IDrawableWidget addTooltipArea(int xPos, int yPos, int width, int height) {
		if (width < 0) {
			throw new IllegalArgumentException("width must be non-negative");
		}
		if (height < 0) {
			throw new IllegalArgumentException("height must be non-negative");
		}
		DrawableRecipeWidget widget = new DrawableRecipeWidget(new DrawableBlank(width, height));
		widget.setPosition(xPos, yPos);
		addWidget(widget);
		return widget;
	}

	@Override
	public void addWidget(IRecipeWidget widget) {
		this.allWidgets.add(widget);
		if (widget instanceof ISlottedRecipeWidget slottedWidget) {
			this.slottedWidgets.add(slottedWidget);
		}
	}

	@Override
	public void addSlottedWidget(ISlottedRecipeWidget widget, List<IRecipeSlotDrawable> slots) {
		this.allWidgets.add(widget);
		this.slottedWidgets.add(widget);
		this.slots.removeAll(slots);
	}

	@Override
	public void addInputHandler(IJivInputHandler inputHandler) {
		this.inputHandler.addInputHandler(inputHandler);
	}

	@Override
	public void addGuiEventListener(IJivGuiEventListener guiEventListener) {
		this.inputHandler.addGuiEventListener(guiEventListener);
	}

	@Override
	public IScrollBoxWidget addScrollBoxWidget(int width, int height, int xPos, int yPos) {
		ScrollBoxRecipeWidget widget = new ScrollBoxRecipeWidget(width, height, xPos, yPos);
		addWidget(widget);
		addInputHandler(widget);
		return widget;
	}

	@Override
	public IScrollGridWidget addScrollGridWidget(List<IRecipeSlotDrawable> slots, int columns, int visibleRows) {
		ScrollGridRecipeWidget widget = ScrollGridRecipeWidget.create(slots, columns, visibleRows);
		addSlottedWidget(widget, slots);
		addInputHandler(widget);
		return widget;
	}

	@Override
	public IDrawableWidget addRecipeArrowWidget() {
		Textures textures = Internal.getTextures();
		return addDrawableWidget(textures.getRecipeArrow());
	}

	@Override
	public IDrawableWidget addRecipePlusSignWidget() {
		Textures textures = Internal.getTextures();
		return addDrawableWidget(textures.getRecipePlusSign());
	}

	@Override
	public IDrawableWidget addAnimatedRecipeArrowWidget(int ticksPerCycle) {
		Textures textures = Internal.getTextures();
		IDrawableStatic recipeArrowFilled = textures.getRecipeArrowFilled();
		IDrawable animatedFill = new DrawableAnimated(recipeArrowFilled, ticksPerCycle, IDrawableAnimated.StartDirection.LEFT, false);
		IDrawable drawable = new DrawableCombined(textures.getRecipeArrow(), animatedFill);
		return addDrawableWidget(drawable);
	}

	@Override
	public IDrawableWidget addAnimatedRecipeFlameWidget(int cookTime) {
		Textures textures = Internal.getTextures();
		IDrawableStatic flameIcon = textures.getFlameIcon();
		IDrawableAnimated animatedFill = new DrawableAnimated(flameIcon, cookTime, IDrawableAnimated.StartDirection.TOP, true);
		IDrawable drawable = new DrawableCombined(textures.getFlameEmptyIcon(), animatedFill);
		return addDrawableWidget(drawable);
	}

	@Override
	public ITextWidget addText(List<FormattedText> text, int maxWidth, int maxHeight) {
		TextWidget textWidget = new TextWidget(text, 0, 0, maxWidth, maxHeight);
		addWidget(textWidget);
		return textWidget;
	}

	private record RecipeSlotsView(@Unmodifiable List<IRecipeSlotView> slots) implements IRecipeSlotsView {
		@Override
		public @Unmodifiable List<IRecipeSlotView> getSlotViews() {
			return slots;
		}
	}
}
