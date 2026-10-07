package eakerzt.jiv.common.search;

import eakerzt.jiv.api.search.ISearchStorage;
import eakerzt.jiv.common.search.internal.suffixtree.GeneralizedSuffixTree;

import java.util.Collection;
import java.util.function.Consumer;

public class GeneralizedSuffixTreeSearchStorage<T> implements ISearchStorage<T> {
	private final GeneralizedSuffixTree<T> generalizedSuffixTree = new GeneralizedSuffixTree<>();

	@Override
	public void getSearchResults(String token, Consumer<Collection<T>> resultsConsumer) {
		generalizedSuffixTree.getSearchResults(token, resultsConsumer);
	}

	@Override
	public void getAllElements(Consumer<Collection<T>> resultsConsumer) {
		generalizedSuffixTree.getAllElements(resultsConsumer);
	}

	@Override
	public void put(String key, T value) {
		generalizedSuffixTree.put(key, value);
	}

	@Override
	public String statistics() {
		return generalizedSuffixTree.statistics();
	}
}
