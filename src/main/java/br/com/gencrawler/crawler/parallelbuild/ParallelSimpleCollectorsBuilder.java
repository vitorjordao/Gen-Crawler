package br.com.gencrawler.crawler.parallelbuild;

import java.util.ArrayList;
import java.util.List;

import br.com.gencrawler.crawler.core.Crawler;
import br.com.gencrawler.crawler.core.SimpleCollector;

public class ParallelSimpleCollectorsBuilder implements ParallelCollectorsBuilder {

	private final List<SimpleCollector> crawlers = new ArrayList<>();
	private final List<String> url = new ArrayList<>();
	private final List<String> finderProducts = new ArrayList<>();
	private final List<String> matchs = new ArrayList<>();

	@Override
	public ParallelSimpleCollectorsBuilder addAllUrl(final List<String> pages) {
		this.url.addAll(pages);
		return this;
	}

	@Override
	public ParallelSimpleCollectorsBuilder addUrl(final String page) {
		this.url.add(page);
		return this;
	}

	@Override
	public ParallelSimpleCollectorsBuilder addAllFinder(final List<String> products) {
		this.finderProducts.addAll(products);
		return this;
	}

	@Override
	public ParallelSimpleCollectorsBuilder addFinder(final String product) {
		this.finderProducts.add(product);
		return this;
	}

	@Override
	public ParallelSimpleCollectorsBuilder addAllMatchs(final List<String> matchs) {
		this.matchs.addAll(matchs);
		return this;
	}

	@Override
	public ParallelSimpleCollectorsBuilder addMatch(final String match) {
		this.matchs.add(match);
		return this;
	}

	@Override
	public void verify() {
		if (this.url.isEmpty() || this.finderProducts.isEmpty() || this.matchs.isEmpty())
			throw new RuntimeException("Peat all data");
		else if (this.url.size() != this.finderProducts.size() || this.url.size() != this.matchs.size())
			throw new RuntimeException("The amount of data is not known");
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Crawler> List<T> build() {
		verify();
		Thread thread = null;
		for (int i = 0; i < this.url.size(); i++) {
			this.crawlers.add(new SimpleCollector(this.url.get(i), this.finderProducts.get(i), this.matchs.get(i)));
			thread = new Thread(this.crawlers.get(i), i + "-");
			thread.run();
		}
		synchronized (thread) {
			try {
				thread.join();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
			return (List<T>) this.crawlers;
		}
	}

}