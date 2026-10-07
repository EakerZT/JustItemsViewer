package eakerzt.jiv.library.runtime;

import eakerzt.jiv.api.helpers.ICodecHelper;
import eakerzt.jiv.api.helpers.IColorHelper;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.api.helpers.IJivHelpers;
import eakerzt.jiv.api.helpers.IModIdHelper;
import eakerzt.jiv.api.helpers.IPlatformFluidHelper;
import eakerzt.jiv.api.helpers.IStackHelper;
import eakerzt.jiv.api.recipe.IFocusFactory;
import eakerzt.jiv.api.recipe.category.IRecipeCategory;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import eakerzt.jiv.api.recipe.vanilla.IVanillaRecipeFactory;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.api.runtime.IIngredientVisibility;
import eakerzt.jiv.common.platform.Services;
import eakerzt.jiv.library.gui.helpers.GuiHelper;
import eakerzt.jiv.library.ingredients.IngredientVisibility;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

public class JivHelpers implements IJivHelpers {
	private final GuiHelper guiHelper;
	private final IStackHelper stackHelper;
	private final IModIdHelper modIdHelper;
	private final IFocusFactory focusFactory;
	private final IColorHelper colorHelper;
	private final IIngredientManager ingredientManager;
	private final IVanillaRecipeFactory vanillaRecipeFactory;
	private final IngredientVisibility ingredientVisibility;
	private final IPlatformFluidHelper<?> platformFluidHelper;
	private final ICodecHelper codecHelper;
	private @Nullable Collection<IRecipeCategory<?>> recipeCategories;

	public JivHelpers(
		GuiHelper guiHelper,
		IStackHelper stackHelper,
		IModIdHelper modIdHelper,
		IFocusFactory focusFactory,
		IColorHelper colorHelper,
		IIngredientManager ingredientManager,
		IVanillaRecipeFactory vanillaRecipeFactory,
		ICodecHelper codecHelper,
		IngredientVisibility ingredientVisibility
	) {
		this.guiHelper = guiHelper;
		this.stackHelper = stackHelper;
		this.modIdHelper = modIdHelper;
		this.focusFactory = focusFactory;
		this.colorHelper = colorHelper;
		this.ingredientManager = ingredientManager;
		this.vanillaRecipeFactory = vanillaRecipeFactory;
		this.ingredientVisibility = ingredientVisibility;
		this.platformFluidHelper = Services.PLATFORM.getFluidHelper();
		this.codecHelper = codecHelper;
	}

	public void setRecipeCategories(Collection<IRecipeCategory<?>> recipeCategories) {
		this.recipeCategories = Collections.unmodifiableCollection(recipeCategories);
	}

	@Override
	public IGuiHelper getGuiHelper() {
		return guiHelper;
	}

	@Override
	public IStackHelper getStackHelper() {
		return stackHelper;
	}

	@Override
	public IModIdHelper getModIdHelper() {
		return modIdHelper;
	}

	@Override
	public IFocusFactory getFocusFactory() {
		return focusFactory;
	}

	@Override
	public IColorHelper getColorHelper() {
		return colorHelper;
	}

	@Override
	public IPlatformFluidHelper<?> getPlatformFluidHelper() {
		return platformFluidHelper;
	}

	@Override
	public <T> Optional<IRecipeType<T>> getRecipeType(Identifier uid, Class<? extends T> recipeClass) {
		return getRecipeType(uid)
			.filter(t -> t.getRecipeClass().equals(recipeClass))
			.map(t -> {
				@SuppressWarnings("unchecked")
				IRecipeType<T> cast = (IRecipeType<T>) t;
				return cast;
			});
	}

	@Override
	public Optional<IRecipeType<?>> getRecipeType(Identifier uid) {
		return Optional.ofNullable(this.recipeCategories)
			.flatMap(r -> r.stream()
				.map(IRecipeCategory::getRecipeType)
				.filter(t -> t.getUid().equals(uid))
				.findFirst()
			);
	}

	@Override
	public Stream<IRecipeType<?>> getAllRecipeTypes() {
		if (this.recipeCategories == null) {
			return Stream.of();
		}
		return this.recipeCategories.stream()
			.map(IRecipeCategory::getRecipeType);
	}

	@Override
	public IIngredientManager getIngredientManager() {
		return ingredientManager;
	}

	@Override
	public ICodecHelper getCodecHelper() {
		return codecHelper;
	}

	@Override
	public IVanillaRecipeFactory getVanillaRecipeFactory() {
		return vanillaRecipeFactory;
	}

	@Override
	public IIngredientVisibility getIngredientVisibility() {
		return ingredientVisibility;
	}

	public void onRuntimeStopped() {
		ingredientVisibility.onRuntimeStopped();
	}
}
