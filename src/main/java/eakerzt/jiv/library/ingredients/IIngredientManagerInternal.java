package eakerzt.jiv.library.ingredients;

import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.ingredients.ITypedIngredientFactory;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.List;
import java.util.stream.Stream;

/**
 * Internal ingredient services that rely on state built alongside the public ingredient manager.
 */
public interface IIngredientManagerInternal extends IIngredientManager, ITypedIngredientFactory {
	Stream<SlotIngredient<?>> resolveSlotDisplay(
		ContextMap contextMap,
		RecipeIngredientRole role,
		SlotDisplay slotDisplay
	);

	<T> Stream<SlotIngredient<T>> resolveSlotDisplay(
		IIngredientType<T> ingredientType,
		ContextMap contextMap,
		RecipeIngredientRole role,
		SlotDisplay slotDisplay
	);

	<T> List<ITypedIngredient<T>> getGroupedIngredients(ITypedIngredient<T> ingredient);
}
