package eakerzt.jiv.gui.ingredients;

import eakerzt.jiv.api.ingredients.IIngredientHelper;
import eakerzt.jiv.common.util.StringUtil;
import eakerzt.jiv.common.util.Translator;

public final class DisplayNameUtil {
	private DisplayNameUtil() {
	}

	public static <T> String getLowercaseDisplayNameForSearch(T ingredient, IIngredientHelper<T> ingredientHelper) {
		String displayName = ingredientHelper.getDisplayName(ingredient);
		displayName = StringUtil.removeChatFormatting(displayName);
		displayName = Translator.toLowercaseWithLocale(displayName);
		return displayName;
	}

}
