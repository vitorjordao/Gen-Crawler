package br.com.gencrawler.crawler.parallelbuild;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.com.gencrawler.crawler.core.Crawler;
import br.com.gencrawler.crawler.core.SimpleExtractor;

public class ParallelSimpleExtractorsBuilder implements ParallelCrawlerBuilder {

	private final List<SimpleExtractor> crawlers = new ArrayList<>();
	private final List<String> url = new ArrayList<>();
	private final List<String> paginators = new ArrayList<>();
	private final List<String> finderProducts = new ArrayList<>();
	private final List<String> matchs = new ArrayList<>();

	@Override
	public ParallelSimpleExtractorsBuilder addAllUrl(final List<String> pages) {
		this.url.addAll(pages);
		return this;
	}

	@Override
	public ParallelSimpleExtractorsBuilder addUrl(final String page) {
		this.url.add(page);
		return this;
	}

	public ParallelSimpleExtractorsBuilder addAllPaginators(final List<String> paginators) {
		this.paginators.addAll(paginators);
		return this;
	}

	public ParallelSimpleExtractorsBuilder addPaginator(final String paginator) {
		this.paginators.add(paginator);
		return this;
	}

	@Override
	public ParallelSimpleExtractorsBuilder addAllFinder(final List<String> products) {
		this.finderProducts.addAll(products);
		return this;
	}

	@Override
	public ParallelSimpleExtractorsBuilder addFinder(final String product) {
		this.finderProducts.add(product);
		return this;
	}

	@Override
	public ParallelSimpleExtractorsBuilder addAllMatchs(final List<String> matchs) {
		this.matchs.addAll(matchs);
		return this;
	}

	@Override
	public ParallelSimpleExtractorsBuilder addMatch(final String match) {
		this.matchs.add(match);
		return this;
	}

	@Override
	public void verify() {
		if (this.url.isEmpty() || this.paginators.isEmpty() || this.finderProducts.isEmpty() || this.matchs.isEmpty())
			throw new RuntimeException("Peat all data");
		else if (this.url.size() != this.paginators.size() || this.url.size() != this.finderProducts.size()
				|| this.url.size() != this.matchs.size())
			throw new RuntimeException("The amount of data is not known");
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Crawler> List<T> build() {
		verify();
		final ExecutorService executor = Executors.newCachedThreadPool();
		for (int i = 0; i < this.url.size(); i++) {
			this.crawlers.add(new SimpleExtractor(this.url.get(i), this.paginators.get(i), this.finderProducts.get(i),
					this.matchs.get(i)));
			executor.submit(this.crawlers.get(i));
		}
		executor.shutdown();
		while(!executor.isTerminated()) {}
		return (List<T>) this.crawlers;
	}
}