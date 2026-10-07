package eakerzt.jiv.api.constants;

import eakerzt.jiv.api.recipe.types.IRecipeHolderType;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import eakerzt.jiv.api.recipe.category.extensions.vanilla.brewing.IBrewingCategoryExtension;
import eakerzt.jiv.api.recipe.category.extensions.vanilla.brewing.IExtendableBrewingRecipeCategory;
import eakerzt.jiv.api.recipe.vanilla.IJivAnvilRecipe;
import eakerzt.jiv.api.recipe.vanilla.IJivBrewingRecipe;
import eakerzt.jiv.api.recipe.vanilla.IJivCompostingRecipe;
import eakerzt.jiv.api.recipe.vanilla.IJivFuelingRecipe;
import eakerzt.jiv.api.recipe.vanilla.IJivGrindstoneRecipe;
import eakerzt.jiv.api.recipe.vanilla.IJivIngredientInfoRecipe;
import eakerzt.jiv.api.recipe.vanilla.IVanillaRecipeFactory;
import eakerzt.jiv.api.registration.IRecipeRegistration;
import eakerzt.jiv.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.block.ComposterBlock;

/**
 * List of all the built-in {@link IRecipeType}s that are added by JIV.
 *
 * @since 9.5.0
 */
public final class RecipeTypes {
	/**
	 * The crafting recipe type.
	 *
	 * Automatically includes all recipes in the {@link net.minecraft.world.item.crafting.RecipeManager}.
	 *
	 * @since 9.5.0
	 */
	public static final IRecipeHolderType<CraftingRecipe> CRAFTING = IRecipeHolderType.create(RecipeType.CRAFTING);

	/**
	 * The stonecutting recipe type.
	 *
	 * Automatically includes every {@link StonecutterRecipe}.
	 *
	 * @since 9.5.0
	 */
	public static final IRecipeHolderType<StonecutterRecipe> STONECUTTING = IRecipeHolderType.create(RecipeType.STONECUTTING);

	/**
	 * The smelting recipe type.
	 *
	 * Automatically includes every {@link SmeltingRecipe}.
	 *
	 * @since 9.5.0
	 */
	public static final IRecipeHolderType<SmeltingRecipe> SMELTING = IRecipeHolderType.create(RecipeType.SMELTING);

	/**
	 * The smoking recipe type.
	 *
	 * Automatically includes every {@link SmokingRecipe}.
	 *
	 * @since 9.5.0
	 */
	public static final IRecipeHolderType<SmokingRecipe> SMOKING = IRecipeHolderType.create(RecipeType.SMOKING);

	/**
	 * The blasting recipe type.
	 *
	 * Automatically includes every {@link BlastingRecipe}.
	 *
	 * @since 9.5.0
	 */
	public static final IRecipeHolderType<BlastingRecipe> BLASTING = IRecipeHolderType.create(RecipeType.BLASTING);

	/**
	 * The campfire cooking recipe type.
	 *
	 * Automatically includes every {@link CampfireCookingRecipe}.
	 *
	 * @since 9.5.0
	 */
	public static final IRecipeHolderType<CampfireCookingRecipe> CAMPFIRE_COOKING = IRecipeHolderType.create(RecipeType.CAMPFIRE_COOKING);

	/**
	 * The furnace fuel recipe type.
	 *
	 * JIV automatically creates a fuel recipe for anything that has a burn time.
	 *
	 * @since 20.0.0
	 */
	public static final IRecipeType<IJivFuelingRecipe> SMELTING_FUEL = IRecipeType.create(ModIds.MINECRAFT_ID, "smelting_fuel", IJivFuelingRecipe.class);

	/**
	 * The blast furnace fuel recipe type.
	 *
	 * JIV automatically creates a fuel recipe for anything that has a burn time.
	 *
	 * @since 20.0.0
	 */
	public static final IRecipeType<IJivFuelingRecipe> BLASTING_FUEL = IRecipeType.create(ModIds.MINECRAFT_ID, "blasting_fuel", IJivFuelingRecipe.class);

	/**
	 * The smoker fuel recipe type.
	 *
	 * JIV automatically creates a fuel recipe for anything that has a burn time.
	 *
	 * @since 20.0.0
	 */
	public static final IRecipeType<IJivFuelingRecipe> SMOKING_FUEL = IRecipeType.create(ModIds.MINECRAFT_ID, "smoking_fuel", IJivFuelingRecipe.class);

	/**
	 * The brewing recipe type.
	 *
	 * JIV automatically tries to generate all potion variations from the basic ingredients
	 * and adds platform brewing recipes that expose their inputs and outputs.
	 *
	 * @see IVanillaRecipeFactory#createBrewingRecipe to create new brewing recipes in JIV.
	 * @see IVanillaCategoryExtensionRegistration#getBrewingCategory()
	 * @see IExtendableBrewingRecipeCategory#addExtension(Class, IBrewingCategoryExtension)
	 *
	 * @since 9.5.0
	 */
	public static final IRecipeType<IJivBrewingRecipe> BREWING = IRecipeType.create(ModIds.MINECRAFT_ID, "brewing", IJivBrewingRecipe.class);

	/**
	 * The anvil recipe type.
	 *
	 * @see IVanillaRecipeFactory#createAnvilRecipe to create new anvil recipes in JIV.
	 *
	 * @since 9.5.0
	 */
	public static final IRecipeType<IJivAnvilRecipe> ANVIL = IRecipeType.create(ModIds.MINECRAFT_ID, "anvil", IJivAnvilRecipe.class);

	/**
	 * The grindstone recipe type.
	 *
	 * @see IVanillaRecipeFactory#createGrindstoneRecipe to create new grindstone recipes in JIV.
	 *
	 * @since 23.1.0
	 */
	public static final IRecipeType<IJivGrindstoneRecipe> GRINDSTONE = IRecipeType.create(ModIds.MINECRAFT_ID, "grindstone", IJivGrindstoneRecipe.class);

	/**
	 * The smithing recipe type.
	 * Automatically includes every
	 * {@link net.minecraft.world.item.crafting.SmithingTrimRecipe}
	 * {@link net.minecraft.world.item.crafting.SmithingTransformRecipe}
	 *
	 * @since 9.5.0
	 */
	public static final IRecipeHolderType<SmithingRecipe> SMITHING = IRecipeHolderType.create(RecipeType.SMITHING);

	/**
	 * The composting recipe type.
	 * Automatically includes every item added to {@link ComposterBlock#COMPOSTABLES}.
	 *
	 * @since 9.5.0
	 */
	public static final IRecipeType<IJivCompostingRecipe> COMPOSTING = IRecipeType.create(ModIds.MINECRAFT_ID, "compostable", IJivCompostingRecipe.class);

	/**
	 * The JIV info recipe type.
	 *
	 * @see IRecipeRegistration#addIngredientInfo to create this type of recipe.
	 *
	 * @since 9.5.0
	 */
	public static final IRecipeType<IJivIngredientInfoRecipe> INFORMATION = IRecipeType.create(ModIds.JIV_ID, "information", IJivIngredientInfoRecipe.class);

	private RecipeTypes() {}
}
