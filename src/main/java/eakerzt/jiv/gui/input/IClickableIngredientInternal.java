package eakerzt.jiv.gui.input;

import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.api.runtime.IRecipesGui;
import eakerzt.jiv.gui.overlay.elements.IElement;
import eakerzt.jiv.gui.util.FocusUtil;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IClickableIngredientInternal<T> {
	ITypedIngredient<T> getTypedIngredient();

	IElement<T> getElement();

	boolean isMouseOver(double mouseX, double mouseY);

	/**
	 * Returns an ItemStack if this clickable slot allows players to cheat ingredients from it
	 * (when the server has granted them permission to cheat).
	 *
	 * Returns an empty ItemStack if cheating is not allowed.
	 *
	 * This is generally only active in the JIV ingredient list and bookmark list.
	 */
	ItemStack getCheatItemStack(IIngredientManager ingredientManager);

	/**
	 * Most GUIs shouldn't allow JIV to click to set the focus,
	 * because it would conflict with their normal behavior.
	 *
	 * JIV's recipe GUI has clickable slots that do allow click to focus,
	 * in order to let players navigate recipes.
	 */
	boolean canClickToFocus();

	/**
	 * Open recipes or usages for this ingredient.
	 */
	default void show(IRecipesGui recipesGui, FocusUtil focusUtil, List<RecipeIngredientRole> roles) {
		getElement().show(recipesGui, focusUtil, roles);
	}
}
