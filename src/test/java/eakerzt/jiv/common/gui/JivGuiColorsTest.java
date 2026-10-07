package eakerzt.jiv.common.gui;

import com.google.gson.JsonParser;
import eakerzt.jiv.common.gui.JivGuiColors.GuiColor;
import net.minecraft.DetectedVersion;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class JivGuiColorsTest {
	@AfterEach
	public void resetGuiColors() {
		try (CloseableResourceManager resourceManager = new MultiPackResourceManager(PackType.CLIENT_RESOURCES, List.of())) {
			JivGuiColors.onResourceManagerReload(resourceManager);
		}
	}

	@Test
	public void loadColorsFromResourcePack(@TempDir Path tempDir) throws IOException {
		Path resourcePack = createResourcePack(tempDir, """
			{
			"recipeTextWidgetText": "0x112233",
			"pageNavigationBackground": "0x80224466"
			}
			""");
		try (CloseableResourceManager resourceManager = createResourceManager(resourcePack)) {
			JivGuiColors.onResourceManagerReload(resourceManager);
		}

		Assertions.assertEquals(0xFF112233, JivGuiColors.getColor(GuiColor.RECIPE_TEXT_WIDGET_TEXT));
		Assertions.assertEquals(0x80224466, JivGuiColors.getColor(GuiColor.PAGE_NAVIGATION_BACKGROUND));
		Assertions.assertEquals(GuiColor.ANVIL_EXPERIENCE_COST_ERROR_TEXT.getDefaultColor(), JivGuiColors.getColor(GuiColor.ANVIL_EXPERIENCE_COST_ERROR_TEXT));
	}

	@Test
	public void parseArgbColorString() {
		Assertions.assertEquals(0xFF808080, JivGuiColors.parseColorString("0xFF808080").orElseThrow());
		Assertions.assertEquals(0xDDFF0000, JivGuiColors.parseColorString("0xDDFF0000").orElseThrow());
		Assertions.assertEquals(0x30000000, JivGuiColors.parseColorString("0x30000000").orElseThrow());
	}

	@Test
	public void parseRgbColorStringAsOpaqueArgb() {
		Assertions.assertEquals(0xFF808080, JivGuiColors.parseColorString("0x808080").orElseThrow());
		Assertions.assertEquals(0xFFFFFFFF, JivGuiColors.parseColorString("0xFFFFFF").orElseThrow());
	}

	@Test
	public void rejectInvalidColor() {
		Assertions.assertTrue(JivGuiColors.parseColor(JsonParser.parseString("805306368")).isEmpty());
		Assertions.assertTrue(JivGuiColors.parseColorString("#123456").isEmpty());
		Assertions.assertTrue(JivGuiColors.parseColorString("123456").isEmpty());
		Assertions.assertTrue(JivGuiColors.parseColorString("0x12345").isEmpty());
		Assertions.assertTrue(JivGuiColors.parseColorString("0xGG000000").isEmpty());
		Assertions.assertTrue(JivGuiColors.parseColorString("0x123456789").isEmpty());
		Assertions.assertTrue(JivGuiColors.parseColor(JsonParser.parseString("true")).isEmpty());
	}

	private static Path createResourcePack(Path tempDir, CharSequence overrides) throws IOException {
		Path resourcePack = tempDir.resolve("jiv-color-overrides");
		Files.createDirectories(resourcePack.resolve("assets/jiv/gui"));
		var packFormat = DetectedVersion.BUILT_IN
			.packVersion(PackType.CLIENT_RESOURCES);
		Files.writeString(resourcePack.resolve("pack.mcmeta"), """
			{
			"pack": {
				"pack_format": {
				"major": %d,
				"minor": %d
				},
				"description": "JIV GUI color override test"
			}
			}
			""".formatted(
			packFormat.major(),
			packFormat.minor()
		));
		Files.writeString(resourcePack.resolve("assets/jiv/gui/colors.json"), overrides);
		return resourcePack;
	}

	private static CloseableResourceManager createResourceManager(Path resourcePack) {
		PackLocationInfo locationInfo = new PackLocationInfo(
			"jiv-color-overrides",
			Component.literal("JIV GUI color overrides"),
			PackSource.DEFAULT,
			Optional.empty()
		);
		PackResources packResources = new PathPackResources(locationInfo, resourcePack);
		return new MultiPackResourceManager(PackType.CLIENT_RESOURCES, List.of(packResources));
	}
}
