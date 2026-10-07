package eakerzt.jiv.library.plugins.vanilla.gui;

import eakerzt.jiv.api.gui.handlers.IGuiContainerHandler;
import eakerzt.jiv.common.Internal;
import eakerzt.jiv.common.platform.IPlatformRenderHelper;
import eakerzt.jiv.common.platform.IPlatformScreenHelper;
import eakerzt.jiv.common.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class InventoryEffectRendererGuiHandler implements IGuiContainerHandler<AbstractContainerScreen<?>> {
	/**
	 * Modeled after {@link DisplayEffectsScreen#drawActivePotionEffects()}
	 */
	@SuppressWarnings("JavadocReference")
	@Override
	public List<Rect2i> getGuiExtraAreas(AbstractContainerScreen<?> containerScreen) {
		if (!Internal.getJivFeatures().getInventoryEffectRendererGuiHandlerEnabled()) {
			return List.of();
		}
		if (!containerScreen.showsActiveEffects()) {
			return List.of();
		}
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null) {
			return Collections.emptyList();
		}
		Collection<MobEffectInstance> activePotionEffects = player.getActiveEffects();
		if (activePotionEffects.isEmpty()) {
			return Collections.emptyList();
		}

		IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
		List<Rect2i> areas = new ArrayList<>();
		int x = screenHelper.getLeftPos(containerScreen) + screenHelper.getImageWidth(containerScreen) + 2;
		int y = screenHelper.getTopPos(containerScreen);
		// JIV always forces the potion effect renderer to "compact" width mode when JIV is open.
		int width = 32;

		int height = 33;
		if (activePotionEffects.size() > 5) {
			height = 132 / (activePotionEffects.size() - 1);
		}
		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		for (MobEffectInstance potionEffect : activePotionEffects) {
			if (renderHelper.shouldRender(potionEffect)) {
				areas.add(new Rect2i(x, y, width, height));
				y += height;
			}
		}
		return areas;
	}
}
