package br.com.gencrawler.crawler.util;

import java.util.ArrayList;
import java.util.List;

import br.com.gencrawler.crawler.core.Crawler;
import br.com.gencrawler.crawler.core.Extractor;

public class ParallelExtractorsBuilder implements ParallelCrawlerBuilder {

	private final List<Extractor> crawlers = new ArrayList<>();
	private final List<String> initialPages = new ArrayList<>();
	private final List<String> paginators = new ArrayList<>();
	private final List<String> finderProducts = new ArrayList<>();
	private final List<String> matchs = new ArrayList<>();

	public ParallelExtractorsBuilder addAllInitial(final List<String> pages) {
		this.initialPages.addAll(pages);
		return this;
	}

	public ParallelExtractorsBuilder addInitial(final String page) {
		this.initialPages.add(page);
		return this;
	}

	public ParallelExtractorsBuilder addAllPaginators(final List<String> paginators) {
		this.paginators.addAll(paginators);
		return this;
	}

	public ParallelExtractorsBuilder addPaginator(final String paginator) {
		this.paginators.add(paginator);
		return this;
	}

	@Override
	public ParallelExtractorsBuilder addAllFinder(final List<String> products) {
		this.finderProducts.addAll(products);
		return this;
	}

	@Override
	public ParallelExtractorsBuilder addFinder(final String product) {
		this.finderProducts.add(product);
		return this;
	}

	@Override
	public ParallelExtractorsBuilder addAllMatchs(final List<String> matchs) {
		this.matchs.addAll(matchs);
		return this;
	}

	@Override
	public ParallelExtractorsBuilder addMatch(final String match) {
		this.matchs.add(match);
		return this;
	}

	@Override
	public void verify() {
		if (this.initialPages.isEmpty() || this.paginators.isEmpty() || this.finderProducts.isEmpty()
				|| this.matchs.isEmpty())
			throw new RuntimeException("Peat all data");
		else if (this.initialPages.size() != this.paginators.size()
				|| this.initialPages.size() != this.finderProducts.size()
				|| this.initialPages.size() != this.matchs.size())
			throw new RuntimeException("The amount of data is not known");
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Crawler> List<T> build() {
		verify();
		for (int i = 0; i < this.initialPages.size(); i++) {
			this.crawlers.add(new Extractor(this.initialPages.get(i), this.paginators.get(i),
					this.finderProducts.get(i), this.matchs.get(i)));
			new Thread(this.crawlers.get(i), i + "-").run();

		}
		return (List<T>) this.crawlers;
	}

}