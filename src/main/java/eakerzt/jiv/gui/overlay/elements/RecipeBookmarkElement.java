package eakerzt.jiv.gui.overlay.elements;

import eakerzt.jiv.api.gui.IRecipeLayoutDrawable;
import eakerzt.jiv.api.gui.drawable.IDrawable;
import eakerzt.jiv.api.gui.drawable.IScalableDrawable;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.api.helpers.IJivHelpers;
import eakerzt.jiv.api.helpers.IModIdHelper;
import eakerzt.jiv.api.ingredients.IIngredientHelper;
import eakerzt.jiv.api.ingredients.IIngredientRenderer;
import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.IFocus;
import eakerzt.jiv.api.recipe.IFocusFactory;
import eakerzt.jiv.api.recipe.IRecipeManager;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.recipe.category.IRecipeCategory;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferError;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.api.runtime.IJivKeyMapping;
import eakerzt.jiv.api.runtime.IJivRuntime;
import eakerzt.jiv.api.runtime.IRecipesGui;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.config.BookmarkTooltipFeature;
import eakerzt.jiv.common.config.IClientConfig;
import eakerzt.jiv.common.gui.JivTooltip;
import eakerzt.jiv.common.input.IInternalKeyMappings;
import eakerzt.jiv.common.input.keys.IJivKeyMappingInternal;
import eakerzt.jiv.common.transfer.RecipeTransferService;
import eakerzt.jiv.common.util.SafeIngredientUtil;
import eakerzt.jiv.gui.bookmarks.IBookmark;
import eakerzt.jiv.gui.bookmarks.RecipeBookmark;
import eakerzt.jiv.common.input.UserInput;
import eakerzt.jiv.gui.overlay.ingredients.IngredientGridTooltipHelper;
import eakerzt.jiv.common.gui.IngredientsTooltipComponent;
import eakerzt.jiv.gui.overlay.bookmarks.PreviewTooltipComponent;
import eakerzt.jiv.gui.recipes.RecipeCategoryIconUtil;
import eakerzt.jiv.gui.util.FocusUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecipeBookmarkElement<R, I> implements IElement<I> {
	private final RecipeBookmark<R, I> recipeBookmark;
	private final IClientConfig clientConfig;
	private final RecipeTransferService recipeTransferService;
	private @Nullable PreviewTooltipComponent<R> previewTooltipComponent;
	private @Nullable IngredientsTooltipComponent ingredientsTooltipComponent;
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private @Nullable Optional<IRecipeLayoutDrawable<R>> cachedLayoutDrawable;

	public RecipeBookmarkElement(
		RecipeBookmark<R, I> recipeBookmark,
		RecipeTransferService recipeTransferService
	) {
		this.recipeBookmark = recipeBookmark;
		this.recipeTransferService = recipeTransferService;
		this.clientConfig = Internal.getClientConfigs().getClientConfig();
	}

	@Override
	public ITypedIngredient<I> getTypedIngredient() {
		return recipeBookmark.getDisplayIngredient();
	}

	@Override
	public Optional<IBookmark> getBookmark() {
		return Optional.of(recipeBookmark);
	}

	@Override
	public IDrawable createRenderOverlay() {
		IRecipeCategory<R> recipeCategory = recipeBookmark.getRecipeCategory();
		return new RecipeBookmarkIcon(recipeCategory);
	}

	@Override
	public boolean handleClick(UserInput input, IInternalKeyMappings keyBindings) {
		boolean transferOnce = input.is(keyBindings.getTransferRecipeBookmark());
		boolean transferMax = input.is(keyBindings.getMaxTransferRecipeBookmark());
		if (transferOnce || transferMax) {
			Minecraft minecraft = Minecraft.getInstance();
			Screen screen = minecraft.screen;
			Player player = minecraft.player;
			if (player != null && screen instanceof AbstractContainerScreen<?> containerScreen) {
				IRecipeLayoutDrawable<R> recipeLayout = getRecipeLayoutDrawable().orElse(null);
				if (recipeLayout == null) {
					return false;
				}

				if (input.isSimulate()) {
					IRecipeTransferError recipeTransferError = recipeTransferService.getTransferRecipeError(containerScreen, recipeLayout, player).orElse(null);
					return recipeTransferError == null || recipeTransferError.getType().allowsTransfer;
				} else {
					return recipeTransferService.transferRecipe(containerScreen, recipeLayout, player, transferMax);
				}
			}
		}
		return false;
	}

	@Override
	public void show(IRecipesGui recipesGui, FocusUtil focusUtil, List<RecipeIngredientRole> roles) {
		if (!roles.equals(List.of(RecipeIngredientRole.OUTPUT))) {
			recipesGui.show(focusUtil.createFocuses(getTypedIngredient(), roles));
			return;
		}

		IRecipeCategory<R> recipeCategory = recipeBookmark.getRecipeCategory();
		R recipe = recipeBookmark.getRecipe();
		ITypedIngredient<?> ingredient = getTypedIngredient();
		List<IFocus<?>> focuses = focusUtil.createFocuses(ingredient, List.of(RecipeIngredientRole.OUTPUT));
		recipesGui.showRecipes(recipeCategory, List.of(recipe), focuses);
	}

	@Override
	public void getTooltip(JivTooltip tooltip, IngredientGridTooltipHelper tooltipHelper, IIngredientRenderer<I> ingredientRenderer, IIngredientHelper<I> ingredientHelper) {
		getTooltip(tooltip, ingredientRenderer, ingredientHelper, false);
	}

	public void getPinnedTooltip(JivTooltip tooltip) {
		IJivRuntime jivRuntime = Internal.getJivRuntime();
		IIngredientManager ingredientManager = jivRuntime.getIngredientManager();
		ITypedIngredient<I> displayIngredient = recipeBookmark.getDisplayIngredient();
		IIngredientType<I> ingredientType = displayIngredient.getType();
		IIngredientRenderer<I> ingredientRenderer = ingredientManager.getIngredientRenderer(ingredientType);
		IIngredientHelper<I> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		getTooltip(tooltip, ingredientRenderer, ingredientHelper, true);
	}

	public Optional<PreviewTooltipComponent<R>> getInteractivePreview() {
		IJivKeyMappingInternal pauseRecipeCycling = Internal.getKeyMappings().getPauseRecipeCycling();
		if (pauseRecipeCycling.isUnbound() ||
			!getBookmarkTooltipFeatures().contains(BookmarkTooltipFeature.PREVIEW)
		) {
			return Optional.empty();
		}
		PreviewTooltipComponent<R> component = this.previewTooltipComponent;
		if (component == null) {
			component = createPreviewTooltipComponent();
			if (component == null) {
				return Optional.empty();
			}
			this.previewTooltipComponent = component;
		}
		return Optional.of(component);
	}

	private void getTooltip(JivTooltip tooltip, IIngredientRenderer<I> ingredientRenderer, IIngredientHelper<I> ingredientHelper, boolean pinned) {
		ITypedIngredient<I> displayIngredient = recipeBookmark.getDisplayIngredient();
		R recipe = recipeBookmark.getRecipe();

		IRecipeCategory<R> recipeCategory = recipeBookmark.getRecipeCategory();
		JivTooltip bookmarkTooltip = new JivTooltip();
		boolean previewAdded = addBookmarkTooltipFeaturesIfEnabled(bookmarkTooltip, pinned);

		if (recipeBookmark.isDisplayIsOutput()) {
			IJivRuntime jivRuntime = Internal.getJivRuntime();
			IIngredientManager ingredientManager = jivRuntime.getIngredientManager();
			IModIdHelper modIdHelper = jivRuntime.getJivHelpers().getModIdHelper();
			boolean recipeByAdded = false;

			Identifier recipeName = recipeCategory.getIdentifier(recipe);
			if (recipeName != null) {
				String recipeModId = recipeName.getNamespace();
				Identifier ingredientId = ingredientHelper.getIdentifier(displayIngredient.getIngredient());
				String ingredientModId = ingredientId.getNamespace();
				if (!recipeModId.equals(ingredientModId)) {
					Component modName = modIdHelper.getFormattedModNameComponentForModId(recipeModId);
					MutableComponent recipeBy = Component.translatable("jiv.tooltip.recipe.by", modName);
					tooltip.add(recipeBy.withStyle(ChatFormatting.GRAY));
					recipeByAdded = true;
				}
			}

			if (recipeByAdded) {
				tooltip.add(Component.empty());
			}

			SafeIngredientUtil.getRichTooltip(tooltip, ingredientManager, ingredientRenderer, displayIngredient);
		}

		if (previewAdded && !pinned) {
			IJivKeyMappingInternal pauseRecipeCycling = Internal.getKeyMappings().getPauseRecipeCycling();
			if (!pauseRecipeCycling.isUnbound()) {
				bookmarkTooltip.addKeyUsageComponent("jiv.tooltip.bookmarks.preview.pin.usage", pauseRecipeCycling);
			}
		}

		if (pinned) {
			tooltip.addAll(bookmarkTooltip);
		} else {
			tooltip.addIngredientTooltipFooter(bookmarkTooltip);
		}
	}

	private boolean addBookmarkTooltipFeaturesIfEnabled(JivTooltip tooltip, boolean pinned) {
		JivTooltip transferComponents = new JivTooltip();
		if (!pinned) {
			transferComponents.addAll(createTransferComponents());
		}
		List<BookmarkTooltipFeature> bookmarkTooltipFeatures = getBookmarkTooltipFeatures();

		if (bookmarkTooltipFeatures.isEmpty() && transferComponents.isEmpty()) {
			return false;
		}

		if (!pinned && clientConfig.holdShiftToShowBookmarkTooltipFeaturesEnabled().get()) {
			IJivKeyMappingInternal pauseRecipeCycling = Internal.getKeyMappings().getPauseRecipeCycling();
			if (pauseRecipeCycling.isUnbound()) {
				return false;
			}
			if (!pauseRecipeCycling.isDown()) {
				tooltip.addKeyUsageComponent(
					"jiv.tooltip.bookmarks.tooltips.usage",
					pauseRecipeCycling
				);
				return false;
			}
		}

		boolean previewAdded = addBookmarkTooltipFeatures(tooltip, bookmarkTooltipFeatures);
		tooltip.addAll(transferComponents);
		return previewAdded;
	}

	private List<BookmarkTooltipFeature> getBookmarkTooltipFeatures() {
		List<BookmarkTooltipFeature> features = new ArrayList<>(2);
		if (clientConfig.bookmarkTooltipPreviewEnabled().get()) {
			features.add(BookmarkTooltipFeature.PREVIEW);
		}
		if (clientConfig.bookmarkTooltipIngredientsEnabled().get()) {
			features.add(BookmarkTooltipFeature.INGREDIENTS);
		}
		return List.copyOf(features);
	}

	private boolean addBookmarkTooltipFeatures(JivTooltip tooltip, List<BookmarkTooltipFeature> features) {
		boolean previewAdded = false;
		for (BookmarkTooltipFeature feature : features) {
			boolean added = addBookmarkTooltipFeature(tooltip, feature);
			if (feature == BookmarkTooltipFeature.PREVIEW && added) {
				previewAdded = true;
			}
			if (!added) {
				break;
			}
		}
		return previewAdded;
	}

	private boolean addBookmarkTooltipFeature(JivTooltip tooltip, BookmarkTooltipFeature feature) {
		return switch (feature) {
			case PREVIEW -> addPreviewTooltipComponent(tooltip);
			case INGREDIENTS -> addIngredientsTooltipComponent(tooltip);
		};
	}

	private boolean addPreviewTooltipComponent(JivTooltip tooltip) {
		PreviewTooltipComponent<R> component = previewTooltipComponent;
		if (component == null) {
			component = createPreviewTooltipComponent();
			if (component == null) {
				return false;
			}
			previewTooltipComponent = component;
		}
		component.setStatic();
		tooltip.add(component);
		return true;
	}

	private @Nullable PreviewTooltipComponent<R> createPreviewTooltipComponent() {
		IRecipeLayoutDrawable<R> recipeLayout = getRecipeLayoutDrawable().orElse(null);
		if (recipeLayout == null) {
			return null;
		}
		return new PreviewTooltipComponent<>(recipeLayout, recipeTransferService);
	}

	private boolean addIngredientsTooltipComponent(JivTooltip tooltip) {
		IngredientsTooltipComponent component = ingredientsTooltipComponent;
		if (component == null) {
			IRecipeLayoutDrawable<R> recipeLayout = getRecipeLayoutDrawable().orElse(null);
			if (recipeLayout == null) {
				return false;
			}
			component = new IngredientsTooltipComponent(recipeLayout);
			ingredientsTooltipComponent = component;
		}

		tooltip.add(component);
		return true;
	}

	private JivTooltip createTransferComponents() {
		JivTooltip results = new JivTooltip();

		Minecraft minecraft = Minecraft.getInstance();
		Screen screen = minecraft.screen;
		Player player = minecraft.player;
		if (player != null && screen instanceof AbstractContainerScreen<?> containerScreen) {
			IRecipeTransferError recipeTransferError = getRecipeLayoutDrawable()
				.flatMap(recipeLayout -> {
					return recipeTransferService.getTransferRecipeError(containerScreen, recipeLayout, player);
				})
				.orElse(null);

			if (recipeTransferError == null || recipeTransferError.getType().allowsTransfer) {
				IInternalKeyMappings keyMappings = Internal.getKeyMappings();
				IJivKeyMapping transferRecipeBookmark = keyMappings.getTransferRecipeBookmark();
				if (!transferRecipeBookmark.isUnbound()) {
					results.addKeyUsageComponent(
						"jiv.tooltip.bookmarks.tooltips.transfer.usage",
						transferRecipeBookmark
					);
				}

				IJivKeyMapping maxTransferRecipeBookmark = keyMappings.getMaxTransferRecipeBookmark();
				if (!maxTransferRecipeBookmark.isUnbound()) {
					results.addKeyUsageComponent(
						"jiv.tooltip.bookmarks.tooltips.transfer.max.usage",
						maxTransferRecipeBookmark
					);
				}
			}
		}
		return results;
	}

	private Optional<IRecipeLayoutDrawable<R>> getRecipeLayoutDrawable() {
		//noinspection OptionalAssignedToNull
		if (cachedLayoutDrawable == null) {
			IJivRuntime jivRuntime = Internal.getJivRuntime();
			IRecipeManager recipeManager = jivRuntime.getRecipeManager();
			IFocusFactory focusFactory = jivRuntime.getJivHelpers().getFocusFactory();
			IScalableDrawable recipePreviewBackground = Internal.getTextures().getRecipePreviewBackground();

			cachedLayoutDrawable = recipeManager.createRecipeLayoutDrawable(
				recipeBookmark.getRecipeCategory(),
				recipeBookmark.getRecipe(),
				focusFactory.getEmptyFocusGroup(),
				recipePreviewBackground,
				4
			);
		}
		return cachedLayoutDrawable;
	}

	@Override
	public boolean isVisible() {
		return recipeBookmark.isVisible();
	}

	@Override
	public void tick() {
		PreviewTooltipComponent<R> component = previewTooltipComponent;
		if (component != null) {
			component.tick();
		}
	}

	private static class RecipeBookmarkIcon implements IDrawable {
		private final IDrawable icon;

		public RecipeBookmarkIcon(IRecipeCategory<?> recipeCategory) {
			IJivRuntime jivRuntime = Internal.getJivRuntime();
			IRecipeManager recipeManager = jivRuntime.getRecipeManager();
			IJivHelpers jivHelpers = jivRuntime.getJivHelpers();
			IGuiHelper guiHelper = jivHelpers.getGuiHelper();
			icon = RecipeCategoryIconUtil.create(
				recipeCategory,
				recipeManager,
				guiHelper
			);
		}

		@Override
		public int getWidth() {
			return 16;
		}

		@Override
		public int getHeight() {
			return 16;
		}

		@Override
		public void draw(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset) {
			var poseStack = guiGraphics.pose();
			poseStack.pushMatrix();
			{
				poseStack.translate(8 + xOffset, 8 + yOffset);
				poseStack.scale(0.5f, 0.5f);
				icon.draw(guiGraphics);
			}
			poseStack.popMatrix();
		}
	}
}
