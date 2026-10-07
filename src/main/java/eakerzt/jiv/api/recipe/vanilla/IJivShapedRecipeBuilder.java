package eakerzt.jiv.api.recipe.vanilla;

import eakerzt.jiv.api.recipe.advanced.IRecipeManagerPlugin;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jetbrains.annotations.ApiStatus;

/**
 * Builds a serializable ShapedRecipe that isn't registered with the vanilla game.
 * Useful for generating crafting recipes from {@link IRecipeManagerPlugin}.
 *
 * @since 19.15.0
 */
@ApiStatus.NonExtendable
public interface IJivShapedRecipeBuilder {

	/**
	 * Indicate which ingredient should be used for the given character in the recipe pattern.
	 *
	 * @see ShapedRecipeBuilder#define
	 * @since 19.15.0
	 */
	IJivShapedRecipeBuilder define(Character character, Ingredient ingredient);

	/**
	 * Indicate which ingredient should be used for the given character in the recipe pattern.
	 *
	 * @see ShapedRecipeBuilder#define
	 * @since 20.0.0
	 */
	IJivShapedRecipeBuilder define(Character character, Ingredient ingredient, SlotDisplay display);

	/**
	 * Set a row of the pattern for this recipe.
	 *
	 * @see ShapedRecipeBuilder#pattern
	 * @since 19.15.0
	 */
	IJivShapedRecipeBuilder pattern(String patternRow);

	/**
	 * Optionally set the group name of this recipe.
	 *
	 * @see ShapedRecipeBuilder#group(String)
	 * @since 19.15.0
	 */
	IJivShapedRecipeBuilder group(String group);

	/**
	 * Create an unregistered shaped recipe based on the ingredients and pattern.
	 *
	 * @since 19.15.0
	 */
	CraftingRecipe build();
}
