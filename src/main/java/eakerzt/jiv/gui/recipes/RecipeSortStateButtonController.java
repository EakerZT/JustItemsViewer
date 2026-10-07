package eakerzt.jiv.gui.recipes;

import eakerzt.jiv.api.gui.builder.ITooltipBuilder;
import eakerzt.jiv.api.gui.drawable.IDrawable;
import eakerzt.jiv.api.gui.inputs.IJivUserInput;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.config.IClientConfig;
import eakerzt.jiv.common.config.IClientConfigs;
import eakerzt.jiv.common.config.RecipeSorterStage;
import eakerzt.jiv.api.gui.buttons.IButtonState;
import eakerzt.jiv.api.gui.buttons.IIconButtonController;
import net.minecraft.network.chat.Component;

public class RecipeSortStateButtonController implements IIconButtonController {
	private final IDrawable offIcon;
	private final IDrawable onIcon;
	private final RecipeSorterStage recipeSorterStage;
	private final Component disabledTooltip;
	private final Component enabledTooltip;
	private final Runnable onValueChanged;
	private boolean toggledOn;

	public RecipeSortStateButtonController(
		RecipeSorterStage recipeSorterStage,
		IDrawable offIcon,
		IDrawable onIcon,
		Component disabledTooltip,
		Component enabledTooltip,
		Runnable onValueChanged
	) {
		this.offIcon = offIcon;
		this.onIcon = onIcon;
		this.recipeSorterStage = recipeSorterStage;
		this.disabledTooltip = disabledTooltip;
		this.enabledTooltip = enabledTooltip;
		this.onValueChanged = onValueChanged;
	}

	@Override
	public void getTooltips(ITooltipBuilder tooltip) {
		if (toggledOn) {
			tooltip.add(enabledTooltip);
		} else {
			tooltip.add(disabledTooltip);
		}
	}

	@Override
	public void updateState(IButtonState state) {
		IClientConfigs jivClientConfigs = Internal.getClientConfigs();
		IClientConfig clientConfig = jivClientConfigs.getClientConfig();
		boolean toggledOn = recipeSorterStage.isEnabled(clientConfig);
		if (toggledOn != this.toggledOn) {
			this.toggledOn = toggledOn;
			this.onValueChanged.run();
		}
		if (toggledOn) {
			state.setForcePressed(true);
			state.setIcon(onIcon);
		} else {
			state.setForcePressed(false);
			state.setIcon(offIcon);
		}
	}

	@Override
	public boolean onPress(IJivUserInput input) {
		if (!input.isSimulate()) {
			IClientConfigs jivClientConfigs = Internal.getClientConfigs();
			IClientConfig clientConfig = jivClientConfigs.getClientConfig();
			this.toggledOn = !this.toggledOn;
			recipeSorterStage.setEnabled(clientConfig, this.toggledOn);
			this.onValueChanged.run();
		}
		return true;
	}
}
