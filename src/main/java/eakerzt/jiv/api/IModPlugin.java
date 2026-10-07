package eakerzt.jiv.api;

import eakerzt.jiv.api.helpers.IPlatformFluidHelper;
import eakerzt.jiv.api.registration.IAdvancedRegistration;
import eakerzt.jiv.api.registration.IAdvancedSearchRegistration;
import eakerzt.jiv.api.registration.IExtraIngredientRegistration;
import eakerzt.jiv.api.registration.IGuiHandlerRegistration;
import eakerzt.jiv.api.registration.IIngredientAliasRegistration;
import eakerzt.jiv.api.registration.IModInfoRegistration;
import eakerzt.jiv.api.registration.IModIngredientRegistration;
import eakerzt.jiv.api.registration.IRecipeCatalystRegistration;
import eakerzt.jiv.api.registration.IRecipeCategoryRegistration;
import eakerzt.jiv.api.registration.IRecipeRegistration;
import eakerzt.jiv.api.registration.IRecipeTransferRegistration;
import eakerzt.jiv.api.registration.IRuntimeRegistration;
import eakerzt.jiv.api.registration.ISubtypeRegistration;
import eakerzt.jiv.api.registration.ISlotDisplayInterpreterRegistration;
import eakerzt.jiv.api.registration.IVanillaCategoryExtensionRegistration;
import eakerzt.jiv.api.runtime.IJivFeatures;
import eakerzt.jiv.api.runtime.IJivRuntime;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.display.SlotDisplay;

/**
 * The main class to implement to create a JIV plugin.
 * Everything communicated between a mod and JIV is through this class.
 * IModPlugins must have the {@link JivPlugin} annotation to get loaded by JIV.
 */

public interface IModPlugin {

	/**
	 * The unique ID for this mod plugin.
	 * The namespace of the ID should be your mod's modId.
	 */
	Identifier getPluginUid();

	/**
	 * Configure JIV feature changes.
	 *
	 * <p>
	 * This is called early, before JIV collects ingredients, recipes, GUI handlers, and runtime registrations.
	 * Use this for features that need to affect JIV startup, such as {@link IJivFeatures#disableJivGui()}.
	 * </p>
	 *
	 * @since 29.20.0
	 */
	default void configureJiv(IJivFeatures jivFeatures) {

	}

	/**
	 * If your item has subtypes that depend on NBT or capabilities, use this to help JIV identify those subtypes correctly.
	 */
	default void registerItemSubtypes(ISubtypeRegistration registration) {

	}

	/**
	 * If your fluid has subtypes that depend on NBT or capabilities,
	 * use this to help JIV identify those subtypes correctly.
	 *
	 * @since 10.1.0
	 */
	default <T> void registerFluidSubtypes(ISubtypeRegistration registration, IPlatformFluidHelper<T> platformFluidHelper) {

	}

	/**
	 * Register special ingredients, beyond the basic ItemStack and FluidStack.
	 */
	default void registerIngredients(IModIngredientRegistration registration) {

	}

	/**
	 * Tell JIV how slot displays used by your mod should be matched and described.
	 * <p>
	 * Use this when resolving a {@link SlotDisplay} does not give JIV enough information to understand
	 * the recipe slot. For example, a display may resolve a tag into stacks without preserving the tag name.
	 * <p>
	 * A registered interpreter can tell JIV to match all subtypes, show a more accurate tooltip,
	 * or show the tag represented by the display.
	 * You do not need to register anything here when the resolved ingredients already describe the slot accurately.
	 *
	 * @param registration used to register slot display interpreters
	 *
	 * @since 29.25.0
	 */
	default void registerSlotDisplayInterpreters(ISlotDisplayInterpreterRegistration registration) {

	}

	/**
	 * Register extra ItemStacks that are not in the creative menu,
	 * or FluidStacks that are different from the default ones available via the fluid registry.
	 *
	 * @since 19.18.0
	 */
	default void registerExtraIngredients(IExtraIngredientRegistration registration) {

	}

	/**
	 * Register search aliases for ingredients.
	 *
	 * @implNote The player's search config controls whether these aliases are used by JIV's search.
	 *
	 * @since 19.10.0
	 */
	default void registerIngredientAliases(IIngredientAliasRegistration registration) {

	}

	/**
	 * Register advanced custom search behavior for JIV.
	 *
	 * @since 29.16.0
	 */
	default void registerAdvancedSearch(IAdvancedSearchRegistration registration) {

	}

	/**
	 * Register extra info about a mod, such as aliases for the mod that users can search for.
	 *
	 * @since 17.1.0
	 */
	default void registerModInfo(IModInfoRegistration modAliasRegistration) {

	}

	/**
	 * Register the recipe categories handled by this plugin.
	 * These are registered before recipes so that they can be checked for validity.
	 */
	default void registerCategories(IRecipeCategoryRegistration registration) {

	}

	/**
	 * Register modded extensions to vanilla recipe categories.
	 * Custom crafting, smithing, and brewing recipes can use this to tell JIV how they work.
	 */
	default void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {

	}

	/**
	 * Register modded recipes.
	 */
	default void registerRecipes(IRecipeRegistration registration) {

	}

	/**
	 * Register recipe transfer handlers (move ingredients from the inventory into crafting GUIs).
	 */
	default void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {

	}

	/**
	 * Register recipe catalysts.
	 * Recipe Catalysts are ingredients that are needed in order to craft other things.
	 * Vanilla examples of Recipe Catalysts are the Crafting Table and Furnace.
	 */
	default void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {

	}

	/**
	 * Register various GUI-related things for your mod.
	 * This includes adding clickable areas in your guis to open JIV,
	 * and adding areas on the screen that JIV should avoid drawing.
	 */
	default void registerGuiHandlers(IGuiHandlerRegistration registration) {

	}

	/**
	 * Register advanced features for your mod plugin.
	 */
	default void registerAdvanced(IAdvancedRegistration registration) {

	}

	/**
	 * Override the default JIV runtime.
	 */
	default void registerRuntime(IRuntimeRegistration registration) {

	}

	/**
	 * Called when JIV's runtime features are available, after all mods have registered.
	 */
	default void onRuntimeAvailable(IJivRuntime jivRuntime) {

	}

	/**
	 * Called when JIV's runtime features are no longer available, after a user quits or logs out of a world.
	 * @since 11.5.0
	 */
	default void onRuntimeUnavailable() {

	}

}
