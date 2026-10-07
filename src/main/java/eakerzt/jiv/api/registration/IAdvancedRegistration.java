package eakerzt.jiv.api.registration;

import eakerzt.jiv.api.IModPlugin;
import eakerzt.jiv.api.gui.IRecipeLayoutDrawable;
import eakerzt.jiv.api.gui.buttons.IIconButtonController;
import eakerzt.jiv.api.recipe.advanced.IRecipeButtonControllerFactory;
import eakerzt.jiv.api.helpers.IJivHelpers;
import eakerzt.jiv.api.recipe.advanced.IRecipeManagerPlugin;
import eakerzt.jiv.api.recipe.advanced.IRecipeManagerPluginHelper;
import eakerzt.jiv.api.recipe.advanced.ISimpleRecipeManagerPlugin;
import eakerzt.jiv.api.recipe.category.extensions.IRecipeCategoryDecorator;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import org.jetbrains.annotations.ApiStatus;

/**
 * The IAdvancedRegistration instance is passed to your mod plugin in {@link IModPlugin#registerAdvanced(IAdvancedRegistration)}.
 */
@ApiStatus.NonExtendable
public interface IAdvancedRegistration {
	/**
	 * {@link IJivHelpers} provides helpers and tools for addon mods.
	 */
	IJivHelpers getJivHelpers();

	/**
	 * Helpers for implementing {@link IRecipeManagerPlugin}s.
	 *
	 * @since 19.15.1
	 */
	IRecipeManagerPluginHelper getRecipeManagerPluginHelper();

	/**
	 * Register your own {@link IRecipeManagerPlugin} here.
	 */
	void addRecipeManagerPlugin(IRecipeManagerPlugin recipeManagerPlugin);

	/**
	 * Register your own {@link ISimpleRecipeManagerPlugin} here.
	 *
	 * @since 20.0.0
	 */
	<T> void addSimpleRecipeManagerPlugin(IRecipeType<T> recipeType, ISimpleRecipeManagerPlugin<T> recipeManagerPlugin);

	/**
	 * Register a {@link IRecipeCategoryDecorator} for a recipe type.
	 *
	 * @since 15.1.0
	 */
	<T> void addRecipeCategoryDecorator(IRecipeType<T> recipeType, IRecipeCategoryDecorator<T> decorator);

	/**
	 * Register a {@link IRecipeButtonControllerFactory} to add custom buttons
	 * to recipe layouts.
	 *
	 * <p>
	 * The factory is used to create {@link IIconButtonController} instances
	 * for individual {@link IRecipeLayoutDrawable} objects as they are created.
	 * </p>
	 *
	 * @since 27.2.0
	 */
	void addRecipeButtonFactory(IRecipeButtonControllerFactory recipeButtonControllerFactory);

}
