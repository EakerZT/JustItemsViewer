package eakerzt.jiv.common.platform;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import eakerzt.jiv.common.input.MouseButtonEventData;
import eakerzt.jiv.common.input.keys.IJivKeyMappingCategoryBuilder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.item.TooltipFlag;

public interface IPlatformInputHelper {
	boolean isActiveAndMatches(KeyMapping keyMapping, InputConstants.Key key, Either<MouseButtonEventData, KeyEvent> event);

	IJivKeyMappingCategoryBuilder createKeyMappingCategoryBuilder(KeyMapping.Category category);

	default TooltipFlag getClientTooltipFlag(TooltipFlag tooltipFlag) {
		return tooltipFlag;
	}

	default TooltipFlag getSearchTooltipFlag(TooltipFlag tooltipFlag) {
		return tooltipFlag;
	}
}
