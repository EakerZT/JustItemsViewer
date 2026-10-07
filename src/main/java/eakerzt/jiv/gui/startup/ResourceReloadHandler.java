package eakerzt.jiv.gui.startup;

import eakerzt.jiv.common.util.LoggedTimer;
import eakerzt.jiv.gui.ingredients.IngredientFilter;
import eakerzt.jiv.gui.overlay.IngredientListOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class ResourceReloadHandler implements ResourceManagerReloadListener {
	private final IngredientListOverlay ingredientListOverlay;
	private final IngredientFilter ingredientFilter;

	public ResourceReloadHandler(IngredientListOverlay ingredientListOverlay, IngredientFilter ingredientFilter) {
		this.ingredientListOverlay = ingredientListOverlay;
		this.ingredientFilter = ingredientFilter;
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		LoggedTimer timer = new LoggedTimer();
		timer.start("Rebuilding ingredient filter");
		ingredientFilter.rebuildItemFilter();
		timer.stop();

		Minecraft minecraft = Minecraft.getInstance();
		ingredientListOverlay.getScreenPropertiesUpdater()
			.updateScreen(minecraft.screen)
			.forceUpdate();
	}
}
