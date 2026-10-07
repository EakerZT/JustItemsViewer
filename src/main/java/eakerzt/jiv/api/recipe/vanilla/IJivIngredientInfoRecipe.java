package eakerzt.jiv.api.recipe.vanilla;

import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.FormattedText;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * A recipe in JIV that displays a page of text information about an ingredient.
 *
 * Create your own with {@link IRecipeRegistration#addIngredientInfo}.
 *
 * @since 9.5.0
 */
@ApiStatus.NonExtendable
public interface IJivIngredientInfoRecipe {
	/**
	 * The input ingredients for the recipe.
	 *
	 * @since 9.5.0
	 */
	@Unmodifiable
	List<ITypedIngredient<?>> getIngredients();

	/**
	 * A short description of the ingredients, broken up across multiple lines of text.
	 *
	 * @since 9.5.0
	 */
	@Unmodifiable
	List<FormattedText> getDescription();
}
