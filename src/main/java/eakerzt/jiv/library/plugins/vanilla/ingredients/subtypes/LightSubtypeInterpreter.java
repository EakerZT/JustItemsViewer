package eakerzt.jiv.library.plugins.vanilla.ingredients.subtypes;

import eakerzt.jiv.api.ingredients.subtypes.ISubtypeInterpreter;
import eakerzt.jiv.api.ingredients.subtypes.UidContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.LightBlock;
import org.jspecify.annotations.Nullable;

public class LightSubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {
	public static final LightSubtypeInterpreter INSTANCE = new LightSubtypeInterpreter();

	private LightSubtypeInterpreter() {

	}

	@Override
	public @Nullable Object getSubtypeData(ItemStack ingredient, UidContext context) {
		BlockItemStateProperties properties = ingredient.get(DataComponents.BLOCK_STATE);
		if (properties == null) {
			return null;
		}
		return properties.get(LightBlock.LEVEL);
	}
}
