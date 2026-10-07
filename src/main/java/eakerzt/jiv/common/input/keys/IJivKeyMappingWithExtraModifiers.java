package eakerzt.jiv.common.input.keys;

import com.mojang.blaze3d.platform.InputConstants;
import eakerzt.jiv.api.runtime.IJivKeyMapping;

public interface IJivKeyMappingWithExtraModifiers extends IJivKeyMapping {
	boolean isActiveAndMatchesAllowingExtraModifiers(InputConstants.Key key);
}
