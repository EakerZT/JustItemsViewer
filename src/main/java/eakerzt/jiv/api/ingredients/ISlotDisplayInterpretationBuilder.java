package eakerzt.jiv.api.ingredients;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.UnaryOperator;

/**
 * Builds JIV's interpretation of a slot display.
 * <p>
 * An interpretation can describe how a display delegates to other slot displays and add meaning that is lost when
 * Minecraft resolves the display into ordinary ingredients. JIV passes a new builder to each slot display
 * interpreter. If the interpreter does not change the builder, JIV handles the display as one ordinary ingredient
 * group.
 *
 * @param <T> the type of ingredient being interpreted
 *
 * @since 29.25.0
 */
@ApiStatus.NonExtendable
public interface ISlotDisplayInterpretationBuilder<T> {

	/**
	 * Append a display to the displays that make up this display.
	 * <p>
	 * JIV resolves and interprets the child as its own ingredient group. Displays that were previously added to this
	 * builder are retained.
	 *
	 * @param childDisplay the display to append
	 * @return this builder, for chaining calls
	 *
	 * @since 29.30.0
	 */
	ISlotDisplayInterpretationBuilder<T> addChildDisplay(SlotDisplay childDisplay);

	/**
	 * Append a display to the displays that make up this display, and transform each ingredient produced by that child.
	 * <p>
	 * JIV resolves and interprets the child first, then applies the transformer to each of its ingredients. This allows
	 * transformed and untransformed children to be combined while preserving their separate ingredient groups and
	 * interpretation details. Nested transformations are applied from the inside out.
	 * <p>
	 * The transformer must not mutate the ingredient it receives. It should return a new ingredient when making a
	 * change. Invalid ingredients returned by the transformer are omitted.
	 *
	 * @param childDisplay the display to append
	 * @param ingredientTransformer transforms each ingredient produced by this child
	 * @return this builder, for chaining calls
	 *
	 * @since 29.30.0
	 */
	ISlotDisplayInterpretationBuilder<T> addChildDisplay(
		SlotDisplay childDisplay,
		UnaryOperator<T> ingredientTransformer
	);

	/**
	 * Set whether each resolved ingredient is a wildcard, representing every subtype with the same
	 * {@link IIngredientHelper#getGroupingUid grouping UID}.
	 * <p>
	 * When enabled, JIV matches all subtypes for input slots whose resolved ingredients can have subtypes according to
	 * their ingredient helper. JIV also adds its standard "Any &lt;ingredient&gt;" tooltip heading unless this display has
	 * a tag or an explicitly set or cleared tooltip heading.
	 * <p>
	 * The default is false.
	 * Calling this method with false suppresses wildcard behavior of wrapped or child displays.
	 *
	 * @param wildcardForSubtypes true if resolved ingredients represent every subtype, false to disable wildcard behavior
	 * @return this builder, for chaining calls
	 *
	 * @since 29.26.0
	 */
	ISlotDisplayInterpretationBuilder<T> setWildcardForSubtypes(boolean wildcardForSubtypes);

	/**
	 * Set the tag represented by the slot display so JIV can show it in recipe slot tooltips.
	 * By default, no tag is recorded.
	 *
	 * @param tagKey the tag represented by the display
	 * @return this builder, for chaining calls
	 *
	 * @since 29.25.0
	 */
	ISlotDisplayInterpretationBuilder<T> setTagKey(TagKey<?> tagKey);

	/**
	 * Prevent JIV from showing a tag for this display.
	 * Use this to suppress a tag inherited from a wrapped or child display, or one that JIV would otherwise infer from
	 * the resolved ingredients.
	 *
	 * @return this builder, for chaining calls
	 *
	 * @since 29.25.0
	 */
	ISlotDisplayInterpretationBuilder<T> clearTagKey();

	/**
	 * Set a heading that JIV shows above the normal tooltip for the currently displayed ingredient.
	 * Use this to explain a slot that displays concrete ingredients but has broader meaning, such as "Any Fuel".
	 * JIV preserves the component's styling.
	 * By default, no heading is added.
	 *
	 * @param tooltipHeader the heading to add
	 * @return this builder, for chaining calls
	 *
	 * @since 29.25.0
	 */
	ISlotDisplayInterpretationBuilder<T> setTooltipHeader(Component tooltipHeader);

	/**
	 * Prevent a tooltip heading inherited from a wrapped or child display from being shown for this display.
	 * <p>
	 * This has no effect unless this display declares a wrapped display or child displays.
	 *
	 * @return this builder, for chaining calls
	 *
	 * @since 29.25.0
	 */
	ISlotDisplayInterpretationBuilder<T> clearTooltipHeader();
}
