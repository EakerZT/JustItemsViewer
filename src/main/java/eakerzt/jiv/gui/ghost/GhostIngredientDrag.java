package eakerzt.jiv.gui.ghost;

import eakerzt.jiv.api.gui.handlers.IGhostIngredientHandler;
import eakerzt.jiv.api.gui.handlers.IGhostIngredientHandler.Target;
import eakerzt.jiv.api.ingredients.IIngredientRenderer;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.config.IClientConfig;
import eakerzt.jiv.common.gui.JivGuiColors;
import eakerzt.jiv.common.gui.JivGuiColors.GuiColor;
import eakerzt.jiv.common.util.ImmutableRect2i;
import eakerzt.jiv.common.util.MathUtil;
import eakerzt.jiv.common.util.SafeIngredientUtil;
import eakerzt.jiv.common.input.UserInput;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.phys.Vec2;

import java.util.List;

public class GhostIngredientDrag<T> {
	private final List<HandlerData<T>> handlersData;
	private final IIngredientRenderer<T> ingredientRenderer;
	private final ITypedIngredient<T> ingredient;
	private final double mouseStartX;
	private final double mouseStartY;
	private final ImmutableRect2i origin;
	private final long dragCanStartTime;

	public GhostIngredientDrag(
		List<HandlerData<T>> handlersData,
		IIngredientRenderer<T> ingredientRenderer,
		ITypedIngredient<T> ingredient,
		double mouseX,
		double mouseY,
		ImmutableRect2i origin
	) {
		this.handlersData = handlersData;
		this.ingredientRenderer = ingredientRenderer;
		this.ingredient = ingredient;
		this.origin = origin;
		this.mouseStartX = mouseX;
		this.mouseStartY = mouseY;
		IClientConfig clientConfig = Internal.getClientConfigs().getClientConfig();
		this.dragCanStartTime = System.currentTimeMillis() + clientConfig.dragDelayMs().get();
	}

	public void drawTargets(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		for (HandlerData<T> data : handlersData) {
			IGhostIngredientHandler<?> handler = data.handler;
			if (handler.shouldHighlightTargets()) {
				drawTargets(guiGraphics, mouseX, mouseY, data.targetAreas);
			}
		}
	}

	public static boolean canStart(GhostIngredientDrag<?> drag, double mouseX, double mouseY) {
		if (System.currentTimeMillis() < drag.dragCanStartTime) {
			return false;
		}
		ImmutableRect2i origin = drag.getOrigin();
		final Vec2 center;
		if (origin.isEmpty()) {
			center = new Vec2((float) drag.mouseStartX, (float) drag.mouseStartY);
		} else {
			if (origin.contains(mouseX, mouseY)) {
				return false;
			}
			center = new Vec2(
				origin.getX() + (origin.getWidth() / 2.0f),
				origin.getY() + (origin.getHeight() / 2.0f)
			);
		}

		double mouseXDist = center.x - mouseX;
		double mouseYDist = center.y - mouseY;
		double mouseDistSq = mouseXDist * mouseXDist + mouseYDist * mouseYDist;
		return mouseDistSq > 64.0;
	}

	public void drawItem(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (!canStart(this, mouseX, mouseY)) {
			return;
		}

		SafeIngredientUtil.render(guiGraphics, ingredientRenderer, ingredient, mouseX - 8, mouseY - 8);
	}

	public static void drawTargets(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, List<Rect2i> targetAreas) {
		for (Rect2i area : targetAreas) {
			int color;
			if (MathUtil.contains(area, mouseX, mouseY)) {
				color = JivGuiColors.getColor(GuiColor.GHOST_INGREDIENT_DRAG_HOVER_HIGHLIGHT);
			} else {
				color = JivGuiColors.getColor(GuiColor.GHOST_INGREDIENT_DRAG_TARGET_HIGHLIGHT);
			}
			guiGraphics.fill(
				area.getX(),
				area.getY(),
				area.getX() + area.getWidth(),
				area.getY() + area.getHeight(),
				color
			);
		}
	}

	public boolean onClick(UserInput input) {
		if (!canStart(this, input.getMouseX(), input.getMouseY())) {
			return false;
		}

		for (HandlerData<T> data : handlersData) {
			for (Target<T> target : data.targets) {
				Rect2i area = target.getArea();
				if (MathUtil.contains(area, input.getMouseX(), input.getMouseY())) {
					if (!input.isSimulate()) {
						target.accept(ingredient.getIngredient());
						data.handler.onComplete();
					}
					return true;
				}
			}

			if (!input.isSimulate()) {
				data.handler.onComplete();
			}
		}
		return false;
	}

	public void stop() {
		for (HandlerData<T> data : handlersData) {
			data.handler.onComplete();
		}
	}

	public IIngredientRenderer<T> getIngredientRenderer() {
		return ingredientRenderer;
	}

	public ITypedIngredient<T> getIngredient() {
		return ingredient;
	}

	public ImmutableRect2i getOrigin() {
		return origin;
	}

	public record HandlerData<T>(IGhostIngredientHandler<?> handler, List<Target<T>> targets, List<Rect2i> targetAreas) {
		public HandlerData(IGhostIngredientHandler<?> handler, List<Target<T>> targets) {
			this(
				handler,
				targets,
				targets.stream()
					.map(Target::getArea)
					.toList()
			);
		}
	}
}
