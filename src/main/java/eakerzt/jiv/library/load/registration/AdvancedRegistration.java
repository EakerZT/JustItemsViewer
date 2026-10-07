package eakerzt.jiv.library.load.registration;

import com.google.common.collect.ImmutableListMultimap;
import eakerzt.jiv.api.helpers.IJivHelpers;
import eakerzt.jiv.api.recipe.advanced.IRecipeButtonControllerFactory;
import eakerzt.jiv.api.recipe.advanced.IRecipeManagerPlugin;
import eakerzt.jiv.api.recipe.advanced.IRecipeManagerPluginHelper;
import eakerzt.jiv.api.recipe.advanced.ISimpleRecipeManagerPlugin;
import eakerzt.jiv.api.recipe.category.extensions.IRecipeCategoryDecorator;
import eakerzt.jiv.api.recipe.types.IRecipeType;
import eakerzt.jiv.api.registration.IAdvancedRegistration;
import eakerzt.jiv.common.util.ErrorUtil;
import eakerzt.jiv.common.collect.ListMultiMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

public class AdvancedRegistration implements IAdvancedRegistration {
	private static final Logger LOGGER = LogManager.getLogger();

	private final List<IRecipeManagerPlugin> recipeManagerPlugins = new ArrayList<>();
	private final ListMultiMap<IRecipeType<?>, IRecipeCategoryDecorator<?>> recipeCategoryDecorators = new ListMultiMap<>();
	private final List<IRecipeButtonControllerFactory> recipeButtonControllerFactories = new ArrayList<>();
	private final IJivHelpers jivHelpers;
	private final IRecipeManagerPluginHelper pluginHelper;

	public AdvancedRegistration(IJivHelpers jivHelpers, IRecipeManagerPluginHelper pluginHelper) {
		this.jivHelpers = jivHelpers;
		this.pluginHelper = pluginHelper;
	}

	@Override
	public void addRecipeManagerPlugin(IRecipeManagerPlugin recipeManagerPlugin) {
		ErrorUtil.checkNotNull(recipeManagerPlugin, "recipeManagerPlugin");

		LOGGER.info("Added recipe manager plugin: {}", recipeManagerPlugin.getClass());
		recipeManagerPlugins.add(recipeManagerPlugin);
	}

	@Override
	public <T> void addSimpleRecipeManagerPlugin(IRecipeType<T> recipeType, ISimpleRecipeManagerPlugin<T> recipeManagerPlugin) {
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.checkNotNull(recipeManagerPlugin, "recipeManagerPlugin");

		SingleTypeRecipeManagerPluginAdapter<T> adapter = new SingleTypeRecipeManagerPluginAdapter<>(pluginHelper, recipeType, recipeManagerPlugin);
		LOGGER.info("Added typed recipe manager plugin: {}", recipeManagerPlugin.getClass());
		recipeManagerPlugins.add(adapter);
	}

	@Override
	public <T> void addRecipeCategoryDecorator(IRecipeType<T> recipeType, IRecipeCategoryDecorator<T> decorator) {
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.checkNotNull(decorator, "decorator");

		LOGGER.info("Added recipe category decorator: {} for recipe type: {}", decorator.getClass(), recipeType.getUid());
		recipeCategoryDecorators.put(recipeType, decorator);
	}

	@Override
	public void addRecipeButtonFactory(IRecipeButtonControllerFactory recipeButtonControllerFactory) {
		this.recipeButtonControllerFactories.add(recipeButtonControllerFactory);
	}

	@Override
	public IJivHelpers getJivHelpers() {
		return jivHelpers;
	}

	@Override
	public IRecipeManagerPluginHelper getRecipeManagerPluginHelper() {
		return pluginHelper;
	}

	@Unmodifiable
	public List<IRecipeManagerPlugin> getRecipeManagerPlugins() {
		return List.copyOf(recipeManagerPlugins);
	}

	@Unmodifiable
	public List<IRecipeButtonControllerFactory> getRecipeButtonControllerFactories() {
		return List.copyOf(recipeButtonControllerFactories);
	}

	@Unmodifiable
	public ImmutableListMultimap<IRecipeType<?>, IRecipeCategoryDecorator<?>> getRecipeCategoryDecorators() {
		return recipeCategoryDecorators.toImmutable();
	}
}
