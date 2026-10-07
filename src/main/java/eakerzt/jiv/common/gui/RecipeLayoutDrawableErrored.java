package eakerzt.jiv.common.gui;

import eakerzt.jiv.api.gui.IRecipeLayoutDrawable;
import eakerzt.jiv.api.gui.drawable.IScalableDrawable;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotsView;
import eakerzt.jiv.api.gui.inputs.IJivInputHandler;
import eakerzt.jiv.api.gui.inputs.RecipeSlotUnderMouse;
import eakerzt.jiv.api.gui.widgets.IScrollBoxWidget;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.api.helpers.IJivHelpers;
import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.recipe.category.IRecipeCategory;
import eakerzt.jiv.api.runtime.IJivRuntime;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.util.ImmutableRect2i;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecipeLayoutDrawableErrored<R> implements IRecipeLayoutDrawable<R> {
	private final IRecipeCategory<R> recipeCategory;
	private final R recipe;
	private final IScrollBoxWidget scrollBoxWidget;
	private final IJivInputHandler inputHandler;
	private final IScalableDrawable background;
	private final int borderPadding;
	private ImmutableRect2i area;

	public RecipeLayoutDrawableErrored(IRecipeCategory<R> recipeCategory, R recipe, IScalableDrawable background, int borderPadding) {
		this.recipeCategory = recipeCategory;
		this.recipe = recipe;
		this.area = new ImmutableRect2i(0, 0, Math.max(100, recipeCategory.getWidth()), recipeCategory.getHeight());
		this.background = background;
		this.borderPadding = borderPadding;

		List<FormattedText> lines = new ArrayList<>();
		lines.add(Component.translatable("gui.jiv.category.recipe.crashed").withStyle(ChatFormatting.RED));
		Identifier id = recipeCategory.getIdentifier(recipe);
		if (id != null) {
			lines.add(Component.literal(id.toString()).withStyle(ChatFormatting.GRAY));
		}
		lines.add(Component.empty());
		lines.add(Component.literal(recipeCategory.getRecipeType().getUid().toString()).withStyle(ChatFormatting.GRAY));

		IJivRuntime jivRuntime = Internal.getJivRuntime();
		IJivHelpers jivHelpers = jivRuntime.getJivHelpers();
		IGuiHelper guiHelper = jivHelpers.getGuiHelper();
		this.scrollBoxWidget = guiHelper.createScrollBoxWidget(area.width(), area.getHeight(), 0, 0)
			.setContents(lines);

		this.inputHandler = new OffsetJivInputHandler(this.scrollBoxWidget, this::getScreenPosition);
	}

	private ScreenPosition getScreenPosition() {
		return this.area.getScreenPosition();
	}

	@Override
	public void setPosition(int posX, int posY) {
		this.area = this.area.setPosition(posX, posY);
	}

	@Override
	public void drawRecipe(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		background.draw(guiGraphics, getRectWithBorder());

		var poseStack = guiGraphics.pose();
		poseStack.pushMatrix();
		{
			poseStack.translate(area.x(), area.y());
			int recipeMouseX = mouseX - area.x();
			int recipeMouseY = mouseY - area.y();
			ScreenPosition position = scrollBoxWidget.getPosition();
			poseStack.pushMatrix();
			{
				poseStack.translate(position.x(), position.y());
				scrollBoxWidget.drawWidget(guiGraphics, recipeMouseX - position.x(), recipeMouseY - position.y());
			}
			poseStack.popMatrix();
		}
		poseStack.popMatrix();
	}

	@Override
	public void drawOverlays(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {

	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return area.contains(mouseX, mouseY);
	}

	@Override
	public <T> Optional<T> getIngredientUnderMouse(int mouseX, int mouseY, IIngredientType<T> ingredientType) {
		return Optional.empty();
	}

	@Override
	public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY) {
		return Optional.empty();
	}

	@Override
	public Rect2i getRect() {
		return area.toMutable();
	}

	@Override
	public Rect2i getRectWithBorder() {
		return area.expandBy(borderPadding).toMutable();
	}

	@Override
	public Rect2i getSideButtonArea(int buttonIndex) {
		return new Rect2i(0, 0, 0, 0);
	}

	@Override
	public IRecipeSlotsView getRecipeSlotsView() {
		return List::of;
	}

	@Override
	public IRecipeCategory<R> getRecipeCategory() {
		return recipeCategory;
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

	}
}
