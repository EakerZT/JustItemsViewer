package eakerzt.jiv.gui.config;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import eakerzt.jiv.api.helpers.ICodecHelper;
import eakerzt.jiv.api.helpers.IGuiHelper;
import eakerzt.jiv.api.recipe.IFocusFactory;
import eakerzt.jiv.api.recipe.IRecipeManager;
import eakerzt.jiv.api.runtime.IIngredientManager;
import eakerzt.jiv.common.config.file.JsonArrayFileHelper;
import eakerzt.jiv.common.util.DeduplicatingRunner;
import eakerzt.jiv.common.util.ServerConfigPathUtil;
import eakerzt.jiv.gui.bookmarks.BookmarkList;
import eakerzt.jiv.gui.bookmarks.IBookmark;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Unmodifiable;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class BookmarkJsonConfig implements IBookmarkConfig {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Duration SAVE_DELAY_TIME = Duration.ofSeconds(5);
	private static final int VERSION = 3;

	private final Path jivConfigurationDir;
	private final DeduplicatingRunner delayedSave = new DeduplicatingRunner(SAVE_DELAY_TIME);

	private static Optional<Path> getPath(Path jivConfigurationDir) {
		return ServerConfigPathUtil.getWorldPath(jivConfigurationDir)
			.flatMap(configPath -> {
				try {
					Files.createDirectories(configPath);
				} catch (IOException e) {
					LOGGER.error("Unable to create bookmark config folder: {}", configPath, e);
					return Optional.empty();
				}
				Path path = configPath.resolve("bookmarks.json");
				return Optional.of(path);
			});
	}

	public BookmarkJsonConfig(Path jivConfigurationDir) {
		this.jivConfigurationDir = jivConfigurationDir;
	}

	private RegistryOps<JsonElement> getRegistryOps(RegistryAccess registryAccess) {
		return registryAccess.createSerializationContext(JsonOps.INSTANCE);
	}

	@Override
	public void saveBookmarks(
		IRecipeManager recipeManager,
		IFocusFactory focusFactory,
		IGuiHelper guiHelper,
		IIngredientManager ingredientManager,
		RegistryAccess registryAccess,
		ICodecHelper codecHelper,
		List<IBookmark> bookmarks,
		Codec<IBookmark> bookmarkCodec
	) {
		List<IBookmark> bookmarksSnapshot = List.copyOf(bookmarks);
		getPath(jivConfigurationDir)
			.ifPresent(path -> {
				delayedSave.run(() -> {
					save(path, registryAccess, bookmarksSnapshot, bookmarkCodec);
				});
			});
	}

	private void save(Path path, RegistryAccess registryAccess, Collection<IBookmark> bookmarks, Codec<IBookmark> bookmarkCodec) {
		RegistryOps<JsonElement> registryOps = getRegistryOps(registryAccess);

		try {
			JsonArrayFileHelper.write(
				path,
				VERSION,
				bookmarks,
				bookmarkCodec,
				registryOps,
				error -> {
					LOGGER.error("Encountered an error when saving the bookmarks config to file {}\n{}", path, error);
				},
				(element, exception) -> {
					LOGGER.error("Encountered an exception when saving the bookmarks config to file {}\n{}", path, element, exception);
				}
			);
			LOGGER.debug("Saved bookmarks config to file: {}", path);
		} catch (RuntimeException | IOException e) {
			LOGGER.error("Failed to save bookmarks config to file {}", path, e);
		}
	}

	@Override
	public void loadBookmarks(
		IRecipeManager recipeManager,
		IFocusFactory focusFactory,
		IGuiHelper guiHelper,
		IIngredientManager ingredientManager,
		RegistryAccess registryAccess,
		BookmarkList bookmarkList,
		ICodecHelper codecHelper,
		Codec<IBookmark> bookmarkCodec
	) {
		RegistryOps<JsonElement> registryOps = getRegistryOps(registryAccess);
		List<IBookmark> bookmarks = loadJsonBookmarks(ingredientManager, recipeManager, registryOps, codecHelper, bookmarkCodec);
		bookmarkList.setFromConfigFile(bookmarks);
	}

	@Unmodifiable
	private List<IBookmark> loadJsonBookmarks(
		IIngredientManager ingredientManager,
		IRecipeManager recipeManager,
		RegistryOps<JsonElement> registryOps,
		ICodecHelper codecHelper,
		Codec<IBookmark> bookmarkCodec
	) {
		return getPath(jivConfigurationDir)
			.<List<IBookmark>>map(path -> {
				if (!Files.exists(path)) {
					return List.of();
				}

				List<IBookmark> bookmarks;

				try (BufferedReader reader = Files.newBufferedReader(path)) {
					bookmarks = JsonArrayFileHelper.read(
						reader,
						VERSION,
						bookmarkCodec,
						registryOps,
						(element, error) -> {
							LOGGER.error("Encountered an error when loading the bookmark config from file {}\n{}\n{}", path, element, error);
						},
						(element, exception) -> {
							LOGGER.error("Encountered an exception when loading the bookmark config from file {}\n{}", path, element, exception);
						}
					);
					LOGGER.debug("Loaded bookmarks config from file: {}", path);
				} catch (RuntimeException | IOException e) {
					LOGGER.error("Failed to load bookmarks from file {}", path, e);
					bookmarks = new ArrayList<>();
				}

				return bookmarks;
			})
			.orElseGet(List::of);
	}
}
