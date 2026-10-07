package eakerzt.jiv.gui.util;

import eakerzt.jiv.api.ingredients.IIngredientTypeWithSubtypes;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.IFocus;
import eakerzt.jiv.api.recipe.IFocusFactory;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.config.IClientConfig;
import eakerzt.jiv.common.platform.IPlatformFluidHelperInternal;
import eakerzt.jiv.common.platform.Services;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FocusUtil {
	private final IFocusFactory focusFactory;
	private final IClientConfig clientConfig;
	private final IIngredientManager ingredientManager;

	public FocusUtil(IFocusFactory focusFactory, IClientConfig clientConfig, IIngredientManager ingredientManager) {
		this.focusFactory = focusFactory;
		this.clientConfig = clientConfig;
		this.ingredientManager = ingredientManager;
	}

	public List<IFocus<?>> createFocuses(ITypedIngredient<?> ingredient, List<RecipeIngredientRole> roles) {
		List<ITypedIngredient<?>> ingredients = new ArrayList<>();
		ingredients.add(ingredient);

		if (clientConfig.lookupFluidContentsEnabled().get()) {
			IPlatformFluidHelperInternal<?> fluidHelper = Services.PLATFORM.getFluidHelper();
			getContainedFluid(fluidHelper, ingredient)
				.ifPresent(ingredients::add);
		}

		return roles.stream()
			.<IFocus<?>>flatMap(role -> {
				return ingredients.stream()
					.map(i -> focusFactory.createFocus(role, i));
			})
			.toList();
	}

	private <T> Optional<ITypedIngredient<T>> getContainedFluid(IPlatformFluidHelperInternal<T> fluidHelper, ITypedIngredient<?> ingredient) {
		return fluidHelper.getContainedFluid(ingredient)
			.flatMap(fluid -> {
				IIngredientTypeWithSubtypes<Fluid, T> type = fluidHelper.getFluidIngredientType();
				return ingredientManager.createTypedIngredient(type, fluid, false);
			});
	}
}
