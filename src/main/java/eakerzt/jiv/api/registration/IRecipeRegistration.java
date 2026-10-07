package eakerzt.jiv.api.registration;

import eakerzt.jiv.api.constants.VanillaTypes;
import eakerzt.jiv.api.helpers.IJivHelpers;
import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import eakerzt.jiv.api.recipe.vanilla.IVanillaRecipeFactory;
import eakerzt.jiv.api.runtime.IIngredientManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.NonExtendable
public interface IRecipeRegistration {
	/**
	 * {@link IJivHelpers} provides helpers and tools for addon mods.
	 */
	IJivHelpers getJivHelpers();

	/**
	 * The {@link IIngredientManager} has some useful functions related to recipe ingredients.
	 */
	IIngredientManager getIngredientManager();

	/**
	 * The {@link IVanillaRecipeFactory} allows creation of vanilla recipes.
	 */
	IVanillaRecipeFactory getVanillaRecipeFactory();

	/**
	 * @return the current context for resolving recipe displays.
	 *
	 * @since 29.12.0
	 */
	ContextMap getContextMap();

	/**
	 * Add the recipes provided by your plugin.
	 *
	 * @since 9.5.0
	 */
	<T> void addRecipes(IRecipeType<T> recipeType, List<T> recipes);

	/**
	 * Add an info page for an ingredient.
	 * Description pages show in the recipes for an ingredient and tell the player a little bit about it.
	 *
	 * @param ingredient            The ingredient to describe
	 * @param ingredientType        The type of the ingredient
	 * @param descriptionComponents Text components for info text.
	 *                              New lines can be added with "\n" or by giving multiple descriptions.
	 *                              Long lines are wrapped automatically.
	 *                              Very long entries will span multiple pages automatically.
	 * @since 7.6.4
	 */
	<T> void addIngredientInfo(T ingredient, IIngredientType<T> ingredientType, Component... descriptionComponents);

	/**
	 * Add an info page for multiple ingredients together.
	 * Description pages show in the recipes for an ingredient and tell the player a little bit about it.
	 *
	 * @param ingredients           The ingredients to describe
	 * @param ingredientType        The type of the ingredients
	 * @param descriptionComponents Text components for info text.
	 *                              New lines can be added with "\n" or by giving multiple descriptions.
	 *                              Long lines are wrapped automatically.
	 *                              Very long entries will span multiple pages automatically.
	 * @since 7.6.4
	 */
	<T> void addIngredientInfo(List<T> ingredients, IIngredientType<T> ingredientType, Component... descriptionComponents);

	/**
	 * Add an info page for an ItemLike.
	 * Description pages show in the recipes for an ItemStack and tell the player a little about it.
	 *
	 * @param itemLike              The ItemLike to describe
	 * @param descriptionComponents Text components for info text.
	 *                              New lines can be added with "\n" or by giving multiple descriptions.
	 *                              Long lines are wrapped automatically.
	 *                              Very long entries will span multiple pages automatically.
	 * @since 19.18.3
	 */
	default void addIngredientInfo(ItemLike itemLike, Component... descriptionComponents) {
		addIngredientInfo(itemLike.asItem().getDefaultInstance(), VanillaTypes.ITEM_STACK, descriptionComponents);
	}

	/**
	 * Add an info page for an ItemStack.
	 * Description pages show in the recipes for an ItemStack and tell the player a little about it.
	 *
	 * @param ingredient            The ItemStack to describe
	 * @param descriptionComponents Text components for info text.
	 *                              New lines can be added with "\n" or by giving multiple descriptions.
	 *                              Long lines are wrapped automatically.
	 *                              Very long entries will span multiple pages automatically.
	 * @since 11.1.1
	 */
	default void addItemStackInfo(ItemStack ingredient, Component... descriptionComponents) {
		addIngredientInfo(ingredient, VanillaTypes.ITEM_STACK, descriptionComponents);
	}

	/**
	 * Add an info page for multiple ItemStacks together.
	 * Description pages show in the recipes for an ItemStack and tell the player a little about it.
	 *
	 * @param ingredients           The ItemStacks to describe
	 * @param descriptionComponents Text components for info text.
	 *                              New lines can be added with "\n" or by giving multiple descriptions.
	 *                              Long lines are wrapped automatically.
	 *                              Very long entries will span multiple pages automatically.
	 * @since 7.6.4
	 */
	default void addItemStackInfo(List<ItemStack> ingredients, Component... descriptionComponents) {
		addIngredientInfo(ingredients, VanillaTypes.ITEM_STACK, descriptionComponents);
	}
}
