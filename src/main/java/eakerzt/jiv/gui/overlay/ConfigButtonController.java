package eakerzt.jiv.gui.overlay;

import eakerzt.jiv.api.constants.ModIds;
import eakerzt.jiv.api.gui.builder.ITooltipBuilder;
import eakerzt.jiv.api.gui.buttons.IButtonState;
import eakerzt.jiv.api.gui.buttons.IIconButtonController;
import eakerzt.jiv.api.gui.drawable.IDrawable;
import eakerzt.jiv.api.gui.inputs.IJivUserInput;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.config.IClientToggleState;
import eakerzt.jiv.common.gui.textures.Textures;
import eakerzt.jiv.common.input.IInternalKeyMappings;
import eakerzt.jiv.common.platform.IPlatformConfigHelper;
import eakerzt.jiv.common.platform.Services;
import eakerzt.jiv.gui.util.CheatModeUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.Optional;
import java.util.function.BooleanSupplier;

public class ConfigButtonController implements IIconButtonController {
	private final IDrawable normalIcon;
	private final IDrawable cheatIcon;
	private final BooleanSupplier isListDisplayed;
	private final IClientToggleState toggleState;
	private final IInternalKeyMappings keyBindings;

	public ConfigButtonController(BooleanSupplier isListDisplayed, IClientToggleState toggleState, IInternalKeyMappings keyBindings) {
		Textures textures = Internal.getTextures();
		this.normalIcon = textures.getConfigButtonIcon();
		this.cheatIcon = textures.getConfigButtonCheatIcon();
		this.isListDisplayed = isListDisplayed;
		this.toggleState = toggleState;
		this.keyBindings = keyBindings;
	}

	@Override
	public void updateState(IButtonState state) {
		if (toggleState.isCheatItemsEnabled()) {
			state.setIcon(cheatIcon);
		} else {
			state.setIcon(normalIcon);
		}
	}

	@Override
	public void getTooltips(ITooltipBuilder tooltip) {
		tooltip.add(Component.translatable("jiv.tooltip.config"));
		if (!toggleState.isOverlayEnabled()) {
			tooltip.add(
				Component.translatable("jiv.tooltip.ingredient.list.disabled")
					.withStyle(ChatFormatting.GOLD)
			);
			tooltip.addKeyUsageComponent(
				"jiv.tooltip.ingredient.list.disabled.how.to.fix",
				keyBindings.getToggleOverlay()
			);
		} else if (!isListDisplayed.getAsBoolean()) {
			tooltip.add(
				Component.translatable("jiv.tooltip.not.enough.space")
					.withStyle(ChatFormatting.GOLD)
			);
		}
		if (toggleState.isCheatItemsEnabled()) {
			tooltip.add(
				Component.translatable("jiv.tooltip.cheat.mode.button.enabled")
					.withStyle(ChatFormatting.RED)
			);

			if (!keyBindings.getToggleCheatMode().isUnbound()) {
				tooltip.addKeyUsageComponent(
					"jiv.tooltip.cheat.mode.how.to.disable.hotkey",
					keyBindings.getToggleCheatMode()
				);
			} else if (!keyBindings.getToggleCheatModeConfigButton().isUnbound()) {
				tooltip.addKeyUsageComponent(
					"jiv.tooltip.cheat.mode.how.to.disable.hover.config.button.hotkey",
					keyBindings.getToggleCheatModeConfigButton()
				);
			}
		}
	}

	@Override
	public boolean onPress(IJivUserInput input) {
		if (toggleState.isOverlayEnabled()) {
			if (!input.isSimulate()) {
				if (input.is(keyBindings.getToggleCheatModeConfigButton())) {
					CheatModeUtil.toggleCheatMode(toggleState);
				} else {
					openSettings();
				}
			}
			return true;
		}
		return false;
	}

	private static void openSettings() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return;
		}

		IPlatformConfigHelper configHelper = Services.PLATFORM.getConfigHelper();
		Optional<Screen> configScreen = configHelper.getConfigScreen(ModIds.JIV_ID, mc.screen);

		if (configScreen.isPresent()) {
			mc.setScreen(configScreen.get());
		} else {
			Component message = getMissingConfigScreenMessage(configHelper);
			mc.player.sendSystemMessage(message);
		}
	}

	private static Component getMissingConfigScreenMessage(IPlatformConfigHelper configHelper) {
		return Component.translatable("jiv.message.config.unavailable")
			.append("\n")
			.append(
				Component.translatable("jiv.message.config.folder")
					.setStyle(
						Style.EMPTY
							.withColor(ChatFormatting.WHITE)
							.withUnderlined(true)
							.withClickEvent(
								new ClickEvent.OpenFile(
									configHelper.createJivConfigDir().toAbsolutePath().toString()
								)
							)
					)
			);
	}
}
