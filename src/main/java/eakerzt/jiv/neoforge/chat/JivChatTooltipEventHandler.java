package eakerzt.jiv.neoforge.chat;

import eakerzt.jiv.common.gui.JivTooltip;
import eakerzt.jiv.common.gui.JivTooltip.TooltipRenderData;
import eakerzt.jiv.gui.chat.ChatIngredientTooltip;
import eakerzt.jiv.gui.chat.ChatIngredientTooltip.IngredientTooltipData;
import eakerzt.jiv.neoforge.events.PermanentEventSubscriptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.component.DataComponents;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

import java.util.List;
import java.util.Optional;

public final class JivChatTooltipEventHandler {
	private static boolean renderingJivChatTooltip;

	private JivChatTooltipEventHandler() {
	}

	public static void register(PermanentEventSubscriptions subscriptions) {
		subscriptions.register(RenderTooltipEvent.Pre.class, JivChatTooltipEventHandler::onRenderTooltipPre);
	}

	private static void onRenderTooltipPre(RenderTooltipEvent.Pre event) {
		if (renderingJivChatTooltip) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		Screen screen = minecraft.screen;
		Optional<IngredientTooltipData<?>> optionalTooltipData = ChatIngredientTooltip.getTooltipForHoveredChatLink(
			screen,
			event.getX(),
			event.getY()
		);
		if (optionalTooltipData.isEmpty()) {
			return;
		}

		IngredientTooltipData<?> tooltipData = optionalTooltipData.get();
		if (renderJivChatTooltip(event, tooltipData)) {
			event.setCanceled(true);
		}
	}

	private static <T> boolean renderJivChatTooltip(RenderTooltipEvent.Pre event, IngredientTooltipData<T> tooltipData) {
		JivTooltip tooltip = tooltipData.tooltip();
		TooltipRenderData renderData = tooltip.prepareForIngredientTooltip(
			tooltipData.typedIngredient(),
			tooltipData.ingredientRenderer(),
			tooltipData.ingredientManager()
		);
		if (tooltip.isEmpty()) {
			return false;
		}

		List<ClientTooltipComponent> components = ClientHooks.gatherTooltipComponentsFromElements(
			renderData.itemStack(),
			tooltip.getLines(),
			event.getX(),
			event.getScreenWidth(),
			event.getScreenHeight(),
			renderData.font()
		);
		if (components.isEmpty()) {
			return false;
		}

		renderingJivChatTooltip = true;
		try {
			event.getGraphics().tooltip(
				renderData.font(),
				components,
				event.getX(),
				event.getY(),
				event.getTooltipPositioner(),
				renderData.itemStack().get(DataComponents.TOOLTIP_STYLE),
				renderData.itemStack()
			);
		} finally {
			renderingJivChatTooltip = false;
		}
		return true;
	}
}
