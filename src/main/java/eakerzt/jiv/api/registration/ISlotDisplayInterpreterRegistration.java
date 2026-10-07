package eakerzt.jiv.api.registration;

import eakerzt.jiv.api.IModPlugin;
import eakerzt.jiv.api.constants.VanillaTypes;
import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.ingredients.ISlotDisplayInterpreter;
import eakerzt.jiv.api.ingredients.IUniversalSlotDisplayInterpreter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jetbrains.annotations.ApiStatus;

/**
 * Register slot display interpreters so JIV can correctly match and describe resolved ingredients.
 * <p>
 * Use this for slot displays that lose important information when Minecraft resolves it into ordinary
 * ingredients. An interpreter can tell JIV that one resolved value stands for all subtypes, preserve a tag name
 * for tooltips, or add a heading that explains the display's broader meaning.
 * <p>
 * This is given to plugins in {@link IModPlugin#registerSlotDisplayInterpreters}.
 *
 * @since 29.25.0
 */
@ApiStatus.NonExtendable
public interface ISlotDisplayInterpreterRegistration {
	/**
	 * Register an interpreter that applies to a slot display for every ingredient type.
	 * <p>
	 * Use this for displays such as composites that always delegate to the same child displays, regardless of the
	 * ingredient type being resolved. JIV applies this interpreter before one registered for a specific ingredient
	 * type.
	 * <p>
	 * Use an ingredient-specific {@link #register} overload instead when the interpretation depends on a resolved
	 * ingredient type.
	 *
	 * @param slotDisplayType the type of slot display to interpret
	 * @param interpreter describes the display independently of a resolved ingredient type
	 *
	 * @since 29.25.0
	 */
	<D extends SlotDisplay> void registerUniversal(
		SlotDisplay.Type<D> slotDisplayType,
		IUniversalSlotDisplayInterpreter<D> interpreter
	);

	/**
	 * Register an interpreter for a slot display that resolves into item stacks.
	 * The interpreter can get all item stacks produced by the display from its context.
	 *
	 * @param slotDisplayType the type of slot display to interpret
	 * @param interpreter describes how JIV should match and present the resolved item stacks
	 *
	 * @since 29.25.0
	 */
	default <D extends SlotDisplay> void register(
		SlotDisplay.Type<D> slotDisplayType,
		ISlotDisplayInterpreter<D, ItemStack> interpreter
	) {
		register(slotDisplayType, VanillaTypes.ITEM_STACK, interpreter);
	}

	/**
	 * Register an interpreter for one slot display type and ingredient type.
	 * The interpreter can get all ingredients of this type produced by the display from its context.
	 *
	 * @param slotDisplayType the type of slot display to interpret
	 * @param ingredientType the type of ingredient produced by the display
	 * @param interpreter describes how JIV should match and present the resolved ingredients
	 *
	 * @since 29.25.0
	 */
	<D extends SlotDisplay, T> void register(
		SlotDisplay.Type<D> slotDisplayType,
		IIngredientType<T> ingredientType,
		ISlotDisplayInterpreter<D, T> interpreter
	);
}
