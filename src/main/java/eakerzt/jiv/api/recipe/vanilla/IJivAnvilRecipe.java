package eakerzt.jiv.api.recipe.vanilla;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * There is no vanilla registry of Anvil Recipes,
 * so JIV creates these Anvil recipes to use internally.
 *
 * Create your own with {@link IVanillaRecipeFactory#createAnvilRecipe}
 */
@ApiStatus.NonExtendable
public interface IJivAnvilRecipe {
	/**
	 * Get the inputs that go into the left slot of the Anvil.
	 *
	 * @since 9.5.0
	 */
	@Unmodifiable
	List<ItemStack> getLeftInputs();

	/**
	 * Get the inputs that go into the right slot of the Anvil.
	 *
	 * @since 9.5.0
	 */
	@Unmodifiable
	List<ItemStack> getRightInputs();

	/**
	 * Get the outputs of the Anvil recipe.
	 *
	 * @since 9.5.0
	 */
	@Unmodifiable
	List<ItemStack> getOutputs();

	/**
	 * Unique ID for this recipe.
	 * @since 19.1.0
	 */
	@Nullable
	Identifier getUid();
}
