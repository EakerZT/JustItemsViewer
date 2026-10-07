package eakerzt.jiv.library.load.registration;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import eakerzt.jiv.api.IModPlugin;
import eakerzt.jiv.api.helpers.IColorHelper;
import eakerzt.jiv.api.ingredients.IIngredientHelper;
import eakerzt.jiv.api.ingredients.IIngredientRenderer;
import eakerzt.jiv.api.ingredients.IIngredientType;
import eakerzt.jiv.api.ingredients.IIngredientTypeWithSubtypes;
import eakerzt.jiv.api.ingredients.ITypedIngredient;
import eakerzt.jiv.api.ingredients.subtypes.ISubtypeManager;
import eakerzt.jiv.api.registration.IExtraIngredientRegistration;
import eakerzt.jiv.api.registration.IIngredientAliasRegistration;
import eakerzt.jiv.api.registration.IModIngredientRegistration;
import eakerzt.jiv.api.registration.ISlotDisplayInterpreterRegistration;
import eakerzt.jiv.common.platform.IPlatformFluidHelperInternal;
import eakerzt.jiv.common.platform.Services;
import eakerzt.jiv.common.ingredients.TypedIngredientUtil;
import eakerzt.jiv.common.util.ErrorUtil;
import eakerzt.jiv.library.ingredients.IngredientInfo;
import eakerzt.jiv.library.ingredients.IngredientManager;
import eakerzt.jiv.library.ingredients.RegisteredIngredients;
import eakerzt.jiv.common.ingredients.TypedIngredient;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.level.material.Fluid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;

public class IngredientManagerBuilder implements IModIngredientRegistration, IIngredientAliasRegistration, IExtraIngredientRegistration {
	private static final Logger LOGGER = LogManager.getLogger();

	private final SequencedMap<IIngredientType<?>, IngredientInfo<?>> ingredientInfos = new LinkedHashMap<>();
	private final ISubtypeManager subtypeManager;
	private final IColorHelper colorHelper;
	private final ContextMap contextMap;
	private final SlotDisplayInterpreterRegistration slotDisplayInterpreterRegistration = new SlotDisplayInterpreterRegistration();
	@Nullable
	private Identifier registeringPluginUid;

	public IngredientManagerBuilder(ISubtypeManager subtypeManager, IColorHelper colorHelper, ContextMap contextMap) {
		this.subtypeManager = subtypeManager;
		this.colorHelper = colorHelper;
		this.contextMap = contextMap;
	}

	public void registerIngredients(IModPlugin plugin) {
		@Nullable
		Identifier previousPluginUid = registeringPluginUid;
		registeringPluginUid = plugin.getPluginUid();
		try {
			plugin.registerIngredients(this);
		} finally {
			registeringPluginUid = previousPluginUid;
		}
	}

	@Override
	public <V> void register(
		IIngredientType<V> ingredientType,
		Collection<V> allIngredients,
		IIngredientHelper<V> ingredientHelper,
		IIngredientRenderer<V> ingredientRenderer,
		Codec<V> ingredientCodec
	) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(allIngredients, "allIngredients");
		ErrorUtil.checkNotNull(ingredientHelper, "ingredientHelper");
		ErrorUtil.checkNotNull(ingredientRenderer, "ingredientRenderer");
		ErrorUtil.checkNotNull(ingredientCodec, "ingredientCodec");
		Preconditions.checkArgument(ingredientRenderer.getWidth() == 16,
			"the default ingredient renderer registered here will be used for drawing " +
				"ingredients in the ingredient list, and it must have a width of 16"
		);
		Preconditions.checkArgument(ingredientRenderer.getHeight() == 16,
			"the default ingredient renderer registered here will be used for drawing " +
				"ingredients in the ingredient list, and it must have a height of 16"
		);

		if (ingredientInfos.containsKey(ingredientType)) {
			throw new IllegalArgumentException("Ingredient type has already been registered: " + ingredientType.getIngredientClass());
		}

		List<ITypedIngredient<V>> allTypedIngredients = new ArrayList<>(allIngredients.size());
		for (V ingredient : allIngredients) {
			if (!ingredientHelper.isIngredientOnServer(ingredient)) {
				String errorInfo = ingredientHelper.getErrorInfo(ingredient);
				LOGGER.warn("Attempted to add an Ingredient that is not on the server: {}", errorInfo);
				continue;
			}
			ITypedIngredient<V> typedIngredient = TypedIngredient.createAndFilterInvalid(ingredientHelper, ingredientType, ingredient, false);
			if (typedIngredient == null) {
				LOGGER.warn("Detected an invalid ingredient during ingredient registration: {}", ingredientHelper.getErrorInfo(ingredient));
				continue;
			}

			allTypedIngredients.add(typedIngredient);
		}

		ingredientInfos.put(ingredientType, new IngredientInfo<>(
			ingredientType,
			allTypedIngredients,
			ingredientHelper,
			ingredientRenderer,
			ingredientCodec,
			registeringPluginUid
		));
	}

	@Override
	public <V> void addExtraIngredients(IIngredientType<V> ingredientType, Collection<V> extraIngredients) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(extraIngredients, "extraIngredients");

		IngredientInfo<V> castIngredientInfo = getIngredientInfo(ingredientType);
		IIngredientHelper<V> ingredientHelper = castIngredientInfo.getIngredientHelper();

		List<ITypedIngredient<V>> extraTypedIngredients = new ArrayList<>(extraIngredients.size());
		for (V ingredient : extraIngredients) {
			if (!ingredientHelper.isIngredientOnServer(ingredient)) {
				String errorInfo = ingredientHelper.getErrorInfo(ingredient);
				LOGGER.warn("Attempted to add an extra Ingredient that is not on the server: {}", errorInfo);
				continue;
			}

			ITypedIngredient<V> typedIngredient = TypedIngredient.createAndFilterInvalid(ingredientHelper, ingredientType, ingredient, false);
			if (typedIngredient == null) {
				LOGGER.warn("Detected an invalid ingredient when adding extra ingredients: {}", ingredientHelper.getErrorInfo(ingredient));
				continue;
			}

			extraTypedIngredients.add(typedIngredient);
		}
		castIngredientInfo.addIngredients(extraTypedIngredients);
	}

	@Override
	public <I> void addAlias(IIngredientType<I> type, I ingredient, String alias) {
		ErrorUtil.checkNotNull(type, "type");
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		ErrorUtil.checkNotNull(alias, "alias");
		checkIngredientType(type, ingredient);

		IngredientInfo<I> ingredientInfo = getIngredientInfo(type);
		ingredientInfo.addIngredientAlias(ingredient, alias);
	}

	@Override
	public <B, I> void addAlias(IIngredientTypeWithSubtypes<B, I> type, B baseIngredient, String alias) {
		ErrorUtil.checkNotNull(type, "type");
		ErrorUtil.checkNotNull(baseIngredient, "baseIngredient");
		ErrorUtil.checkNotNull(alias, "alias");
		checkBaseIngredientType(type, baseIngredient);

		IngredientInfo<I> ingredientInfo = getIngredientInfo(type);
		ingredientInfo.addBaseIngredientAlias(baseIngredient, alias);
	}

	@Override
	public void addAlias(Fluid fluid, String alias) {
		ErrorUtil.checkNotNull(fluid, "fluid");
		ErrorUtil.checkNotNull(alias, "alias");

		IPlatformFluidHelperInternal<?> fluidHelper = Services.PLATFORM.getFluidHelper();
		IIngredientTypeWithSubtypes<Fluid, ?> fluidIngredientType = fluidHelper.getFluidIngredientType();
		addAlias(fluidIngredientType, fluid, alias);
	}

	@Override
	public <I> void addAlias(ITypedIngredient<I> typedIngredient, String alias) {
		ErrorUtil.checkNotNull(typedIngredient, "typedIngredient");
		ErrorUtil.checkNotNull(alias, "alias");

		IngredientInfo<I> ingredientInfo = getIngredientInfo(typedIngredient.getType());
		IIngredientHelper<I> ingredientHelper = ingredientInfo.getIngredientHelper();
		ITypedIngredient<I> checkedIngredient = TypedIngredientUtil.checkTypedIngredientFromApi(ingredientHelper, typedIngredient);
		ingredientInfo.addIngredientAlias(checkedIngredient, alias);
	}

	@Override
	public <I> void addAliases(IIngredientType<I> type, I ingredient, Collection<String> aliases) {
		ErrorUtil.checkNotNull(type, "type");
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		ErrorUtil.checkNotNull(aliases, "aliases");
		checkIngredientType(type, ingredient);

		IngredientInfo<I> ingredientInfo = getIngredientInfo(type);
		ingredientInfo.addIngredientAliases(ingredient, aliases);
	}

	@Override
	public <B, I> void addAliases(IIngredientTypeWithSubtypes<B, I> type, B baseIngredient, Collection<String> aliases) {
		ErrorUtil.checkNotNull(type, "type");
		ErrorUtil.checkNotNull(baseIngredient, "baseIngredient");
		ErrorUtil.checkNotNull(aliases, "aliases");
		checkBaseIngredientType(type, baseIngredient);

		IngredientInfo<I> ingredientInfo = getIngredientInfo(type);
		ingredientInfo.addBaseIngredientAliases(baseIngredient, aliases);
	}

	@Override
	public void addAliases(Fluid fluid, Collection<String> aliases) {
		ErrorUtil.checkNotNull(fluid, "fluid");
		ErrorUtil.checkNotNull(aliases, "aliases");

		IPlatformFluidHelperInternal<?> fluidHelper = Services.PLATFORM.getFluidHelper();
		IIngredientTypeWithSubtypes<Fluid, ?> fluidIngredientType = fluidHelper.getFluidIngredientType();
		addAliases(fluidIngredientType, fluid, aliases);
	}

	@Override
	public <I> void addAliases(ITypedIngredient<I> typedIngredient, Collection<String> aliases) {
		ErrorUtil.checkNotNull(typedIngredient, "typedIngredient");
		ErrorUtil.checkNotNull(aliases, "aliases");

		IngredientInfo<I> ingredientInfo = getIngredientInfo(typedIngredient.getType());
		IIngredientHelper<I> ingredientHelper = ingredientInfo.getIngredientHelper();
		ITypedIngredient<I> checkedIngredient = TypedIngredientUtil.checkTypedIngredientFromApi(ingredientHelper, typedIngredient);
		ingredientInfo.addIngredientAliases(checkedIngredient, aliases);
	}

	@Override
	public <I> void addAliases(IIngredientType<I> type, Collection<I> ingredients, String alias) {
		ErrorUtil.checkNotNull(type, "type");
		ErrorUtil.checkNotNull(ingredients, "ingredients");
		ErrorUtil.checkNotNull(alias, "alias");

		IngredientInfo<I> ingredientInfo = getIngredientInfo(type);
		for (I ingredient : ingredients) {
			ingredientInfo.addIngredientAlias(ingredient, alias);
		}
	}

	@Override
	public <I> void addAliases(Collection<ITypedIngredient<I>> typedIngredients, String alias) {
		ErrorUtil.checkNotNull(typedIngredients, "typedIngredients");
		ErrorUtil.checkNotNull(alias, "alias");

		for (ITypedIngredient<I> typedIngredient : typedIngredients) {
			addAlias(typedIngredient, alias);
		}
	}

	@Override
	public <I> void addAliases(IIngredientType<I> type, Collection<I> ingredients, Collection<String> aliases) {
		ErrorUtil.checkNotNull(type, "type");
		ErrorUtil.checkNotNull(ingredients, "ingredients");
		ErrorUtil.checkNotNull(aliases, "aliases");

		IngredientInfo<I> ingredientInfo = getIngredientInfo(type);
		for (I ingredient : ingredients) {
			ingredientInfo.addIngredientAliases(ingredient, aliases);
		}
	}

	@Override
	public <I> void addAliases(Collection<ITypedIngredient<I>> typedIngredients, Collection<String> aliases) {
		ErrorUtil.checkNotNull(typedIngredients, "typedIngredients");
		ErrorUtil.checkNotNull(aliases, "aliases");

		for (ITypedIngredient<I> typedIngredient : typedIngredients) {
			addAliases(typedIngredient, aliases);
		}
	}

	private static <I> void checkIngredientType(IIngredientType<I> type, I ingredient) {
		Class<? extends I> ingredientClass = type.getIngredientClass();
		if (!ingredientClass.isInstance(ingredient)) {
			throw new IllegalArgumentException(String.format("ingredient (%s) must be an instance of %s", ingredient.getClass(), ingredientClass));
		}
	}

	private static <B, I> void checkBaseIngredientType(IIngredientTypeWithSubtypes<B, I> type, B baseIngredient) {
		Class<? extends B> ingredientBaseClass = type.getIngredientBaseClass();
		if (!ingredientBaseClass.isInstance(baseIngredient)) {
			throw new IllegalArgumentException(String.format("baseIngredient (%s) must be an instance of %s", baseIngredient.getClass(), ingredientBaseClass));
		}
	}

	private <T> IngredientInfo<T> getIngredientInfo(IIngredientType<T> ingredientType) {
		IngredientInfo<?> ingredientInfo = ingredientInfos.get(ingredientType);
		if (ingredientInfo == null) {
			throw new IllegalArgumentException("Ingredient type has not been registered: " + ingredientType.getUid());
		}
		@SuppressWarnings("unchecked")
		IngredientInfo<T> cast = (IngredientInfo<T>) ingredientInfo;
		return cast;
	}

	@Override
	public ISubtypeManager getSubtypeManager() {
		return subtypeManager;
	}

	@Override
	public IColorHelper getColorHelper() {
		return colorHelper;
	}

	@Override
	public ContextMap getContextMap() {
		return contextMap;
	}

	public ISlotDisplayInterpreterRegistration getSlotDisplayInterpreterRegistration() {
		return slotDisplayInterpreterRegistration;
	}

	public IngredientManager build() {
		RegisteredIngredients registeredIngredients = new RegisteredIngredients(ingredientInfos);
		return new IngredientManager(
			registeredIngredients,
			slotDisplayInterpreterRegistration.createRegistry()
		);
	}
}
