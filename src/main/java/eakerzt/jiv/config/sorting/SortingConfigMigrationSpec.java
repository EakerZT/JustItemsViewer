package eakerzt.jiv.config.sorting;

import eakerzt.jiv.config.api.migration.ISortingConfigMigrator;
import eakerzt.jiv.config.migration.LegacyMigrationPaths;
import eakerzt.jiv.config.util.ErrorUtil;

import java.nio.file.Path;
import java.util.List;

record SortingConfigMigrationSpec<T>(
	List<Path> legacyPaths,
	ISortingConfigMigrator<T> migrator
) {
	SortingConfigMigrationSpec {
		legacyPaths = LegacyMigrationPaths.normalize(legacyPaths);
		migrator = ErrorUtil.checkNotNull(migrator, "migrator");
	}
}
