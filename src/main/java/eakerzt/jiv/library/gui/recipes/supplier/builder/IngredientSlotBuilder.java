package eakerzt.jiv.library.gui.recipes.supplier.builder;

import eakerzt.jiv.api.gui.builder.IRecipeSlotBuilder;
import eakerzt.jiv.api.gui.drawable.IDrawable;
import eakerzt.jiv.api.gui.drawable.TilingDirection;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import eakerzt.jiv.api.gui.placement.HorizontalAlignment;
import eakerzt.jiv.api.gui.placement.VerticalAlignment;
import eakerzt.jiv.api.ingredients.IIngredientRenderer;
import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.library.gui.recipes.layout.builder.RecipeSlotBuilder;
import eakerzt.jiv.library.ingredients.DisplayIngredientAcceptor;
import eakerzt.jiv.library.ingredients.IIngredientManagerInternal;
import eakerzt.jiv.library.ingredients.SlotIngredient;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Minimal version of {@link RecipeSlotBuilder} that can only return the ingredients,
 * but doesn't bother building anything for drawing on screen.
 */
public class IngredientSlotBuilder implements IRecipeSlotBuilder {
	private final DisplayIngredientAcceptor ingredients;
	private final RecipeIngredientRole role;

	public IngredientSlotBuilder(IIngredientManagerInternal ingredientManager, ContextMap contextMap, RecipeIngredientRole role) {
		this.ingredients = new DisplayIngredientAcceptor(ingredientManager, contextMap, role);
		this.role = role;
	}

	@Override
	public ContextMap getContextMap() {
		return ingredients.getContextMap();
	}

	@Override
	public IRecipeSlotBuilder add(SlotDisplay slotDisplay) {
		this.ingredients.add(slotDisplay);
		return this;
	}

	@Override
	public <I> IRecipeSlotBuilder add(IIngredientType<I> ingredientType, SlotDisplay slotDisplay) {
		this.ingredients.add(ingredientType, slotDisplay);
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(ItemStack itemStack) {
		this.ingredients.add(itemStack);
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(ItemLike itemLike) {
		this.ingredients.add(itemLike);
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(ItemStackTemplate itemStackTemplate) {
		this.ingredients.add(itemStackTemplate);
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(Fluid fluid) {
		this.ingredients.add(fluid);
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(Fluid fluid, long amount) {
		this.ingredients.add(fluid, amount);
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(Fluid fluid, long amount, DataComponentPatch component) {
		this.ingredients.add(fluid, amount, component);
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(Ingredient ingredient) {
		this.ingredients.add(ingredient);
		return this;
	}

	@Override
	public <I> IRecipeSlotBuilder add(IIngredientType<I> ingredientType, Ingredient ingredient) {
		this.ingredients.add(ingredientType, ingredient);
		return this;
	}

	@Override
	public <I> IRecipeSlotBuilder add(ITypedIngredient<I> typedIngredient) {
		this.ingredients.add(typedIngredient);
		return this;
	}

	@Override
	public <I> IRecipeSlotBuilder add(IIngredientType<I> ingredientType, I ingredient) {
		this.ingredients.add(ingredientType, ingredient);
		return this;
	}

	@Override
	public <I> IRecipeSlotBuilder addIngredients(IIngredientType<I> ingredientType, List<@Nullable I> ingredients) {
		this.ingredients.addIngredients(ingredientType, ingredients);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addIngredientsUnsafe(List<?> ingredients) {
		this.ingredients.addIngredientsUnsafe(ingredients);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addTypedIngredients(List<ITypedIngredient<?>> ingredients) {
		this.ingredients.addTypedIngredients(ingredients);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addOptionalTypedIngredients(List<Optional<ITypedIngredient<?>>> ingredients) {
		this.ingredients.addOptionalTypedIngredients(ingredients);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addItemStacks(List<ItemStack> itemStacks) {
		this.ingredients.addItemStacks(itemStacks);
		return this;
	}

	@Override
	public IRecipeSlotBuilder setStandardSlotBackground() {
		return this;
	}

	@Override
	public IRecipeSlotBuilder setOutputSlotBackground() {
		return this;
	}

	@Override
	public IRecipeSlotBuilder setBackground(IDrawable background, int xOffset, int yOffset) {
		return this;
	}

	@Override
	public IRecipeSlotBuilder setOverlay(IDrawable overlay, int xOffset, int yOffset) {
		return this;
	}

	@Override
	public IRecipeSlotBuilder setFluidRenderer(long capacity, boolean showCapacity, int width, int height) {
		return this;
	}

	@Override
	public IRecipeSlotBuilder setFluidRenderer(long capacity, boolean showCapacity, int width, int height, TilingDirection tilingDirection) {
		return this;
	}

	@Override
	public <T> IRecipeSlotBuilder setCustomRenderer(IIngredientType<T> ingredientType, IIngredientRenderer<T> ingredientRenderer) {
		return this;
	}

	@Override
	public IRecipeSlotBuilder addRichTooltipCallback(IRecipeSlotRichTooltipCallback tooltipCallback) {
		return this;
	}

	@Override
	public IRecipeSlotBuilder setSlotName(String slotName) {
		return this;
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
	public IRecipeSlotBuilder setPosition(int xPos, int yPos) {
		return this;
	}

	@Override
	public IRecipeSlotBuilder setPosition(int areaX, int areaY, int areaWidth, int areaHeight, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
		return this;
	}

	public List<@Nullable SlotIngredient<?>> getAllSlotIngredients() {
		return this.ingredients.getAllSlotIngredients();
	}

	public RecipeIngredientRole getRole() {
		return role;
	}
}
