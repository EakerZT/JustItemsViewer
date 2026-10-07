package eakerzt.jiv.common.gui;

import com.google.gson.JsonObject;
import com.google.gson.GsonBuilder;
import eakerzt.jiv.api.constants.ModIds;
import eakerzt.jiv.common.gui.JivGuiColors.GuiColor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class JivGuiColorsDataGenerator {
	private JivGuiColorsDataGenerator() {

	}

	public static void main(String[] args) throws IOException {
		Path outputPath = Path.of(args[0], "assets", ModIds.JIV_ID, "gui", "colors.json");
		JsonObject json = new JsonObject();
		json.addProperty("_comment", "JIV GUI colors. Override from a resource pack at assets/jiv/gui/colors.json. Values use hex format: 0xAARRGGBB, or 0xRRGGBB for fully opaque colors. Packs may include only the colors they change.");
		for (GuiColor color : GuiColor.values()) {
			json.addProperty(color.getKey(), color.getDefaultColorString());
		}
		Files.createDirectories(outputPath.getParent());
		Files.writeString(outputPath, new GsonBuilder().setPrettyPrinting().create().toJson(json) + "\n", StandardCharsets.UTF_8);
	}
}
