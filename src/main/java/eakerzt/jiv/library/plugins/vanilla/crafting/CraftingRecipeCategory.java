package eakerzt.jiv.library.plugins.vanilla.crafting;

import com.mojang.serialization.Codec;
import eakerzt.jiv.api.constants.RecipeTypes;
import eakerzt.jiv.api.gui.builder.IRecipeLayoutBuilder;
import eakerzt.jiv.api.gui.builder.ITooltipBuilder;
import eakerzt.jiv.api.gui.ingredient.ICraftingGridHelper;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotDrawable;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotsView;
import eakerzt.jiv.api.gui.placement.HorizontalAlignment;
import eakerzt.jiv.api.gui.placement.VerticalAlignment;
import eakerzt.jiv.api.gui.widgets.IRecipeExtrasBuilder;
import eakerzt.jiv.api.gui.widgets.IRecipeWidget;
import eakerzt.jiv.api.helpers.ICodecHelper;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.api.recipe.IFocusGroup;
import eakerzt.jiv.api.recipe.IRecipeManager;
import eakerzt.jiv.api.recipe.category.AbstractRecipeCategory;
import eakerzt.jiv.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import eakerzt.jiv.api.recipe.category.extensions.vanilla.crafting.IExtendableCraftingRecipeCategory;
import eakerzt.jiv.common.util.ErrorUtil;
import eakerzt.jiv.common.util.ImmutableSize2i;
import eakerzt.jiv.library.recipes.CraftingExtensionHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class CraftingRecipeCategory extends AbstractRecipeCategory<RecipeHolder<CraftingRecipe>> implements IExtendableCraftingRecipeCategory {
	public static final int width = 116;
	public static final int height = 54;

	private final ICraftingGridHelper craftingGridHelper;
	private final CraftingExtensionHelper extendableHelper = new CraftingExtensionHelper();

	public CraftingRecipeCategory(IGuiHelper guiHelper) {
		super(
			RecipeTypes.CRAFTING,
			Component.translatable("gui.jiv.category.craftingTable"),
			guiHelper.createDrawableItemLike(Blocks.CRAFTING_TABLE),
			width,
			height
		);
		craftingGridHelper = guiHelper.createCraftingGridHelper();
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<CraftingRecipe> recipeHolder, IFocusGroup focuses) {
		var recipeExtension = this.extendableHelper.getRecipeExtension(this, recipeHolder);
		recipeExtension.setRecipe(recipeHolder, builder, craftingGridHelper, focuses);
	}

	@Override
	public void onDisplayedIngredientsUpdate(RecipeHolder<CraftingRecipe> recipeHolder, List<IRecipeSlotDrawable> recipeSlots, IFocusGroup focuses) {
		var recipeExtension = this.extendableHelper.getRecipeExtension(this, recipeHolder);
		recipeExtension.onDisplayedIngredientsUpdate(recipeHolder, recipeSlots, focuses);
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<CraftingRecipe> recipeHolder, IFocusGroup focuses) {
		var recipeExtension = this.extendableHelper.getRecipeExtension(this, recipeHolder);
		int recipeWidth = this.getWidth();
		int recipeHeight = this.getHeight();
		builder.addWidget(new CraftingExtensionRecipeWidget(recipeExtension, recipeHolder, recipeWidth, recipeHeight));

		builder.addRecipeArrowWidget()
			.setPosition(61, 0, width - 61, height, HorizontalAlignment.LEFT, VerticalAlignment.CENTER);

		recipeExtension.createRecipeExtras(recipeHolder, builder, craftingGridHelper, focuses);
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, RecipeHolder<CraftingRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		var extension = this.extendableHelper.getRecipeExtension(this, recipeHolder);
		extension.getTooltip(tooltip, recipeHolder, mouseX, mouseY);
	}

	@Override
	public boolean isHandled(RecipeHolder<CraftingRecipe> recipeHolder) {
		return this.extendableHelper.getOptionalRecipeExtension(recipeHolder)
			.isPresent();
	}

	public <R extends CraftingRecipe> void addExtension(Class<? extends R> recipeClass, ICraftingCategoryExtension<R> extension) {
		ErrorUtil.checkNotNull(recipeClass, "recipeClass");
		ErrorUtil.checkNotNull(extension, "extension");
		extendableHelper.addRecipeExtension(recipeClass, extension);
	}

	@Override
	public Identifier getIdentifier(RecipeHolder<CraftingRecipe> recipeHolder) {
		ErrorUtil.checkNotNull(recipeHolder, "recipeHolder");
		return recipeHolder.id().identifier();
	}

	@Override
	public Codec<RecipeHolder<CraftingRecipe>> getCodec(ICodecHelper codecHelper, IRecipeManager recipeManager) {
		return codecHelper.getRecipeHolderCodec();
	}

	public ImmutableSize2i getRecipeSize(RecipeHolder<CraftingRecipe> recipeHolder) {
		ErrorUtil.checkNotNull(recipeHolder, "recipeHolder");
		return this.extendableHelper.getOptionalRecipeExtension(recipeHolder)
			.map(extension -> {
				int width = extension.getWidth(recipeHolder);
				int height = extension.getHeight(recipeHolder);
				return new ImmutableSize2i(width, height);
			})
			.orElse(ImmutableSize2i.EMPTY);
	}

	public List<SlotDisplay> getIngredients(RecipeHolder<CraftingRecipe> recipeHolder) {
		ErrorUtil.checkNotNull(recipeHolder, "recipeHolder");
		return this.extendableHelper.getOptionalRecipeExtension(recipeHolder)
			.map(extension -> extension.getIngredients(recipeHolder))
			.orElse(List.of());
	}

	private record CraftingExtensionRecipeWidget(
		ICraftingCategoryExtension<CraftingRecipe> recipeExtension,
		RecipeHolder<CraftingRecipe> recipeHolder,
		int recipeWidth,
		int recipeHeight
	) implements IRecipeWidget {
		@Override
		public ScreenPosition getPosition() {
			return new ScreenPosition(0, 0);
		}

		@Override
		public ScreenRectangle getScreenRectangle() {
			return new ScreenRectangle(0, 0, recipeWidth, recipeHeight);
		}

		@Override
		public void drawWidget(GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
			recipeExtension.drawInfo(recipeHolder, recipeWidth, recipeHeight, guiGraphics, mouseX, mouseY);
		}
	}
}
