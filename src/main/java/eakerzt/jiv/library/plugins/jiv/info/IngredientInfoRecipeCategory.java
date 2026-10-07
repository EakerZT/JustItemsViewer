package eakerzt.jiv.library.plugins.jiv.info;

import eakerzt.jiv.api.constants.RecipeTypes;
import eakerzt.jiv.api.gui.builder.IIngredientAcceptor;
import eakerzt.jiv.api.gui.builder.IRecipeLayoutBuilder;
import eakerzt.jiv.api.gui.builder.IRecipeSlotBuilder;
import eakerzt.jiv.api.gui.widgets.IRecipeExtrasBuilder;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.IFocusGroup;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.recipe.category.AbstractRecipeCategory;
import eakerzt.jiv.api.recipe.vanilla.IJivIngredientInfoRecipe;
import eakerzt.jiv.common.gui.textures.Textures;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class IngredientInfoRecipeCategory extends AbstractRecipeCategory<IJivIngredientInfoRecipe> {
	private static final int recipeWidth = 170;
	private static final int recipeHeight = 125;

	public IngredientInfoRecipeCategory(Textures textures) {
		super(
			RecipeTypes.INFORMATION,
			Component.translatable("gui.jiv.category.itemInformation"),
			textures.getInfoIcon(),
			recipeWidth,
			recipeHeight
		);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, IJivIngredientInfoRecipe recipe, IFocusGroup focuses) {
		int xPos = (recipeWidth - 16) / 2;

		IRecipeSlotBuilder inputSlotBuilder = builder.addInputSlot(xPos, 1)
			.setStandardSlotBackground();

		IIngredientAcceptor<?> outputSlotBuilder = builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT);

		for (ITypedIngredient<?> typedIngredient : recipe.getIngredients()) {
			inputSlotBuilder.add(typedIngredient);
			outputSlotBuilder.add(typedIngredient);
		}
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, IJivIngredientInfoRecipe recipe, IFocusGroup focuses) {
		int yPos = 22;
		int height = recipeHeight - yPos;
		builder.addScrollBoxWidget(recipeWidth, height, 0, yPos)
			.setContents(recipe.getDescription());
	}

	@Override
	public @Nullable Identifier getIdentifier(IJivIngredientInfoRecipe recipe) {
		return null;
	}

}
