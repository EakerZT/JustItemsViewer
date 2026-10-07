package eakerzt.jiv.library.plugins.vanilla.anvil;

import eakerzt.jiv.api.recipe.vanilla.IJivAnvilRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record AnvilRecipe(
	List<ItemStack> leftInputs,
	List<ItemStack> rightInputs,
	List<ItemStack> outputs,
	@Nullable Identifier uid
) implements IJivAnvilRecipe {

	@Override
	public List<ItemStack> getLeftInputs() {
		return leftInputs;
	}

	@Override
	public List<ItemStack> getRightInputs() {
		return rightInputs;
	}

	@Override
	public List<ItemStack> getOutputs() {
		return outputs;
	}

	@Override
	@Nullable
	public Identifier getUid() {
		return uid;
	}
}
