package eakerzt.jiv.library.gui.helpers;

import eakerzt.jiv.api.gui.ITickTimer;
import eakerzt.jiv.api.gui.builder.IIngredientAcceptor;
import eakerzt.jiv.api.gui.drawable.IDrawable;
import eakerzt.jiv.api.gui.drawable.IDrawableAnimated;
import eakerzt.jiv.api.gui.drawable.IDrawableBuilder;
import eakerzt.jiv.api.gui.drawable.IDrawableStatic;
import eakerzt.jiv.api.gui.drawable.IScalableDrawable;
import eakerzt.jiv.api.gui.ingredient.ICraftingGridHelper;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotDrawable;
import eakerzt.jiv.api.gui.widgets.IScrollBoxWidget;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.api.ingredients.IIngredientRenderer;
import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.gui.elements.DrawableAnimated;
import eakerzt.jiv.common.gui.elements.DrawableBlank;
import eakerzt.jiv.common.gui.elements.DrawableCombined;
import eakerzt.jiv.common.gui.elements.DrawableIngredient;
import eakerzt.jiv.common.gui.elements.DrawableIngredientRenderer;
import eakerzt.jiv.common.gui.elements.DrawableSprite;
import eakerzt.jiv.common.gui.elements.ScalableDrawable;
import eakerzt.jiv.common.gui.textures.Textures;
import eakerzt.jiv.common.ingredients.TypedIngredientUtil;
import eakerzt.jiv.common.util.ErrorUtil;
import eakerzt.jiv.common.util.TickTimer;
import eakerzt.jiv.library.gui.elements.DrawableBuilder;
import eakerzt.jiv.library.gui.ingredients.CycleTimer;
import eakerzt.jiv.library.gui.recipes.layout.builder.RecipeSlotBuilder;
import eakerzt.jiv.library.gui.widgets.ScrollBoxRecipeWidget;
import eakerzt.jiv.library.focus.FocusGroup;
import eakerzt.jiv.library.ingredients.IIngredientManagerInternal;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class GuiHelper implements IGuiHelper {
	private final IIngredientManagerInternal ingredientManager;
	private final ContextMap contextMap;

	public GuiHelper(IIngredientManagerInternal ingredientManager, ContextMap contextMap) {
		this.ingredientManager = ingredientManager;
		this.contextMap = contextMap;
	}

	@Override
	public IDrawableBuilder drawableBuilder(Identifier id, int u, int v, int width, int height) {
		return new DrawableBuilder(id, u, v, width, height);
	}

	@Override
	public IDrawableStatic createDrawableSprite(TextureAtlas textureAtlas, Identifier spriteId, int width, int height) {
		ErrorUtil.checkNotNull(textureAtlas, "textureAtlas");
		ErrorUtil.checkNotNull(spriteId, "spriteId");
		checkPositive(width, "width");
		checkPositive(height, "height");
		return new DrawableSprite(textureAtlas, spriteId, width, height);
	}

	@Override
	public IScalableDrawable createScalableDrawableSprite(TextureAtlas textureAtlas, Identifier spriteId) {
		ErrorUtil.checkNotNull(textureAtlas, "textureAtlas");
		ErrorUtil.checkNotNull(spriteId, "spriteId");
		return new ScalableDrawable(textureAtlas, spriteId);
	}

	@Override
	public IDrawableAnimated createAnimatedDrawable(IDrawableStatic drawable, int ticksPerCycle, IDrawableAnimated.StartDirection startDirection, boolean inverted) {
		ErrorUtil.checkNotNull(drawable, "drawable");
		ErrorUtil.checkNotNull(startDirection, "startDirection");
		return new DrawableAnimated(drawable, ticksPerCycle, startDirection, inverted);
	}

	@Override
	public IDrawableAnimated createAnimatedDrawable(IDrawableStatic drawable, ITickTimer tickTimer, IDrawableAnimated.StartDirection startDirection) {
		ErrorUtil.checkNotNull(drawable, "drawable");
		ErrorUtil.checkNotNull(tickTimer, "tickTimer");
		ErrorUtil.checkNotNull(startDirection, "startDirection");
		return new DrawableAnimated(drawable, tickTimer, startDirection);
	}

	private static void checkPositive(int value, String name) {
		if (value <= 0) {
			throw new IllegalArgumentException(name + " must be positive.");
		}
	}

	@Override
	public IDrawableStatic getSlotDrawable() {
		Textures textures = Internal.getTextures();
		return textures.getSlot();
	}

	@Override
	public IDrawableStatic getOutputSlot() {
		Textures textures = Internal.getTextures();
		return textures.getOutputSlot();
	}

	@Override
	public IDrawableStatic getRecipeArrow() {
		Textures textures = Internal.getTextures();
		return textures.getRecipeArrow();
	}

	@Override
	public IDrawableStatic getRecipeArrowFilled() {
		Textures textures = Internal.getTextures();
		return textures.getRecipeArrowFilled();
	}

	@Override
	public IDrawableAnimated createAnimatedRecipeArrow(int ticksPerCycle) {
		IDrawableAnimated animatedFill = createAnimatedDrawable(getRecipeArrowFilled(), ticksPerCycle, IDrawableAnimated.StartDirection.LEFT, false);
		return new DrawableCombined(getRecipeArrow(), animatedFill);
	}

	@Override
	public IDrawableStatic getRecipePlusSign() {
		Textures textures = Internal.getTextures();
		return textures.getRecipePlusSign();
	}

	@Override
	public IDrawableStatic getRecipeFlameEmpty() {
		Textures textures = Internal.getTextures();
		return textures.getFlameEmptyIcon();
	}

	@Override
	public IDrawableStatic getRecipeFlameFilled() {
		Textures textures = Internal.getTextures();
		return textures.getFlameIcon();
	}

	@Override
	public IDrawableAnimated createAnimatedRecipeFlame(int ticksPerCycle) {
		IDrawableAnimated animatedFill = createAnimatedDrawable(getRecipeFlameFilled(), ticksPerCycle, IDrawableAnimated.StartDirection.TOP, true);
		return new DrawableCombined(getRecipeFlameEmpty(), animatedFill);
	}

	@Override
	public IDrawableStatic createBlankDrawable(int width, int height) {
		return new DrawableBlank(width, height);
	}

	@Override
	public <V> IDrawable createDrawableIngredient(IIngredientType<V> type, V ingredient) {
		ErrorUtil.checkNotNull(type, "type");
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		IIngredientRenderer<V> ingredientRenderer = ingredientManager.getIngredientRenderer(type);
		ITypedIngredient<V> typedIngredient = ingredientManager.createTypedIngredient(type, ingredient, false)
			.orElseThrow(() -> {
				String info = ErrorUtil.getIngredientInfo(ingredient, type, ingredientManager);
				return new IllegalArgumentException(String.format("Ingredient is invalid and cannot be used as a drawable ingredient: %s", info));
			});
		return new DrawableIngredient<>(typedIngredient, ingredientRenderer);
	}

	@Override
	public <V> IDrawable createDrawableIngredient(ITypedIngredient<V> ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		ITypedIngredient<V> checkedIngredient = TypedIngredientUtil.checkTypedIngredientFromApi(ingredientManager, ingredient);
		IIngredientType<V> type = checkedIngredient.getType();
		IIngredientRenderer<V> ingredientRenderer = ingredientManager.getIngredientRenderer(type);
		return new DrawableIngredient<>(checkedIngredient, ingredientRenderer);
	}

	@Override
	public <V> IDrawable createDrawableIngredient(IIngredientRenderer<V> ingredientRenderer, V ingredient) {
		ErrorUtil.checkNotNull(ingredientRenderer, "ingredientRenderer");
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		return new DrawableIngredientRenderer<>(ingredientRenderer, ingredient);
	}

	@Override
	public IRecipeSlotDrawable createRecipeSlotDrawable(
		RecipeIngredientRole role,
		List<Optional<ITypedIngredient<?>>> ingredients,
		Set<Integer> focusedIngredients,
		int ingredientCycleOffset
	) {
		return createRecipeSlotDrawable(
			role,
			acceptor -> acceptor.addOptionalTypedIngredients(ingredients),
			focusedIngredients,
			ingredientCycleOffset
		);
	}

	@Override
	public IRecipeSlotDrawable createRecipeSlotDrawable(
		RecipeIngredientRole role,
		Consumer<IIngredientAcceptor<?>> ingredientAdder,
		Set<Integer> focusedIngredients,
		int ingredientCycleOffset
	) {
		RecipeSlotBuilder builder = new RecipeSlotBuilder(ingredientManager, contextMap, 0, role);
		ingredientAdder.accept(builder);
		return builder.build(focusedIngredients, FocusGroup.EMPTY, CycleTimer.create(ingredientCycleOffset)).second();
	}

	@Override
	public ICraftingGridHelper createCraftingGridHelper() {
		return CraftingGridHelper.INSTANCE;
	}

	@Override
	public IScrollBoxWidget createScrollBoxWidget(int width, int height, int xPos, int yPos) {
		return new ScrollBoxRecipeWidget(width, height, xPos, yPos);
	}

	@Override
	public ITickTimer createTickTimer(int ticksPerCycle, int maxValue, boolean countDown) {
		return new TickTimer(ticksPerCycle, maxValue, countDown);
	}
}
