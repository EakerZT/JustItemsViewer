package eakerzt.jiv.api.helpers;

import com.mojang.serialization.Codec;
import eakerzt.jiv.api.IModPlugin;
import eakerzt.jiv.api.recipe.IFocusFactory;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import eakerzt.jiv.api.recipe.vanilla.IVanillaRecipeFactory;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.api.runtime.IIngredientVisibility;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * {@link IJivHelpers} provides helpers and tools for addon mods.
 * <p>
 * An instance is passed to your {@link IModPlugin}'s registration methods.
 */
@ApiStatus.NonExtendable
public interface IJivHelpers {
	/**
	 * Helps with the implementation of GUIs.
	 */
	IGuiHelper getGuiHelper();

	/**
	 * Helps with getting itemStacks from recipes.
	 */
	IStackHelper getStackHelper();

	/**
	 * Helps with getting the mod name from a mod ID.
	 */
	IModIdHelper getModIdHelper();

	/**
	 * Helps with creating focuses.
	 *
	 * @since 9.4.0
	 */
	IFocusFactory getFocusFactory();

	/**
	 * Helps with getting colors of ingredients.
	 *
	 * @since 11.5.0
	 */
	IColorHelper getColorHelper();

	/**
	 * Helps with handling fluid ingredients on multiple mod loader platforms.
	 *
	 * @since 10.1.0
	 */
	IPlatformFluidHelper<?> getPlatformFluidHelper();

	/**
	 * Get the registered recipe type for the given unique id.
	 * <p>
	 * This is useful for integrating with other mods that do not share their
	 * recipe types directly from their API.
	 *
	 * @see IRecipeType#getUid()
	 * @since 19.11.0
	 */
	<T> Optional<IRecipeType<T>> getRecipeType(Identifier uid, Class<? extends T> recipeClass);

	/**
	 * Get the registered recipe type for the given unique id.
	 * <p>
	 * This is useful for integrating with other mods that do not share their
	 * recipe types directly from their API.
	 *
	 * @see IRecipeType#getUid()
	 * @since 11.4.0
	 */
	Optional<IRecipeType<?>> getRecipeType(Identifier uid);

	/**
	 * Get all registered recipe types.
	 *
	 * @since 15.1.0
	 */
	Stream<IRecipeType<?>> getAllRecipeTypes();

	/**
	 * The ingredient manager, with information about all registered ingredients.
	 *
	 * @since 11.5.0
	 */
	IIngredientManager getIngredientManager();

	/**
	 * Helps with implementing various {@link Codec}s.
	 *
	 * @since 19.9.0
	 */
	ICodecHelper getCodecHelper();

	/**
	 * The {@link IVanillaRecipeFactory} allows creation of vanilla recipes.
	 *
	 * @since 19.15.0
	 */
	IVanillaRecipeFactory getVanillaRecipeFactory();

	/**
	 * The {@link IIngredientVisibility} allows mod plugins to do advanced filtering of
	 * ingredients based on what is visible in JIV.
	 *
	 * @since 19.18.4
	 */
	IIngredientVisibility getIngredientVisibility();
}
