package eakerzt.jiv.test;

import eakerzt.jiv.api.ingredients.IIngredientHelper;
import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.common.ingredients.TypedIngredient;
import eakerzt.jiv.library.focus.Focus;
import eakerzt.jiv.library.focus.FocusGroup;
import eakerzt.jiv.library.gui.recipes.supplier.builder.IngredientSupplierBuilder;
import eakerzt.jiv.library.ingredients.IIngredientManagerInternal;
import eakerzt.jiv.library.ingredients.RecipeIngredientSupplier;
import eakerzt.jiv.library.ingredients.RecipeIngredientSupplier.FocusLink;
import eakerzt.jiv.library.ingredients.SlotIngredient;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import org.junit.jupiter.api.Test;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RecipeFocusLinkTest {
	private static final IIngredientType<String> INGREDIENT_TYPE = () -> String.class;

	@Test
	void keepsOnlyIndexesVisibleInEveryLinkedSlot() {
		FocusLink focusLink = createFocusLink(
			createIngredients("hidden", "first", "second"),
			createIngredients("a", "b", "hidden")
		);

		Set<Integer> result = focusLink.getVisibleIngredientIndexes(
			FocusGroup.EMPTY,
			createUnusedIngredientManager(),
			RecipeFocusLinkTest::isVisible
		);

		assertEquals(Set.of(1), result);
	}

	@Test
	void appliesVisibilityToFocusedIndexes() {
		FocusLink focusLink = createFocusLink(
			createIngredients("focused", "focused", "not focused"),
			createIngredients("hidden", "visible", "other")
		);
		Focus<String> focus = new Focus<>(RecipeIngredientRole.INPUT, createTypedIngredient("focused"));

		Set<Integer> result = focusLink.getVisibleIngredientIndexes(
			focus,
			createIngredientManagerForFocusMatching(),
			RecipeFocusLinkTest::isVisible
		);

		assertEquals(Set.of(1), result);
	}

	@Test
	void hidesRecipeWhenEveryLinkedPairContainsAHiddenIngredient() {
		FocusLink focusLink = createFocusLink(
			createIngredients("hidden", "visible"),
			createIngredients("visible", "hidden")
		);

		var result = focusLink.getVisibleIngredientIndexes(
			FocusGroup.EMPTY,
			createUnusedIngredientManager(),
			RecipeFocusLinkTest::isVisible
		);

		assertNull(result);
	}

	@Test
	void blankIngredientsRemainLinked() {
		List<@Nullable SlotIngredient<?>> firstSlot = Arrays.asList(null, createIngredient("hidden"));
		FocusLink focusLink = createFocusLink(
			firstSlot,
			createIngredients("visible", "visible")
		);

		Set<Integer> result = focusLink.getVisibleIngredientIndexes(
			FocusGroup.EMPTY,
			createUnusedIngredientManager(),
			RecipeFocusLinkTest::isVisible
		);

		assertEquals(Set.of(0), result);
	}

	@Test
	void leavesUnfocusedVisibleSlotsUnrestricted() {
		FocusLink focusLink = createFocusLink(
			createIngredients("first", "second"),
			createIngredients("a", "b")
		);

		Set<Integer> result = focusLink.getVisibleIngredientIndexes(
			FocusGroup.EMPTY,
			createUnusedIngredientManager(),
			RecipeFocusLinkTest::isVisible
		);

		assertEquals(Set.of(), result);
	}

	@Test
	void ingredientSupplierRecordsCollectionFocusLinksForRecipeVisibility() {
		IIngredientManagerInternal ingredientManager = createUnusedIngredientManager();
		ContextMap contextMap = new ContextMap.Builder()
			.create(new ContextKeySet.Builder().build());
		IngredientSupplierBuilder builder = new IngredientSupplierBuilder(ingredientManager, contextMap);
		var firstSlot = builder.addSlot(RecipeIngredientRole.INPUT)
			.add(createTypedIngredient("hidden"))
			.add(createTypedIngredient("visible"));
		var secondSlot = builder.addSlot(RecipeIngredientRole.OUTPUT)
			.add(createTypedIngredient("visible"))
			.add(createTypedIngredient("hidden"));
		builder.createFocusLink(List.of(firstSlot, secondSlot));
		RecipeIngredientSupplier ingredientSupplier = builder.buildIngredientSupplier();

		var visibleIndexes = ingredientSupplier.getFocusLinks()
			.getFirst()
			.getVisibleIngredientIndexes(FocusGroup.EMPTY, ingredientManager, RecipeFocusLinkTest::isVisible);

		assertNull(visibleIndexes);
	}

	private static FocusLink createFocusLink(
		List<@Nullable SlotIngredient<?>> inputIngredients,
		List<@Nullable SlotIngredient<?>> outputIngredients
	) {
		return new FocusLink(List.of(
			new FocusLink.Slot(RecipeIngredientRole.INPUT, inputIngredients),
			new FocusLink.Slot(RecipeIngredientRole.OUTPUT, outputIngredients)
		));
	}

	private static List<@Nullable SlotIngredient<?>> createIngredients(String... ingredients) {
		return Arrays.stream(ingredients)
			.<@Nullable SlotIngredient<?>>map(RecipeFocusLinkTest::createIngredient)
			.toList();
	}

	private static SlotIngredient<String> createIngredient(String ingredient) {
		return new SlotIngredient<>(createTypedIngredient(ingredient));
	}

	private static ITypedIngredient<String> createTypedIngredient(String ingredient) {
		return TypedIngredient.createUnvalidated(INGREDIENT_TYPE, ingredient);
	}

	private static boolean isVisible(ITypedIngredient<?> ingredient) {
		return !ingredient.getIngredient().equals("hidden");
	}

	private static IIngredientManagerInternal createUnusedIngredientManager() {
		return (IIngredientManagerInternal) Proxy.newProxyInstance(
			IIngredientManagerInternal.class.getClassLoader(),
			new Class<?>[]{IIngredientManagerInternal.class},
			(proxy, method, args) -> {
				throw new AssertionError("Ingredient manager method should not be called: " + method.getName());
			}
		);
	}

	private static IIngredientManagerInternal createIngredientManagerForFocusMatching() {
		IIngredientHelper<?> ingredientHelper = (IIngredientHelper<?>) Proxy.newProxyInstance(
			IIngredientHelper.class.getClassLoader(),
			new Class<?>[]{IIngredientHelper.class},
			(proxy, method, args) -> {
				if (method.getName().equals("getUid")) {
					Object ingredient = args[0];
					if (ingredient instanceof ITypedIngredient<?> typedIngredient) {
						return typedIngredient.getIngredient();
					}
					return ingredient;
				}
				throw new AssertionError("Ingredient helper method should not be called: " + method.getName());
			}
		);
		return (IIngredientManagerInternal) Proxy.newProxyInstance(
			IIngredientManagerInternal.class.getClassLoader(),
			new Class<?>[]{IIngredientManagerInternal.class},
			(proxy, method, args) -> {
				if (method.getName().equals("getIngredientHelper")) {
					return ingredientHelper;
				}
				throw new AssertionError("Ingredient manager method should not be called: " + method.getName());
			}
		);
	}
}
