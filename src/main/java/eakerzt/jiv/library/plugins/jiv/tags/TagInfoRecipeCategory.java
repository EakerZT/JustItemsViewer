package eakerzt.jiv.library.plugins.jiv.tags;

import eakerzt.jiv.api.gui.builder.IRecipeLayoutBuilder;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotDrawable;
import eakerzt.jiv.api.gui.ingredient.IRecipeSlotDrawablesView;
import eakerzt.jiv.api.gui.placement.HorizontalAlignment;
import eakerzt.jiv.api.gui.placement.VerticalAlignment;
import eakerzt.jiv.api.gui.widgets.IRecipeExtrasBuilder;
import eakerzt.jiv.api.gui.widgets.IScrollGridWidget;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.recipe.IFocusGroup;
import eakerzt.jiv.api.recipe.RecipeIngredientRole;
import eakerzt.jiv.api.recipe.category.AbstractRecipeCategory;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import eakerzt.jiv.common.gui.JivGuiColors;
import eakerzt.jiv.common.gui.JivGuiColors.GuiColor;
import eakerzt.jiv.common.platform.IPlatformRenderHelper;
import eakerzt.jiv.common.platform.Services;
import eakerzt.jiv.library.util.ResourceLocationUtil;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class TagInfoRecipeCategory<R extends ITagInfoRecipe, T extends IRecipeType<R>> extends AbstractRecipeCategory<R> {
	private static final int WIDTH = 142;
	private static final int HEIGHT = 110;

	public TagInfoRecipeCategory(IGuiHelper guiHelper, T recipeType, Identifier id) {
		super(
			recipeType,
			createTitle(id),
			guiHelper.createDrawableItemLike(Items.NAME_TAG),
			WIDTH,
			HEIGHT
		);
	}

	private static Component createTitle(Identifier id) {
		String registryName = ResourceLocationUtil.sanitizePath(id.getPath());
		String registryNameTranslationKey = "gui.jiv.category.tagInformation." + registryName;

		Language language = Language.getInstance();
		if (language.has(registryNameTranslationKey)) {
			return Component.translatable(registryNameTranslationKey);
		}

		return Component.translatable("gui.jiv.category.tagInformation", StringUtils.capitalize(id.getPath()));
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, R recipe, IFocusGroup focuses) {
		builder.addInvisibleIngredients(RecipeIngredientRole.INPUT)
			.addTypedIngredients(recipe.getTypedIngredients());

		for (ITypedIngredient<?> stack : recipe.getTypedIngredients()) {
			builder.addSlot(RecipeIngredientRole.RENDER_ONLY)
				.add(stack);
		}
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, R recipe, IFocusGroup focuses) {
		TagKey<?> tag = recipe.getTag();

		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		Component tagName = renderHelper.getName(tag);
		List<FormattedText> text = List.of(
			tagName,
			Component.literal(tag.location().toString())
				.withStyle(style -> style.withColor(TextColor.fromRgb(JivGuiColors.getColor(GuiColor.TAG_INFORMATION_IDENTIFIER_TEXT) & 0xFFFFFF)))
		);
		builder.addText(text, getWidth(), 20)
			.setPosition(0, 0)
			.setColor(JivGuiColors.getColor(GuiColor.TAG_INFORMATION_TEXT))
			.setLineSpacing(0)
			.setTextAlignment(VerticalAlignment.CENTER)
			.setTextAlignment(HorizontalAlignment.CENTER);

		IRecipeSlotDrawablesView recipeSlots = builder.getRecipeSlots();
		List<IRecipeSlotDrawable> ingredientSlots = recipeSlots.getSlots(RecipeIngredientRole.RENDER_ONLY);

		IScrollGridWidget scrollGridWidget = builder.addScrollGridWidget(ingredientSlots, 7, 5);
		scrollGridWidget.setPosition(0, 0, getWidth(), getHeight(), HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM);
	}

	@Override
	public Identifier getIdentifier(R recipe) {
		return recipe.getTag().location();
	}
}
