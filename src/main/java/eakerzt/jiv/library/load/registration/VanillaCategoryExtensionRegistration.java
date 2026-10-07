package eakerzt.jiv.library.load.registration;

import eakerzt.jiv.api.helpers.IJivHelpers;
import eakerzt.jiv.api.recipe.category.extensions.vanilla.brewing.IExtendableBrewingRecipeCategory;
import eakerzt.jiv.api.recipe.category.extensions.vanilla.crafting.IExtendableCraftingRecipeCategory;
import eakerzt.jiv.api.recipe.category.extensions.vanilla.smithing.IExtendableSmithingRecipeCategory;
import eakerzt.jiv.api.registration.IVanillaCategoryExtensionRegistration;
import eakerzt.jiv.library.runtime.JivHelpers;

public class VanillaCategoryExtensionRegistration implements IVanillaCategoryExtensionRegistration {
	private final IExtendableCraftingRecipeCategory craftingCategory;
	private final IExtendableSmithingRecipeCategory smithingCategory;
	private final IExtendableBrewingRecipeCategory brewingCategory;
	private final JivHelpers jivHelpers;

	public VanillaCategoryExtensionRegistration(
		IExtendableCraftingRecipeCategory craftingCategory,
		IExtendableSmithingRecipeCategory smithingCategory,
		IExtendableBrewingRecipeCategory brewingCategory,
		JivHelpers jivHelpers
	) {
		this.craftingCategory = craftingCategory;
		this.smithingCategory = smithingCategory;
		this.brewingCategory = brewingCategory;
		this.jivHelpers = jivHelpers;
	}

	@Override
	public IExtendableCraftingRecipeCategory getCraftingCategory() {
		return craftingCategory;
	}

	@Override
	public IExtendableSmithingRecipeCategory getSmithingCategory() {
		return smithingCategory;
	}

	@Override
	public IJivHelpers getJivHelpers() {
		return jivHelpers;
	}

	@Override
	public IExtendableBrewingRecipeCategory getBrewingCategory() {
		return brewingCategory;
	}
}
