package eakerzt.jiv.gui.plugins;

import eakerzt.jiv.api.IModPlugin;
import eakerzt.jiv.api.JivPlugin;
import eakerzt.jiv.api.constants.ModIds;
import eakerzt.jiv.api.registration.IGuiHandlerRegistration;
import eakerzt.jiv.api.runtime.IJivFeatures;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.gui.recipes.RecipesGui;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

@JivPlugin
public class JivGuiPlugin implements IModPlugin {
	private @Nullable IJivFeatures jivFeatures;

	@Override
	public Identifier getPluginUid() {
		return Identifier.fromNamespaceAndPath(ModIds.JIV_ID, "gui");
	}

	@Override
	public void configureJiv(IJivFeatures jivFeatures) {
		this.jivFeatures = jivFeatures;
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		if (!isJivGuiEnabled()) {
			return;
		}

		IIngredientManager ingredientManager = registration.getJivHelpers().getIngredientManager();
		registration.addGenericGuiScreenHandler(AbstractContainerScreen.class, new AbstractContainerScreenHandler<>());
		registration.addGuiScreenHandler(ChatScreen.class, new ChatScreenHandler(ingredientManager));
		registration.addGuiScreenHandler(RecipesGui.class, RecipesGui::getProperties);
	}

	private boolean isJivGuiEnabled() {
		IJivFeatures jivFeatures = this.jivFeatures;
		return jivFeatures == null || jivFeatures.isJivGuiEnabled();
	}
}
