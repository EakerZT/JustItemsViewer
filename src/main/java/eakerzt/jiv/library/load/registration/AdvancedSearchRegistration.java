package eakerzt.jiv.library.load.registration;

import eakerzt.jiv.api.registration.IAdvancedSearchRegistration;
import eakerzt.jiv.api.search.ISearchStorageBuilder;
import eakerzt.jiv.api.search.ISearchStorageBuilderFactory;
import eakerzt.jiv.api.search.ISearchStorageFactory;
import eakerzt.jiv.common.search.BakedSubstringIndexBuilder;
import eakerzt.jiv.common.search.SearchStorageBuilderAdapter;
import eakerzt.jiv.common.util.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AdvancedSearchRegistration implements IAdvancedSearchRegistration {
	private static final Logger LOGGER = LogManager.getLogger();

	@Nullable
	private ISearchStorageBuilderFactory searchStorageBuilderFactoryOverride;
	private final ISearchStorageBuilderFactory defaultSearchStorageBuilderFactory;

	public AdvancedSearchRegistration() {
		this(BakedSubstringIndexBuilder::new);
	}

	public AdvancedSearchRegistration(ISearchStorageBuilderFactory defaultSearchStorageBuilderFactory) {
		ErrorUtil.checkNotNull(defaultSearchStorageBuilderFactory, "defaultSearchStorageBuilderFactory");
		this.defaultSearchStorageBuilderFactory = defaultSearchStorageBuilderFactory;
	}

	@Override
	public ISearchStorageBuilderFactory getDefaultSearchStorageBuilderFactory() {
		return defaultSearchStorageBuilderFactory;
	}

	@Override
	public void replaceSearchStorage(ISearchStorageFactory searchStorageFactory) {
		ErrorUtil.checkNotNull(searchStorageFactory, "searchStorageFactory");

		LOGGER.info("Replaced search storage factory: {}", searchStorageFactory);
		this.searchStorageBuilderFactoryOverride = new ISearchStorageBuilderFactory() {
			@Override
			public <T> ISearchStorageBuilder<T> create() {
				return new SearchStorageBuilderAdapter<>(searchStorageFactory.createSearchStorage());
			}
		};
	}

	@Override
	public void replaceSearchStorage(ISearchStorageBuilderFactory searchStorageBuilderFactory) {
		ErrorUtil.checkNotNull(searchStorageBuilderFactory, "searchStorageBuilderFactory");

		LOGGER.info("Replaced search storage factory: {}", searchStorageBuilderFactory);
		this.searchStorageBuilderFactoryOverride = searchStorageBuilderFactory;
	}

	public Optional<ISearchStorageBuilderFactory> getSearchStorageBuilderFactoryOverride() {
		return Optional.ofNullable(searchStorageBuilderFactoryOverride);
	}
}
