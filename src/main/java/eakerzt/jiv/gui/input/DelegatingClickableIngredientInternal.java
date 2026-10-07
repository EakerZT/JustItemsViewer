package eakerzt.jiv.gui.input;

import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.api.runtime.IRecipesGui;
import eakerzt.jiv.gui.overlay.elements.IElement;
import eakerzt.jiv.gui.util.FocusUtil;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class DelegatingClickableIngredientInternal<T> implements IClickableIngredientInternal<T> {
	private final IClickableIngredientInternal<T> delegate;

	public DelegatingClickableIngredientInternal(IClickableIngredientInternal<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public ITypedIngredient<T> getTypedIngredient() {
		return delegate.getTypedIngredient();
	}

	@Override
	public IElement<T> getElement() {
		return delegate.getElement();
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return delegate.isMouseOver(mouseX, mouseY);
	}

	@Override
	public ItemStack getCheatItemStack(IIngredientManager ingredientManager) {
		return delegate.getCheatItemStack(ingredientManager);
	}

	@Override
	public boolean canClickToFocus() {
		return delegate.canClickToFocus();
	}

	@Override
	public void show(IRecipesGui recipesGui, FocusUtil focusUtil, List<RecipeIngredientRole> roles) {
		delegate.show(recipesGui, focusUtil, roles);
	}
}
