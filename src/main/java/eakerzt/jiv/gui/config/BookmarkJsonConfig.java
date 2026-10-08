package eakerzt.jiv.gui.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import eakerzt.jiv.api.helpers.ICodecHelper;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.api.recipe.IFocusFactory;
import eakerzt.jiv.api.recipe.IRecipeManager;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.util.PathUtil;
import eakerzt.jiv.common.util.ServerConfigPathUtil;
import eakerzt.jiv.gui.bookmarks.BookmarkList;
import eakerzt.jiv.gui.bookmarks.BookmarkPage;
import eakerzt.jiv.gui.bookmarks.BookmarkWorkspaceJson;
import eakerzt.jiv.gui.bookmarks.IBookmark;

import net.minecraft.core.RegistryAccess;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BookmarkJsonConfig implements IBookmarkConfig {
	private static final Logger LOGGER = LogManager.getLogger();

	private final Path jivConfigurationDir;
	private final Set<Path> failedBackups = new HashSet<>();

	private static Optional<Path> getPath(Path jivConfigurationDir) {
		return ServerConfigPathUtil.getWorldPath(jivConfigurationDir)
				.flatMap(
						configPath -> {
							try {
								Files.createDirectories(configPath);
							} catch (IOException e) {
								LOGGER.error(
										"Unable to create bookmark config folder: {}",
										configPath,
										e);
								return Optional.empty();
							}
							Path path = configPath.resolve("bookmarks.json");
							return Optional.of(path);
						});
	}

	public BookmarkJsonConfig(Path jivConfigurationDir) {
		this.jivConfigurationDir = jivConfigurationDir;
	}

	@Override
	public void saveBookmarks(
			IRecipeManager recipes,
			IFocusFactory focuses,
			IGuiHelper gui,
			IIngredientManager ingredients,
			RegistryAccess registries,
			ICodecHelper codecs,
			List<IBookmark> bookmarks,
			Codec<IBookmark> codec) {
		BookmarkPage page = new BookmarkPage();
		page.bookmarks.addAll(bookmarks);
		saveWorkspace(
				recipes, focuses, gui, ingredients, registries, codecs, List.of(page), 0, codec);
	}

	@Override
	public void saveWorkspace(
			IRecipeManager recipes,
			IFocusFactory focuses,
			IGuiHelper gui,
			IIngredientManager ingredients,
			RegistryAccess registries,
			ICodecHelper codecs,
			List<BookmarkPage> pages,
			int namespace,
			Codec<IBookmark> codec) {
		// Persist a complete snapshot atomically, including namespace and group metadata.
		try {
			var json =
					BookmarkWorkspaceJson.encode(
							pages,
							namespace,
							codec,
							registries.createSerializationContext(JsonOps.INSTANCE));
			getPath(jivConfigurationDir)
					.filter(path -> !failedBackups.contains(path))
					.ifPresent(path -> write(path, json));
		} catch (RuntimeException error) {
			LOGGER.error("Unable to encode bookmark workspace; original file retained", error);
		}
	}

	private void write(Path path, JsonObject json) {
		try {
			Path temp = Files.createTempFile(path.getParent(), "bookmarks-", ".json.tmp");
			try {
				Files.writeString(
						temp, new GsonBuilder().setPrettyPrinting().create().toJson(json));
				PathUtil.moveAtomicReplace(temp, path);
			} finally {
				Files.deleteIfExists(temp);
			}
		} catch (IOException | RuntimeException error) {
			LOGGER.error("Unable to save bookmark workspace {}", path, error);
		}
	}

	@Override
	public void loadBookmarks(
			IRecipeManager recipes,
			IFocusFactory focuses,
			IGuiHelper gui,
			IIngredientManager ingredients,
			RegistryAccess registries,
			BookmarkList list,
			ICodecHelper codecs,
			Codec<IBookmark> codec) {
		getPath(jivConfigurationDir)
				.ifPresent(
						path -> {
							if (!Files.exists(path)) {
								list.setFromConfigFile(List.of());
								return;
							}
							try (var reader = Files.newBufferedReader(path)) {
								var json = JsonParser.parseReader(reader);
								var workspace =
										BookmarkWorkspaceJson.decode(
												json,
												codec,
												registries.createSerializationContext(
														JsonOps.INSTANCE));
								if (json.isJsonArray())
									Files.copy(
											path,
											path.resolveSibling("bookmarks-v3.backup.json"),
											StandardCopyOption.REPLACE_EXISTING);
								list.setWorkspace(workspace.pages(), workspace.namespace());
								if (workspace.pages().stream()
										.anyMatch(p -> !p.unresolved.isEmpty()))
									LOGGER.warn(
											"Unresolved bookmarks retained in workspace {}", path);
							} catch (IOException | RuntimeException error) {
								LOGGER.error(
										"Unable to load bookmark workspace {}; backing up before"
												+ " any new edits",
										path,
										error);
								try {
									Files.copy(
											path,
											path.resolveSibling(
													"bookmarks-failed-"
															+ System.currentTimeMillis()
															+ ".json"));
								} catch (IOException backupError) {
									failedBackups.add(path);
									LOGGER.error(
											"Unable to back up bookmark workspace; saving disabled"
													+ " for this file",
											backupError);
								}
							}
						});
	}
}
