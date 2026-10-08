package eakerzt.jiv.gui.bookmarks;

import static org.junit.jupiter.api.Assertions.*;

import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.IFocus;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.runtime.IRecipesGui;
import eakerzt.jiv.common.ingredients.TypedIngredient;
import eakerzt.jiv.gui.overlay.elements.IElement;
import eakerzt.jiv.gui.util.FocusUtil;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;

class BookmarkCellTest {
	@Test
	void onlyInputsWithAlternativesAreReplaceable() {
		var oak = TypedIngredient.createUnvalidated(TYPE, "oak");
		var spruce = TypedIngredient.createUnvalidated(TYPE, "spruce");
		var candidates = List.<ITypedIngredient<?>>of(oak, spruce);
		var input =
				new BookmarkCell<>(
								oak,
								null,
								0,
								1,
								RecipeIngredientRole.INPUT,
								2,
								1,
								false,
								1,
								false,
								false)
						.withCandidates(candidates);
		assertTrue(input.hasCandidates());
		assertEquals(candidates, input.candidates());
		var output =
				new BookmarkCell<>(
								oak,
								null,
								0,
								0,
								RecipeIngredientRole.OUTPUT,
								4,
								1,
								false,
								1,
								true,
								false)
						.withCandidates(candidates);
		assertFalse(output.hasCandidates());
		var fixed =
				new BookmarkCell<>(
								oak,
								null,
								0,
								1,
								RecipeIngredientRole.INPUT,
								2,
								1,
								false,
								1,
								false,
								false)
						.withCandidates(List.of(oak));
		assertFalse(fixed.hasCandidates());
	}

	private static final IIngredientType<String> TYPE = () -> String.class;

	private static class RecordingFocus extends FocusUtil {
		List<RecipeIngredientRole> roles;

		RecordingFocus() {
			super(null, null, null);
		}

		@Override
		public List<IFocus<?>> createFocuses(
				ITypedIngredient<?> ingredient, List<RecipeIngredientRole> roles) {
			this.roles = roles;
			return List.of();
		}
	}

	private record TestBookmark(IElement<?> element) implements IBookmark {
		public BookmarkType getType() {
			return BookmarkType.RECIPE;
		}

		public IElement<?> getElement() {
			return element;
		}

		public boolean isVisible() {
			return true;
		}

		public void setVisible(boolean visible) {}
	}

	@Test
	void outputUsesQueryDoesNotOpenBookmarkedRecipe() {
		int[] recipeCalls = {0}, usesCalls = {0};
		IElement<?> recipeElement =
				(IElement<?>)
						Proxy.newProxyInstance(
								IElement.class.getClassLoader(),
								new Class<?>[] {IElement.class},
								(proxy, method, args) -> {
									if (method.getName().equals("show")) recipeCalls[0]++;
									return null;
								});
		IRecipesGui gui =
				(IRecipesGui)
						Proxy.newProxyInstance(
								IRecipesGui.class.getClassLoader(),
								new Class<?>[] {IRecipesGui.class},
								(proxy, method, args) -> {
									if (method.getName().equals("show")) usesCalls[0]++;
									return null;
								});
		var cell =
				new BookmarkCell<>(
						TypedIngredient.createUnvalidated(TYPE, "stick"),
						new TestBookmark(recipeElement),
						0,
						0,
						RecipeIngredientRole.OUTPUT,
						4,
						1,
						false,
						1,
						true,
						false);
		var focus = new RecordingFocus();
		var roles = List.of(RecipeIngredientRole.INPUT, RecipeIngredientRole.CRAFTING_STATION);
		cell.show(gui, focus, roles);
		assertEquals(roles, focus.roles);
		assertEquals(1, usesCalls[0]);
		assertEquals(0, recipeCalls[0]);
		cell.show(gui, focus, List.of(RecipeIngredientRole.OUTPUT));
		assertEquals(1, recipeCalls[0]);
		assertEquals(1, usesCalls[0]);
	}
}
