package eakerzt.jiv.api.recipe.advanced;

import eakerzt.jiv.api.recipe.IFocus;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import org.jetbrains.annotations.ApiStatus;

/**
 * Helpers for implementing {@link IRecipeManagerPlugin}s.
 *
 * @since 19.15.1
 */
@ApiStatus.NonExtendable
public interface IRecipeManagerPluginHelper {
	/**
	 * @return true if the given focus should be treated as a crafting station of this recipe type.
	 * @since 20.0.0
	 */
	boolean isCraftingStation(IRecipeType<?> recipeType, IFocus<?> focus);

}
