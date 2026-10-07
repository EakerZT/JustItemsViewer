package eakerzt.jiv.library.plugins.jiv.tags;

import eakerzt.jiv.api.ingredients.ITypedIngredient;
import net.minecraft.tags.TagKey;

import java.util.List;

public interface ITagInfoRecipe {
	TagKey<?> getTag();

	List<ITypedIngredient<?>> getTypedIngredients();
}
