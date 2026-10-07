package eakerzt.jiv.neoforge.platform;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import eakerzt.jiv.common.input.MouseButtonEventData;
import eakerzt.jiv.common.input.keys.IJivKeyMappingCategoryBuilder;
import eakerzt.jiv.common.platform.IPlatformInputHelper;
import eakerzt.jiv.neoforge.input.ForgeJivKeyMappingCategoryBuilder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.client.ClientTooltipFlag;
import net.neoforged.neoforge.common.extensions.TooltipFlagExtension;

public class InputHelper implements IPlatformInputHelper {
	@Override
	public boolean isActiveAndMatches(KeyMapping keyMapping, InputConstants.Key key, Either<MouseButtonEventData, KeyEvent> event) {
		return keyMapping.isActiveAndMatches(key);
	}

	@Override
	public IJivKeyMappingCategoryBuilder createKeyMappingCategoryBuilder(KeyMapping.Category category) {
		return new ForgeJivKeyMappingCategoryBuilder(category);
	}

	@Override
	public TooltipFlag getClientTooltipFlag(TooltipFlag tooltipFlag) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft == null || tooltipFlag instanceof ClientTooltipFlag) {
			return tooltipFlag;
		}
		return ClientTooltipFlag.of(tooltipFlag);
	}

	@Override
	public TooltipFlag getSearchTooltipFlag(TooltipFlag tooltipFlag) {
		return new SearchTooltipFlag(tooltipFlag.isAdvanced(), tooltipFlag.isCreative());
	}

	private record SearchTooltipFlag(boolean advanced, boolean creative) implements TooltipFlag, TooltipFlagExtension {
		@Override
		public boolean isAdvanced() {
			return advanced;
		}

		@Override
		public boolean isCreative() {
			return creative;
		}

		@Override
		public boolean shouldDisplayAllInformation() {
			return true;
		}
	}
}
