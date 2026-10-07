package eakerzt.jiv.gui.chat;

import eakerzt.jiv.api.ingredients.IIngredientRenderer;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.api.runtime.IJivRuntime;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.chat.JivChatItemLinkHover;
import eakerzt.jiv.common.chat.JivChatItemLinks;
import eakerzt.jiv.common.chat.JivChatItemLinks.IngredientLink;
import eakerzt.jiv.common.gui.IngredientTooltipComponent;
import eakerzt.jiv.common.gui.JivTooltip;
import eakerzt.jiv.common.util.SafeIngredientUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public final class ChatIngredientTooltip {
	private ChatIngredientTooltip() {
	}

	public record IngredientTooltipData<T>(
		ITypedIngredient<T> typedIngredient,
		IIngredientRenderer<T> ingredientRenderer,
		IIngredientManager ingredientManager,
		JivTooltip tooltip
	) {
		public void draw(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
			tooltip.draw(guiGraphics, mouseX, mouseY, typedIngredient, ingredientRenderer, ingredientManager);
		}
	}

	public static boolean setTooltipForHoveredText(
		GuiGraphicsExtractor guiGraphics,
		@Nullable Style hoveredStyle,
		int mouseX,
		int mouseY
	) {
		Optional<IngredientTooltipData<?>> optionalTooltipData = getTooltipForHoveredText(hoveredStyle);
		if (optionalTooltipData.isEmpty()) {
			return false;
		}

		IngredientTooltipData<?> tooltipData = optionalTooltipData.get();
		tooltipData.draw(guiGraphics, mouseX, mouseY);
		return true;
	}

	public static Optional<IngredientTooltipData<?>> getTooltipForHoveredChatLink(@Nullable Screen screen, double mouseX, double mouseY) {
		if (screen == null) {
			return Optional.empty();
		}
		return JivChatItemLinkHover.getHoveredStyle(screen, mouseX, mouseY)
			.flatMap(ChatIngredientTooltip::getTooltipForHoveredText);
	}

	public static Optional<IngredientTooltipData<?>> getTooltipForHoveredText(@Nullable Style hoveredStyle) {
		Optional<IngredientLink> optionalLink = JivChatItemLinkHover.getIngredientLink(hoveredStyle);
		if (optionalLink.isEmpty()) {
			return Optional.empty();
		}

		Optional<IJivRuntime> optionalRuntime = Internal.getOptionalJivRuntime();
		if (optionalRuntime.isEmpty()) {
			return Optional.empty();
		}

		IJivRuntime jivRuntime = optionalRuntime.get();
		IIngredientManager ingredientManager = jivRuntime.getIngredientManager();
		IngredientLink link = optionalLink.get();
		Optional<ITypedIngredient<?>> optionalTypedIngredient = JivChatItemLinks.resolveTypedIngredient(link, ingredientManager);
		if (optionalTypedIngredient.isEmpty()) {
			return Optional.empty();
		}

		ITypedIngredient<?> typedIngredient = optionalTypedIngredient.get();
		IngredientTooltipData<?> tooltipData = createTooltipData(typedIngredient, ingredientManager);
		return Optional.of(tooltipData);
	}

	private static <T> IngredientTooltipData<T> createTooltipData(
		ITypedIngredient<T> typedIngredient,
		IIngredientManager ingredientManager
	) {
		IIngredientRenderer<T> ingredientRenderer = ingredientManager.getIngredientRenderer(typedIngredient.getType());
		JivTooltip tooltip = new JivTooltip();
		tooltip.add(new IngredientTooltipComponent<>(typedIngredient, ingredientRenderer));
		SafeIngredientUtil.getRichTooltip(tooltip, ingredientManager, ingredientRenderer, typedIngredient);
		return new IngredientTooltipData<>(typedIngredient, ingredientRenderer, ingredientManager, tooltip);
	}
}
