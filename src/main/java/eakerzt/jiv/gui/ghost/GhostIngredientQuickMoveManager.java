package eakerzt.jiv.gui.ghost;

import eakerzt.jiv.api.gui.handlers.IGhostIngredientHandler;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.runtime.IScreenHelper;
import eakerzt.jiv.gui.input.IDraggableIngredientInternal;
import eakerzt.jiv.gui.input.IRecipeFocusSource;
import eakerzt.jiv.common.input.UserInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class GhostIngredientQuickMoveManager {
	private final IRecipeFocusSource source;
	private final IScreenHelper screenHelper;

	public GhostIngredientQuickMoveManager(
		IRecipeFocusSource source,
		IScreenHelper screenHelper
	) {
		this.source = source;
		this.screenHelper = screenHelper;
	}

	private <T extends Screen, V> boolean quickMoveInternal(T currentScreen, UserInput input, IDraggableIngredientInternal<V> clicked) {
		for (IGhostIngredientHandler<T> handler : screenHelper.getGhostIngredientHandlers(currentScreen)) {
			if (input.isSimulate()) {
				return true;
			}
			ITypedIngredient<V> ingredient = clicked.getTypedIngredient();
			if (handler.quickMove(currentScreen, ingredient)) {
				return true;
			}
		}

		return false;
	}

	public <T extends Screen> boolean quickMove(T screen, UserInput input) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null) {
			return false;
		}

		return source.getDraggableIngredientUnderMouse(input.getMouseX(), input.getMouseY())
			.findFirst()
			.flatMap(clicked -> {
				ItemStack mouseItem = player.containerMenu.getCarried();
				if (mouseItem.isEmpty()) {
					if (quickMoveInternal(screen, input, clicked)) {
						return Optional.of(true);
					}
				}
				return Optional.empty();
			})
			.isPresent();
	}

}
