package eakerzt.jiv.library.plugins.vanilla.cooking.fuel;

import eakerzt.jiv.api.constants.RecipeTypes;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.common.gui.textures.Textures;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public class SmokingFuelCategory extends AbstractFuelCategory {
	public SmokingFuelCategory(IGuiHelper guiHelper, Textures textures) {
		super(
			textures,
			RecipeTypes.SMOKING_FUEL,
			Component.translatable("gui.jiv.category.smoking_fuel"),
			guiHelper.createDrawableItemLike(Items.SMOKER),
			2
		);
	}
}
