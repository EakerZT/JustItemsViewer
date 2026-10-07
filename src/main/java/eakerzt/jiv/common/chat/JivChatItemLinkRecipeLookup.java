package eakerzt.jiv.common.chat;

import eakerzt.jiv.api.helpers.IJivHelpers;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.IFocus;
import eakerzt.jiv.api.recipe.IFocusFactory;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.runtime.IJivRuntime;
import eakerzt.jiv.api.runtime.IRecipesGui;
import eakerzt.jiv.common.Internal;

import java.util.Optional;

public final class JivChatItemLinkRecipeLookup {
	private JivChatItemLinkRecipeLookup() {
	}

	public static int executeShowRecipeCommand(String linkText) {
		Optional<JivChatItemLinks.IngredientLink> optionalLink = JivChatItemLinks.parseCommandArgument(linkText);
		if (optionalLink.isEmpty()) {
			return 0;
		}

		Optional<IJivRuntime> optionalRuntime = Internal.getOptionalJivRuntime();
		if (optionalRuntime.isEmpty()) {
			return 0;
		}

		JivChatItemLinks.IngredientLink link = optionalLink.get();
		IJivRuntime runtime = optionalRuntime.get();
		boolean shown = showRecipeForIngredient(runtime, link);
		if (shown) {
			return 1;
		}
		return 0;
	}

	public static boolean showRecipeForIngredient(IJivRuntime runtime, JivChatItemLinks.IngredientLink link) {
		Optional<ITypedIngredient<?>> optionalIngredient = JivChatItemLinks.resolveTypedIngredient(link, runtime.getIngredientManager());
		if (optionalIngredient.isEmpty()) {
			return false;
		}

		ITypedIngredient<?> typedIngredient = optionalIngredient.get();
		IRecipesGui recipesGui = runtime.getRecipesGui();
		IJivHelpers jivHelpers = runtime.getJivHelpers();
		IFocusFactory focusFactory = jivHelpers.getFocusFactory();
		IFocus<?> focus = createFocus(focusFactory, typedIngredient);

		recipesGui.show(focus);
		return true;
	}

	private static <T> IFocus<T> createFocus(IFocusFactory focusFactory, ITypedIngredient<T> typedIngredient) {
		return focusFactory.createFocus(RecipeIngredientRole.OUTPUT, typedIngredient);
	}
}
