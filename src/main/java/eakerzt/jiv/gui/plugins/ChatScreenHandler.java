package eakerzt.jiv.gui.plugins;

import eakerzt.jiv.api.constants.VanillaTypes;
import eakerzt.jiv.api.gui.builder.IClickableIngredientFactory;
import eakerzt.jiv.api.gui.handlers.IGuiProperties;
import eakerzt.jiv.api.gui.handlers.IScreenHandler;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.runtime.IClickableIngredient;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.chat.JivChatItemLinkHover;
import eakerzt.jiv.common.chat.JivChatItemLinks;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class ChatScreenHandler implements IScreenHandler<ChatScreen> {
	private final IIngredientManager ingredientManager;

	public ChatScreenHandler(IIngredientManager ingredientManager) {
		this.ingredientManager = ingredientManager;
	}

	@Override
	@Nullable
	public IGuiProperties apply(ChatScreen chatScreen) {
		return null;
	}

	@Override
	public Optional<? extends IClickableIngredient<?>> getClickableIngredientUnderMouse(
		IClickableIngredientFactory factory,
		ChatScreen chatScreen,
		double mouseX,
		double mouseY
	) {
		return JivChatItemLinkHover.getHoveredText(chatScreen, mouseX, mouseY)
			.flatMap(hoveredText -> {
				return getIngredient(hoveredText.style())
					.flatMap(typedIngredient -> factory.createBuilder(typedIngredient)
						.buildWithArea(hoveredText.area()));
			});
	}

	private Optional<ITypedIngredient<?>> getIngredient(Style style) {
		return getJivChatLinkIngredient(style)
			.or(() -> getVanillaChatItemIngredient(style));
	}

	private Optional<ITypedIngredient<?>> getJivChatLinkIngredient(Style style) {
		return JivChatItemLinkHover.getIngredientLink(style)
			.flatMap(link -> JivChatItemLinks.resolveTypedIngredient(link, ingredientManager));
	}

	private Optional<ITypedIngredient<ItemStack>> getVanillaChatItemIngredient(Style style) {
		HoverEvent hoverEvent = style.getHoverEvent();
		if (hoverEvent instanceof HoverEvent.ShowItem(ItemStackTemplate item)) {
			ItemStack itemStack = item.create();
			return ingredientManager.createTypedIngredient(
				VanillaTypes.ITEM_STACK,
				itemStack,
				false
			);
		}
		return Optional.empty();
	}

}
