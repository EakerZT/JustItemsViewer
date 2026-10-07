package eakerzt.jiv.gui.overlay.ingredients;

import eakerzt.jiv.api.gui.drawable.IDrawable;
import eakerzt.jiv.api.ingredients.IIngredientRenderer;
import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.ingredients.rendering.BatchRenderElement;
import eakerzt.jiv.api.runtime.IEditModeConfig;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.gui.JivGuiColors;
import eakerzt.jiv.common.gui.JivGuiColors.GuiColor;
import eakerzt.jiv.common.gui.elements.OffsetDrawable;
import eakerzt.jiv.common.util.ImmutableRect2i;
import eakerzt.jiv.common.util.SafeIngredientUtil;
import eakerzt.jiv.common.collect.ListMultiMap;
import eakerzt.jiv.gui.overlay.elements.IElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class IngredientListRenderer {
	private final List<IngredientListSlot> slots = new ArrayList<>();
	private final ListMultiMap<IIngredientType<?>, BatchRenderElement<?>> renderElementsByType = new ListMultiMap<>();
	private final List<IDrawable> renderOverlays = new ArrayList<>();
	private final IIngredientManager ingredientManager;
	private final boolean searchable;

	private int blocked = 0;

	public IngredientListRenderer(IIngredientManager ingredientManager, boolean searchable) {
		this.ingredientManager = ingredientManager;
		this.searchable = searchable;
	}

	public void clear() {
		slots.clear();
		renderElementsByType.clear();
		renderOverlays.clear();
		blocked = 0;
	}

	public int size() {
		return slots.size() - blocked;
	}

	public void add(IngredientListSlot ingredientListSlot) {
		slots.add(ingredientListSlot);
		addRenderElement(ingredientListSlot);
	}

	private void addRenderElement(IngredientListSlot ingredientListSlot) {
		ingredientListSlot.getOptionalElement()
			.ifPresent(element -> {
				ITypedIngredient<?> typedIngredient = element.getTypedIngredient();
				IIngredientType<?> ingredientType = typedIngredient.getType();
				ImmutableRect2i renderArea = ingredientListSlot.getRenderArea();
				BatchRenderElement<?> batchRenderElement = new BatchRenderElement<>(typedIngredient.getIngredient(), renderArea.x(), renderArea.y());
				renderElementsByType.put(ingredientType, batchRenderElement);
				IDrawable renderOverlay = element.createRenderOverlay();
				if (renderOverlay != null) {
					renderOverlays.add(OffsetDrawable.create(renderOverlay, renderArea.x(), renderArea.y()));
				}
			});
	}

	public Stream<IngredientListSlot> getSlots() {
		return slots.stream()
			.filter(s -> !s.isBlocked());
	}

	public List<IngredientListSlot> getAllSlots() {
		return slots;
	}

	public void set(final int startIndex, List<IElement<?>> ingredientList) {
		blocked = 0;
		renderElementsByType.clear();
		renderOverlays.clear();

		Iterator<IElement<?>> elementIterator = ingredientList.listIterator(startIndex);

		for (IngredientListSlot ingredientListSlot : slots) {
			if (ingredientListSlot.isBlocked()) {
				ingredientListSlot.clear();
				blocked++;
			} else if (elementIterator.hasNext()) {
				IElement<?> element = elementIterator.next();
				while (!element.isVisible() && elementIterator.hasNext()) {
					element = elementIterator.next();
				}
				if (element.isVisible()) {
					ingredientListSlot.setElement(element);
					addRenderElement(ingredientListSlot);
				} else {
					ingredientListSlot.clear();
				}
			} else {
				ingredientListSlot.clear();
			}
		}
	}

	public void render(GuiGraphicsExtractor guiGraphics) {
		if (searchable && Internal.getClientToggleState().isEditModeEnabled()) {
			renderEditMode(guiGraphics);
		}

		for (Map.Entry<IIngredientType<?>, List<BatchRenderElement<?>>> entry : renderElementsByType.entrySet()) {
			renderBatch(guiGraphics, entry);
		}

		for (IDrawable overlay : renderOverlays) {
			overlay.draw(guiGraphics);
		}
	}

	private <T> void renderBatch(GuiGraphicsExtractor guiGraphics, Map.Entry<IIngredientType<?>, List<BatchRenderElement<?>>> entry) {
		@SuppressWarnings("unchecked")
		IIngredientType<T> type = (IIngredientType<T>) entry.getKey();
		IIngredientRenderer<T> ingredientRenderer = ingredientManager.getIngredientRenderer(type);
		@SuppressWarnings("unchecked")
		List<BatchRenderElement<T>> elements = (List<BatchRenderElement<T>>) (Object) entry.getValue();
		SafeIngredientUtil.renderBatch(guiGraphics, type, ingredientRenderer, elements);
	}

	private void renderEditMode(GuiGraphicsExtractor guiGraphics) {
		IEditModeConfig editModeConfig = Internal.getJivRuntime().getEditModeConfig();

		for (IngredientListSlot slot : slots) {
			slot.getOptionalElement()
				.ifPresent(element -> {
					renderEditMode(guiGraphics, slot.getArea(), slot.getPadding(), element.getTypedIngredient(), editModeConfig);
				});
		}
	}

	private static <T> void renderEditMode(GuiGraphicsExtractor guiGraphics, ImmutableRect2i area, int padding, ITypedIngredient<T> typedIngredient, IEditModeConfig config) {
		Set<IEditModeConfig.HideMode> hideModes = config.getIngredientHiddenUsingConfigFile(typedIngredient);
		if (!hideModes.isEmpty()) {
			boolean wildcard = hideModes.contains(IEditModeConfig.HideMode.WILDCARD);
			boolean single = hideModes.contains(IEditModeConfig.HideMode.SINGLE);
			int wildcardColor = JivGuiColors.getColor(GuiColor.EDIT_MODE_HIDDEN_WILDCARD_OVERLAY);
			int singleColor = JivGuiColors.getColor(GuiColor.EDIT_MODE_HIDDEN_INGREDIENT_OVERLAY);
			if (wildcard && single) {
				guiGraphics.fill(
					area.getX() + padding,
					area.getY() + padding,
					area.getX() + 16 + padding,
					area.getY() + 8 + padding,
					wildcardColor
				);
				guiGraphics.fill(
					area.getX() + padding,
					area.getY() + 8 + padding,
					area.getX() + 16 + padding,
					area.getY() + 16 + padding,
					singleColor
				);
			} else if (wildcard) {
				guiGraphics.fill(
					area.getX() + padding,
					area.getY() + padding,
					area.getX() + 16 + padding,
					area.getY() + 16 + padding,
					wildcardColor
				);
			} else if (single) {
				guiGraphics.fill(
					area.getX() + padding,
					area.getY() + padding,
					area.getX() + 16 + padding,
					area.getY() + 16 + padding,
					singleColor
				);
			}
		}
	}
}
