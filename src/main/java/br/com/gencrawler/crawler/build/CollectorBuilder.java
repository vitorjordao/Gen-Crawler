package br.com.gencrawler.crawler.build;

import br.com.gencrawler.crawler.core.Collector;

public class CollectorBuilder implements CrawlerBuilder {
	private final Collector ex;

	private String initialPage;
	private String finderProduct;
	private String match;

	public CollectorBuilder() {
		this.ex = new Collector();
	}

	public CollectorBuilder setInitial(final String page) {
		this.initialPage = page;
		return this;
	}

	@Override
	public CollectorBuilder setFinder(final String product) {
		this.finderProduct = product;
		return this;
	}

	@Override
	public CollectorBuilder setMatch(final String match) {
		this.match = match;
		return this;
	}

	@Override
	public void verify() {
		if (this.initialPage == null || this.finderProduct == null || this.match == null)
			throw new RuntimeException("Peat all data");
	}

	@Override
	public Collector build() {
		verify();
		this.ex.runItem(this.finderProduct, this.match, this.initialPage);
		return this.ex;
	}

}
