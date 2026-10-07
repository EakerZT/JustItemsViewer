package eakerzt.jiv.common;

import eakerzt.jiv.api.constants.ModIds;
import eakerzt.jiv.api.recipe.types.IRecipeType;

public final class Constants {
	public static final IRecipeType<?> UNIVERSAL_RECIPE_TRANSFER_TYPE = IRecipeType.create(ModIds.JIV_ID, "universal_recipe_transfer_handler", Object.class);
	private Constants() {

	}
}
