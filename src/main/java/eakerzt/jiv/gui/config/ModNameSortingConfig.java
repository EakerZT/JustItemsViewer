package eakerzt.jiv.gui.config;

import eakerzt.jiv.api.constants.ModIds;
import eakerzt.jiv.common.config.legacy.LegacySortingConfigMigrator;
import eakerzt.jiv.gui.ingredients.IListElementInfo;
import eakerzt.jiv.config.api.IConfigRegistration;
import eakerzt.jiv.config.api.sorting.ISortingConfig;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

public class ModNameSortingConfig {
	private static final String CONFIG_FILE_NAME = "ingredient-list-mod-sort-order.ini";
	private static final boolean ALLOWS_REMOVING_VALUES = false;

	private final ISortingConfig<String> sortingConfig;

	public static ISortingConfig<String> create(IConfigRegistration registration) {
		return registration.createSortingConfig(
			CONFIG_FILE_NAME,
			getDefaultSortOrder(),
			ALLOWS_REMOVING_VALUES
		);
	}

	public static ISortingConfig<String> create(
		IConfigRegistration registration,
		Path jivConfigDirectory,
		@Nullable UUID profileId
	) {
		ISortingConfig<String> sortingConfig = create(registration);
		return LegacySortingConfigMigrator.register(sortingConfig, jivConfigDirectory, profileId, CONFIG_FILE_NAME);
	}

	public ModNameSortingConfig(ISortingConfig<String> sortingConfig) {
		this.sortingConfig = Objects.requireNonNull(sortingConfig);
	}

	public Comparator<IListElementInfo<?>> getComparatorFromMappedValues(Collection<String> modNames) {
		Comparator<String> comparator = sortingConfig.getComparator(modNames);
		return Comparator.comparing(IListElementInfo::getModNameForSorting, comparator);
	}

	public Runnable addChangeListener(Runnable listener) {
		return sortingConfig.addChangeListener(listener);
	}

	private static Comparator<String> getDefaultSortOrder() {
		Comparator<String> minecraftFirst = Comparator.comparing((String s) -> s.equals(ModIds.MINECRAFT_NAME)).reversed();
		Comparator<String> naturalOrder = Comparator.naturalOrder();
		return minecraftFirst.thenComparing(naturalOrder);
	}
}
