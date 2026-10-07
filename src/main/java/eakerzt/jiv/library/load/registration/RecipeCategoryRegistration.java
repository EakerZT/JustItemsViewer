package eakerzt.jiv.library.load.registration;

import com.google.common.base.Preconditions;
import eakerzt.jiv.api.helpers.IJivHelpers;
import eakerzt.jiv.api.recipe.category.IRecipeCategory;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import eakerzt.jiv.api.registration.IRecipeCategoryRegistration;
import eakerzt.jiv.common.util.ErrorUtil;
import eakerzt.jiv.library.runtime.JivHelpers;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeCategoryRegistration implements IRecipeCategoryRegistration {
	private final List<IRecipeCategory<?>> recipeCategories = new ArrayList<>();
	private final Map<Identifier, IRecipeType<?>> recipeTypes = new HashMap<>();
	private final JivHelpers jivHelpers;

	public RecipeCategoryRegistration(JivHelpers jivHelpers) {
		this.jivHelpers = jivHelpers;
	}

	@Override
	public void addRecipeCategories(IRecipeCategory<?>... recipeCategories) {
		ErrorUtil.checkNotEmpty(recipeCategories, "recipeCategories");

		for (IRecipeCategory<?> recipeCategory : recipeCategories) {
			IRecipeType<?> recipeType = recipeCategory.getRecipeType();
			Preconditions.checkNotNull(recipeType, "Recipe type cannot be null %s", recipeCategory);
			Identifier recipeTypeUid = recipeType.getUid();
			if (recipeTypes.containsKey(recipeTypeUid)) {
				IRecipeType<?> existing = recipeTypes.get(recipeTypeUid);
				throw new IllegalArgumentException("Tried to register a recipe type \"" + recipeType + "\" but there is already one registered with the same UID: " + existing);
			} else {
				recipeTypes.put(recipeTypeUid, recipeType);
			}
			Preconditions.checkArgument(recipeCategory.getWidth() > 0, "Width must be greater than 0");
			Preconditions.checkArgument(recipeCategory.getHeight() > 0, "Height must be greater than 0");
		}

		Collections.addAll(this.recipeCategories, recipeCategories);
		this.jivHelpers.setRecipeCategories(Collections.unmodifiableCollection(this.recipeCategories));
	}

	@Override
	public IJivHelpers getJivHelpers() {
		return jivHelpers;
	}

	@Unmodifiable
	public List<IRecipeCategory<?>> getRecipeCategories() {
		return List.copyOf(recipeCategories);
	}
}
