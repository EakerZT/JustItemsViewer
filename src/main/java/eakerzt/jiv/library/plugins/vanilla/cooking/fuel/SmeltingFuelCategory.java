package eakerzt.jiv.library.plugins.vanilla.cooking.fuel;

import eakerzt.jiv.api.constants.RecipeTypes;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.common.gui.textures.Textures;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public class SmeltingFuelCategory extends AbstractFuelCategory {
	public SmeltingFuelCategory(IGuiHelper guiHelper, Textures textures) {
		super(
			textures,
			RecipeTypes.SMELTING_FUEL,
			Component.translatable("gui.jiv.category.smelting_fuel"),
			guiHelper.createDrawableItemLike(Items.FURNACE),
			1
		);
	}
}
