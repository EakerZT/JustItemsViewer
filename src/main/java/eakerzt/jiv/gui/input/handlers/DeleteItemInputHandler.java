package eakerzt.jiv.gui.input.handlers;

import eakerzt.jiv.api.gui.handlers.IGuiProperties;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.config.GiveMode;
import eakerzt.jiv.common.config.IClientConfig;
import eakerzt.jiv.common.config.IClientToggleState;
import eakerzt.jiv.common.gui.JivTooltip;
import eakerzt.jiv.common.input.IInternalKeyMappings;
import eakerzt.jiv.common.network.IConnectionToServer;
import eakerzt.jiv.common.network.packets.PacketDeletePlayerItem;
import eakerzt.jiv.common.util.ServerCommandUtil;
import eakerzt.jiv.common.input.IUserInputHandler;
import eakerzt.jiv.common.input.UserInput;
import eakerzt.jiv.gui.overlay.ingredients.IIngredientGrid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class DeleteItemInputHandler implements IUserInputHandler {
	private final IIngredientGrid ingredientGrid;
	private final IClientToggleState toggleState;
	private final IClientConfig clientConfig;
	private final IConnectionToServer serverConnection;
	private final IIngredientManager ingredientManager;

	public DeleteItemInputHandler(
		IIngredientGrid ingredientGrid,
		IClientToggleState toggleState,
		IClientConfig clientConfig,
		IConnectionToServer serverConnection,
		IIngredientManager ingredientManager
	) {
		this.ingredientGrid = ingredientGrid;
		this.toggleState = toggleState;
		this.clientConfig = clientConfig;
		this.serverConnection = serverConnection;
		this.ingredientManager = ingredientManager;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, IGuiProperties guiProperties, UserInput userInput, IInternalKeyMappings keyBindings) {
		if (!userInput.is(keyBindings.getLeftClick())) {
			return Optional.empty();
		}
		double mouseX = userInput.getMouseX();
		double mouseY = userInput.getMouseY();
		if (!this.ingredientGrid.isMouseOver(mouseX, mouseY)) {
			return Optional.empty();
		}
		Minecraft minecraft = Minecraft.getInstance();
		if (!shouldDeleteItemOnClick(minecraft, mouseX, mouseY)) {
			return Optional.empty();
		}
		LocalPlayer player = minecraft.player;
		if (player == null) {
			return Optional.empty();
		}
		ItemStack itemStack = player.containerMenu.getCarried();
		if (itemStack.isEmpty()) {
			return Optional.empty();
		}
		if (!userInput.isSimulate()) {
			player.containerMenu.setCarried(ItemStack.EMPTY);
			if (!(player.containerMenu instanceof CreativeModeInventoryScreen.ItemPickerMenu)) {
				var packet = new PacketDeletePlayerItem(itemStack);
				serverConnection.sendPacketToServer(packet);
			}
		}
		return Optional.of(this);
	}

	@SuppressWarnings("MethodMayBeStatic")
	public void drawTooltips(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		JivTooltip tooltip = new JivTooltip();
		tooltip.add(Component.translatable("jiv.tooltip.delete.item"));
		tooltip.draw(guiGraphics, mouseX, mouseY);
	}

	public boolean shouldDeleteItemOnClick(Minecraft minecraft, double mouseX, double mouseY) {
		if (!toggleState.isCheatItemsEnabled() || !serverConnection.isJivOnServer()) {
			return false;
		}
		Player player = minecraft.player;
		if (player == null) {
			return false;
		}
		ItemStack itemStack = player.containerMenu.getCarried();
		if (itemStack.isEmpty()) {
			return false;
		}
		GiveMode giveMode = this.clientConfig.giveMode().get();
		if (giveMode == GiveMode.MOUSE_PICKUP) {
			return this.ingredientGrid.getIngredientUnderMouse(mouseX, mouseY)
				.findFirst()
				.map(c -> c.getCheatItemStack(ingredientManager))
				.map(i -> !ServerCommandUtil.canStack(itemStack, i))
				.orElse(true);
		}
		return true;
	}
}
