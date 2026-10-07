package eakerzt.jiv.library.transfer;

import eakerzt.jiv.api.constants.VanillaTypes;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotView;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotsView;
import eakerzt.jiv.api.helpers.IStackHelper;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferContext;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferError;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferHandler;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferHandlerHelper;
import eakerzt.jiv.api.recipe.transfer.IRecipeTransferInfo;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.ingredients.TypedIngredient;
import eakerzt.jiv.common.network.IConnectionToServer;
import eakerzt.jiv.common.transfer.RecipeTransferContext;
import eakerzt.jiv.common.transfer.RecipeTransferErrorInternal;
import eakerzt.jiv.common.util.ErrorUtil;
import eakerzt.jiv.common.util.ImmutableSize2i;
import eakerzt.jiv.library.gui.helpers.CraftingGridHelper;
import eakerzt.jiv.library.plugins.vanilla.crafting.CraftingRecipeCategory;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class RecipeTransferHandlerHelper implements IRecipeTransferHandlerHelper {
	private final IStackHelper stackHelper;
	private final IIngredientManager ingredientManager;
	private final CraftingRecipeCategory craftingRecipeCategory;
	private final IConnectionToServer serverConnection;

	public RecipeTransferHandlerHelper(
		IStackHelper stackHelper,
		IIngredientManager ingredientManager,
		CraftingRecipeCategory craftingRecipeCategory,
		IConnectionToServer serverConnection
	) {
		this.stackHelper = stackHelper;
		this.ingredientManager = ingredientManager;
		this.craftingRecipeCategory = craftingRecipeCategory;
		this.serverConnection = serverConnection;
	}

	@Override
	public IRecipeTransferError createInternalError() {
		return RecipeTransferErrorInternal.INSTANCE;
	}

	@Override
	public IRecipeTransferError createUserErrorWithTooltip(Component tooltipMessage) {
		ErrorUtil.checkNotNull(tooltipMessage, "tooltipMessage");

		return new RecipeTransferErrorTooltip(tooltipMessage);
	}

	@Override
	public <C extends AbstractContainerMenu, R> IRecipeTransferInfo<C, R> createBasicRecipeTransferInfo(
		Class<? extends C> containerClass,
		@Nullable MenuType<C> menuType,
		IRecipeType<R> recipeType,
		int recipeSlotStart,
		int recipeSlotCount,
		int inventorySlotStart,
		int inventorySlotCount
	) {
		ErrorUtil.checkNotNull(containerClass, "containerClass");
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		return new BasicRecipeTransferInfo<>(containerClass, menuType, recipeType, recipeSlotStart, recipeSlotCount, inventorySlotStart, inventorySlotCount);
	}

	@Override
	public <C extends AbstractContainerMenu, R> IRecipeTransferHandler<C, R> createUnregisteredRecipeTransferHandler(IRecipeTransferInfo<C, R> recipeTransferInfo) {
		ErrorUtil.checkNotNull(recipeTransferInfo, "recipeTransferInfo");
		return new BasicRecipeTransferHandler<>(serverConnection, stackHelper, this, recipeTransferInfo);

	}

	@Override
	public <C extends AbstractContainerMenu, R> @Nullable IRecipeTransferError transferRecipeWithInputAlternatives(
		IRecipeTransferHandler<C, R> recipeTransferHandler,
		IRecipeTransferContext<R, C> context,
		List<IRecipeSlotsView> inputAlternatives,
		boolean doTransfer
	) {
		ErrorUtil.checkNotNull(recipeTransferHandler, "recipeTransferHandler");
		ErrorUtil.checkNotNull(context, "context");
		ErrorUtil.checkNotNull(inputAlternatives, "inputAlternatives");
		if (inputAlternatives.isEmpty()) {
			return recipeTransferHandler.transferRecipe(context, doTransfer);
		}

		IRecipeTransferError firstError = null;
		for (IRecipeSlotsView inputAlternative : inputAlternatives) {
			IRecipeTransferContext<R, C> inputContext = RecipeTransferContext.copyWithRecipeSlots(context, inputAlternative);
			IRecipeTransferError error = recipeTransferHandler.transferRecipe(inputContext, false);
			if (error != null) {
				if (firstError == null) {
					firstError = error;
				}
				continue;
			}

			if (doTransfer) {
				return recipeTransferHandler.transferRecipe(inputContext, true);
			}
			return null;
		}
		return firstError;
	}

	@Override
	public IRecipeTransferError createUserErrorForMissingSlots(Component tooltipMessage, Collection<IRecipeSlotView> missingItemSlots) {
		ErrorUtil.checkNotNull(tooltipMessage, "tooltipMessage");
		ErrorUtil.checkNotEmpty(missingItemSlots, "missingItemSlots");

		return new RecipeTransferErrorMissingSlots(tooltipMessage, missingItemSlots);
	}

	@Override
	public IRecipeSlotsView createRecipeSlotsView(List<IRecipeSlotView> slotViews) {
		return () -> slotViews;
	}

	@Override
	public IRecipeSlotView copyWithIngredients(IRecipeSlotView recipeSlot, List<ItemStack> ingredients) {
		ErrorUtil.checkNotNull(recipeSlot, "recipeSlot");
		ErrorUtil.checkNotNull(ingredients, "ingredients");
		List<ITypedIngredient<?>> typedIngredients = Collections.unmodifiableList(
			TypedIngredient.createAndFilterInvalidNonnullList(
				ingredientManager,
				VanillaTypes.ITEM_STACK,
				ingredients,
				false
			)
		);
		return new RecipeSlotViewWithIngredients(recipeSlot, typedIngredients);
	}

	@Override
	public boolean recipeTransferHasServerSupport() {
		return serverConnection.isJivOnServer();
	}

	@Override
	public Map<Integer, SlotDisplay> getGuiSlotIndexToIngredientMap(RecipeHolder<CraftingRecipe> recipeHolder) {
		if (!craftingRecipeCategory.isHandled(recipeHolder)) {
			return Map.of();
		}
		var ingredients = craftingRecipeCategory.getIngredients(recipeHolder);
		ImmutableSize2i recipeSize = craftingRecipeCategory.getRecipeSize(recipeHolder);
		return CraftingGridHelper.getGuiSlotToIngredientMap(ingredients, recipeSize.width(), recipeSize.height());
	}

	private record RecipeSlotViewWithIngredients(
		IRecipeSlotView original,
		List<ITypedIngredient<?>> ingredients
	) implements IRecipeSlotView {
		@Override
		public Stream<ITypedIngredient<?>> getAllIngredients() {
			return ingredients.stream();
		}

		@Override
		public List<ITypedIngredient<?>> getAllIngredientsList() {
			return ingredients;
		}

		@Override
		public Optional<ITypedIngredient<?>> getDisplayedIngredient() {
			return ingredients.stream().findFirst();
		}

		@Override
		public Stream<ITypedIngredient<?>> getDisplayedIngredients() {
			return ingredients.stream();
		}

		@Override
		public Optional<TagKey<?>> getTagKey() {
			return Optional.empty();
		}

		@Override
		public RecipeIngredientRole getRole() {
			return original.getRole();
		}

		@Override
		public void drawHighlight(GuiGraphicsExtractor guiGraphics, int color) {
			original.drawHighlight(guiGraphics, color);
		}

		@Override
		public Optional<String> getSlotName() {
			return original.getSlotName();
		}
	}
}
