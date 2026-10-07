package eakerzt.jiv.common.config;

import eakerzt.jiv.config.api.sorting.ISortingConfig;

public interface IClientConfigs {
	IClientConfig getClientConfig();

	IIngredientFilterConfig getIngredientFilterConfig();

	IIngredientGridConfig getIngredientListConfig();

	IIngredientGridConfig getBookmarkListConfig();

	ISortingConfig<String> getRecipeCategorySortingConfig();

	void registerRuntimeListenerRemoval(Runnable listenerRemoval);

	void onRuntimeStopped();
}
