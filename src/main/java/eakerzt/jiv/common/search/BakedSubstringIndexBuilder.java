package eakerzt.jiv.common.search;

import eakerzt.jiv.api.search.ISearchStorage;
import eakerzt.jiv.api.search.ISearchStorageBuilder;
import eakerzt.jiv.common.search.internal.bakedsubstring.BakedSubstringIndex;

public class BakedSubstringIndexBuilder<T> implements ISearchStorageBuilder<T> {
	private final BakedSubstringIndex.Builder<T> builder = BakedSubstringIndex.builder();

	@Override
	public void put(String key, T value) {
		builder.put(key, value);
	}

	@Override
	public ISearchStorage<T> build() {
		BakedSubstringIndex<T> bakedStorage = builder.build();
		return new BakedSubstringIndexSearchStorage<>(bakedStorage);
	}
}
