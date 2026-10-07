package eakerzt.jiv.library.gui.ingredients;

import com.mojang.datafixers.util.Either;
import eakerzt.jiv.api.gui.builder.IIngredientAcceptor;
import eakerzt.jiv.api.gui.builder.ITooltipBuilder;
import eakerzt.jiv.api.gui.drawable.IDrawable;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotDrawable;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotView;
import eakerzt.jiv.api.ingredients.IIngredientHelper;
import eakerzt.jiv.api.ingredients.IIngredientRenderer;
import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.IFocusGroup;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.config.IClientConfig;
import eakerzt.jiv.common.gui.JivTooltip;
import eakerzt.jiv.common.gui.RecipeSlotOptionsTooltipComponent;
import eakerzt.jiv.common.gui.elements.OffsetDrawable;
import eakerzt.jiv.common.gui.textures.Textures;
import eakerzt.jiv.common.platform.IPlatformRenderHelper;
import eakerzt.jiv.common.platform.IPlatformScreenHelper;
import eakerzt.jiv.common.platform.Services;
import eakerzt.jiv.common.util.ImmutableRect2i;
import eakerzt.jiv.common.util.MathUtil;
import eakerzt.jiv.common.util.SafeIngredientUtil;
import eakerzt.jiv.common.util.function.LazySupplier;
import eakerzt.jiv.library.ingredients.SlotDisplayData;
import eakerzt.jiv.library.ingredients.SlotDisplayInfo;
import eakerzt.jiv.library.ingredients.IIngredientManagerInternal;
import eakerzt.jiv.library.ingredients.SlotIngredient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.context.ContextMap;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class RecipeSlot implements IRecipeSlotView, IRecipeSlotDrawable {
	private final RecipeIngredientRole role;
	private final RecipeSlotIngredients ingredients;
	private final ICycler cycler;
	private final List<IRecipeSlotRichTooltipCallback> tooltipCallbacks;
	private final @Nullable RendererOverrides rendererOverrides;
	private final @Nullable OffsetDrawable background;
	private final @Nullable IDrawable overlay;
	private final @Nullable String slotName;
	private final LazySupplier<Optional<TagKey<?>>> tagKey;
	// Snapshot visibility when the tooltip is first prepared.
	private @Nullable List<ITypedIngredient<?>> visibleCandidates;
	private @Nullable TooltipData tooltipData;
	private Runnable displayOverridesChangedListener = () -> {};
	private ImmutableRect2i rect;
	private boolean showFluidAmount;
	private eakerzt.jiv.library.render.RecipeSlotDecorations recipeDecorations = eakerzt.jiv.library.render.RecipeSlotDecorations.NONE;
	public void setRecipeDecorations(eakerzt.jiv.library.render.RecipeSlotDecorations decorations) {
		this.recipeDecorations = java.util.Objects.requireNonNull(decorations);
	}

	public void setShowFluidAmount(boolean showFluidAmount) { this.showFluidAmount = showFluidAmount; }

	public java.util.OptionalLong getDisplayedFluidAmount() {
		if (!showFluidAmount) return java.util.OptionalLong.empty();
		return getDisplayedIngredient().map(eakerzt.jiv.library.render.FluidAmountRenderer::getAmount)
			.orElseGet(java.util.OptionalLong::empty);
	}

	public RecipeSlot(
		IIngredientManagerInternal ingredientManager,
		RecipeIngredientRole role,
		ImmutableRect2i rect,
		ICycler cycler,
		List<IRecipeSlotRichTooltipCallback> tooltipCallbacks,
		List<? extends @Nullable SlotIngredient<?>> allIngredients,
		@Nullable List<? extends @Nullable SlotIngredient<?>> focusedIngredients,
		IFocusGroup focusGroup,
		@Nullable OffsetDrawable background,
		@Nullable IDrawable overlay,
		@Nullable String slotName,
		@Nullable RendererOverrides rendererOverrides,
		ContextMap contextMap
	) {
		this.ingredients = new RecipeSlotIngredients(
			ingredientManager,
			contextMap,
			role,
			allIngredients,
			focusedIngredients,
			focusGroup,
			this::onDisplayOverridesChanged
		);
		this.background = background;
		this.overlay = overlay;
		this.slotName = slotName;
		this.rendererOverrides = rendererOverrides;
		this.role = role;
		this.rect = rect;
		this.cycler = cycler;
		this.tooltipCallbacks = tooltipCallbacks;
		this.tagKey = new LazySupplier<>(this::calculateTagKey);
	}

	@Override
	public Stream<ITypedIngredient<?>> getAllIngredients() {
		return ingredients.getAllIngredients();
	}

	@Override
	@Unmodifiable
	public List<@Nullable ITypedIngredient<?>> getAllIngredientsList() {
		return ingredients.getAllIngredientsList();
	}

	@Override
	public boolean isEmpty() {
		return ingredients.isEmpty();
	}

	@Override
	public Optional<ITypedIngredient<?>> getDisplayedIngredient() {
		return getDisplayedSlotIngredient()
			.map(SlotIngredient::typedIngredient);
	}

	@Override
	public Stream<ITypedIngredient<?>> getDisplayedIngredients() {
		if (!ingredients.hasDisplayOverrides()) {
			if (visibleCandidates != null) {
				return visibleCandidates.stream();
			}
			return ingredients.getVisibleTypedIngredients();
		}
		return getDisplayedSlotIngredient()
			.stream()
			.flatMap(displayed -> {
				if (tooltipData != null && tooltipData.displayGroup() == displayed.slotDisplayData()) {
					return tooltipData.candidates().stream();
				}
				return ingredients.getVisibleTypedIngredientsInDisplayGroup(displayed);
			});
	}

	private Optional<SlotIngredient<?>> getDisplayedSlotIngredient() {
		IClientConfig clientConfig = Internal.getClientConfigs().getClientConfig();
		if (!clientConfig.recipeSlotCyclingEnabled().get()) {
			return ingredients.getFirstDisplayedIngredient();
		}
		return ingredients.getDisplayedIngredient(cycler);
	}

	@Override
	public Optional<String> getSlotName() {
		return Optional.ofNullable(this.slotName);
	}

	@Override
	public RecipeIngredientRole getRole() {
		return role;
	}

	@Override
	public void drawHighlight(GuiGraphicsExtractor guiGraphics, int color) {
		int x = this.rect.getX();
		int y = this.rect.getY();
		int width = this.rect.getWidth();
		int height = this.rect.getHeight();

		guiGraphics.fillGradient(
			x,
			y,
			x + width,
			y + height,
			color,
			color
		);
	}

	private <T> void addIngredientTooltip(ITooltipBuilder tooltip, SlotIngredient<T> slotIngredient) {
		IIngredientManager ingredientManager = Internal.getJivRuntime().getIngredientManager();
		ITypedIngredient<T> typedIngredient = slotIngredient.typedIngredient();
		TooltipData tooltipData = getTooltipData(ingredientManager, slotIngredient);

		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientRenderer<T> ingredientRenderer = getIngredientRenderer(ingredientType);
		SafeIngredientUtil.getRichTooltip(tooltip, ingredientManager, ingredientRenderer, typedIngredient);
		addSlotDisplayTooltip(tooltip, slotIngredient);
		addTagNameTooltip(tooltip, tooltipData);
		IClientConfig clientConfig = Internal.getClientConfigs().getClientConfig();
		if (clientConfig.tagContentTooltipEnabled().get() && tooltipData.candidates().size() > 1) {
			tooltip.add(tooltipData.grid().get());
		}
		if (tooltipData.candidates().size() > 1) {
			var pauseRecipeCycling = Internal.getKeyMappings().getPauseRecipeCycling();
			if (!pauseRecipeCycling.isUnbound() && !pauseRecipeCycling.isDown()) {
				tooltip.add(new RecipeSlotOptionsTooltipComponent(pauseRecipeCycling));
			}
		}
	}

	@Override
	public void addTooltip(ITooltipBuilder tooltip) {
		getDisplayedSlotIngredient()
			.ifPresent(ingredient -> {
				addIngredientTooltip(tooltip, ingredient);
				recipeDecorations.addTooltip(tooltip);
			});
		for (IRecipeSlotRichTooltipCallback tooltipCallback : tooltipCallbacks) {
			tooltipCallback.onRichTooltip(this, tooltip);
		}
	}

	private static void addSlotDisplayTooltip(
		ITooltipBuilder tooltip,
		SlotIngredient<?> slotIngredient
	) {
		Optional.ofNullable(slotIngredient.slotDisplayData())
			.map(SlotDisplayData::info)
			.flatMap(SlotDisplayInfo::tooltipHeader)
			.ifPresent(tooltipHeader -> tooltip.getLines().addFirst(Either.left(tooltipHeader)));
	}

	private static void addTagNameTooltip(ITooltipBuilder tooltip, TooltipData tooltipData) {
		if (tooltipData.displayGroupSize() == 0) {
			return;
		}

		IClientConfig clientConfig = Internal.getClientConfigs().getClientConfig();
		if (clientConfig.hideSingleTagContentTooltipEnabled().get() && tooltipData.displayGroupSize() == 1) {
			return;
		}

		tooltipData.tagKey().get()
			.ifPresent(tagKeyEquivalent -> {
				String registryName = tagKeyEquivalent.registry().identifier().getPath()
					.replace('_', ' ');
				tooltip.add(
					Component.translatable("jiv.tooltip.recipe.tag", StringUtils.capitalize(registryName))
						.withStyle(ChatFormatting.GRAY)
				);
				IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
				Component tagName = renderHelper.getName(tagKeyEquivalent);
				tooltip.add(
					tagName.copy().withStyle(ChatFormatting.GRAY)
				);
			});
	}

	@Override
	public Optional<TagKey<?>> getTagKey() {
		return this.tagKey.get();
	}

	private Optional<TagKey<?>> calculateTagKey() {
		IIngredientManager ingredientManager = Internal.getJivRuntime().getIngredientManager();
		if (!ingredients.hasDisplayOverrides()) {
			List<ITypedIngredient<?>> allIngredients = ingredients.getAllIngredients().toList();
			return ingredients.getSingleDisplayGroupTagKey(() -> getTagKeyEquivalent(ingredientManager, allIngredients));
		}
		return getDisplayedSlotIngredient()
			.flatMap(displayed -> {
				List<ITypedIngredient<?>> displayGroup = ingredients.getCandidateIngredientsInDisplayGroup(displayed).toList();
				return ingredients.getDisplayGroupTagKey(
					displayed,
					() -> getTagKeyEquivalent(ingredientManager, displayGroup)
				);
			});
	}

	private static Optional<TagKey<?>> getTagKeyEquivalent(
		IIngredientManager ingredientManager,
		List<ITypedIngredient<?>> ingredients
	) {
		return ingredients.stream()
			.findFirst()
			.flatMap(first -> getTagKeyEquivalent(ingredientManager, ingredients, first));
	}

	private static <T> Optional<TagKey<?>> getTagKeyEquivalent(
		IIngredientManager ingredientManager,
		List<ITypedIngredient<?>> allIngredients,
		ITypedIngredient<T> first
	) {
		IIngredientType<T> ingredientType = first.getType();
		List<T> ingredients = allIngredients.stream()
			.map(ingredient -> ingredient.getIngredient(ingredientType))
			.flatMap(Optional::stream)
			.toList();
		if (ingredients.size() != allIngredients.size()) {
			return Optional.empty();
		}
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		return ingredientHelper.getTagKeyEquivalent(ingredients);
	}

	private static <T> Optional<TagKey<?>> getTagKeyEquivalent(
		IIngredientManager ingredientManager,
		List<ITypedIngredient<?>> allIngredients,
		SlotIngredient<T> ingredient
	) {
		if (allIngredients.isEmpty()) {
			return Optional.empty();
		}

		ITypedIngredient<T> typedIngredient = ingredient.typedIngredient();
		IIngredientType<T> ingredientType = typedIngredient.getType();
		List<T> ingredients = allIngredients.stream()
			.map(candidate -> candidate.getIngredient(ingredientType))
			.flatMap(Optional::stream)
			.toList();
		if (ingredients.size() != allIngredients.size()) {
			return Optional.empty();
		}
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		SlotDisplayData<T> slotDisplayData = ingredient.slotDisplayData();
		if (slotDisplayData == null) {
			return ingredientHelper.getTagKeyEquivalent(ingredients);
		}
		return slotDisplayData.info()
			.tagKeyOrElse(() -> ingredientHelper.getTagKeyEquivalent(ingredients));
	}

	private TooltipData getTooltipData(
		IIngredientManager ingredientManager,
		SlotIngredient<?> displayed
	) {
		if (tooltipData == null || tooltipData.displayGroup() != displayed.slotDisplayData()) {
			List<ITypedIngredient<?>> displayGroup = getVisibleIngredientsInDisplayGroup(displayed);
			List<ITypedIngredient<?>> candidates;
			if (ingredients.hasDisplayOverrides()) {
				candidates = displayGroup;
			} else {
				if (visibleCandidates == null) {
					visibleCandidates = ingredients.getVisibleTypedIngredients().toList();
				}
				candidates = visibleCandidates;
			}
			tooltipData = new TooltipData(
				displayed.slotDisplayData(),
				candidates,
				displayGroup.size(),
				new LazySupplier<>(() -> getTagKeyEquivalent(ingredientManager, displayGroup, displayed)),
				new LazySupplier<>(() -> new TagContentTooltipComponent(ingredientManager, candidates))
			);
		}
		return tooltipData;
	}

	private void invalidateTooltipCache() {
		visibleCandidates = null;
		tooltipData = null;
	}

	private record TooltipData(
		@Nullable SlotDisplayData<?> displayGroup,
		List<ITypedIngredient<?>> candidates,
		int displayGroupSize,
		LazySupplier<Optional<TagKey<?>>> tagKey,
		LazySupplier<TagContentTooltipComponent> grid
	) {
	}

	private List<ITypedIngredient<?>> getVisibleIngredientsInDisplayGroup(SlotIngredient<?> displayed) {
		return ingredients.getVisibleTypedIngredientsInDisplayGroup(displayed)
			.toList();
	}

	private boolean hasCandidates() {
		return getDisplayedIngredients()
			.limit(2)
			.count() > 1;
	}

	private <T> IIngredientRenderer<T> getIngredientRenderer(IIngredientType<T> ingredientType) {
		return Optional.ofNullable(rendererOverrides)
			.flatMap(r -> r.getIngredientRenderer(ingredientType))
			.orElseGet(() -> {
				IIngredientManager ingredientManager = Internal.getJivRuntime().getIngredientManager();
				return ingredientManager.getIngredientRenderer(ingredientType);
			});
	}

	@Override
	public void draw(GuiGraphicsExtractor guiGraphics, boolean hovered) {
		final int x = this.rect.getX();
		final int y = this.rect.getY();

		if (background != null) {
			background.draw(guiGraphics, x, y);
		}

		if (hovered) {
			IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
			drawHighlight(guiGraphics, screenHelper.getSlotHighlightBackSprite());
		}

		Optional<SlotIngredient<?>> displayedIngredient = getDisplayedSlotIngredient();
		displayedIngredient
			.map(SlotIngredient::typedIngredient)
			.ifPresent(ingredient -> drawIngredient(guiGraphics, ingredient, x, y));

		if (overlay != null) {
			overlay.draw(guiGraphics, x, y);
		}

		getDisplayedFluidAmount().ifPresent(amount -> eakerzt.jiv.library.render.FluidAmountRenderer.draw(
			guiGraphics, amount, x, y, rect.getWidth(), rect.getHeight()));
		displayedIngredient.ifPresent(ignored -> recipeDecorations.draw(guiGraphics,x,y,rect.getWidth(),rect.getHeight()));

		displayedIngredient.ifPresent(ignored -> drawCandidatesBadge(guiGraphics));

		if (hovered) {
			IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
			drawHighlight(guiGraphics, screenHelper.getSlotHighlightFrontSprite());
		}
	}

	private void drawCandidatesBadge(GuiGraphicsExtractor guiGraphics) {
		if (!hasCandidates()) {
			return;
		}
		Textures textures = Internal.getTextures();
		IDrawable badgeIcon = getTagKey()
			.map(tagKey -> textures.getTagBadgeIcon())
			.orElseGet(textures::getListBadgeIcon);
		int badgeX = this.rect.getX() + this.rect.getWidth() - badgeIcon.getWidth() + 1;
		int badgeY = this.rect.getY() - 1;
		badgeIcon.draw(guiGraphics, badgeX, badgeY);
	}

	private void drawHighlight(GuiGraphicsExtractor guiGraphics, Identifier sprite) {
		int x = this.rect.getX();
		int y = this.rect.getY();
		int width = this.rect.getWidth();
		int height = this.rect.getHeight();

		guiGraphics.blitSprite(
			RenderPipelines.GUI_TEXTURED,
			sprite,
			x - 4,
			y - 4,
			width + 8,
			height + 8
		);
	}

	private <T> void drawIngredient(GuiGraphicsExtractor guiGraphics, ITypedIngredient<T> typedIngredient, int xPos, int yPos) {
		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientRenderer<T> ingredientRenderer = getIngredientRenderer(ingredientType);

		SafeIngredientUtil.render(guiGraphics, ingredientRenderer, typedIngredient, xPos, yPos);
	}

	@Override
	public void drawTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		JivTooltip tooltip = new JivTooltip();
		addTooltip(tooltip);
		tooltip.draw(guiGraphics, mouseX, mouseY);
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.rect.contains(mouseX, mouseY);
	}

	@Override
	public void setPosition(int x, int y) {
		this.rect = this.rect.setPosition(x, y);
	}

	@Override
	public void clearDisplayOverrides() {
		ingredients.clearDisplayOverrides();
	}

	@Override
	public IIngredientAcceptor<?> createDisplayOverrides() {
		return ingredients.createDisplayOverrides();
	}

	public void setDisplayOverridesChangedListener(Runnable listener) {
		this.displayOverridesChangedListener = listener;
	}

	private void onDisplayOverridesChanged() {
		invalidateTooltipCache();
		invalidateTagKey();
		displayOverridesChangedListener.run();
	}

	private void invalidateTagKey() {
		this.tagKey.invalidate();
	}

	@Override
	public Rect2i getAreaIncludingBackground() {
		if (background == null) {
			return rect.toMutable();
		}
		return MathUtil.union(rect, background.getArea()).toMutable();
	}

	@Override
	public String toString() {
		return "RecipeSlot{" +
			"rect=" + rect +
			'}';
	}
}
