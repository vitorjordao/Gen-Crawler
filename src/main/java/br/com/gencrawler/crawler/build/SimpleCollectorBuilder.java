package br.com.gencrawler.crawler.build;

import br.com.gencrawler.crawler.core.SimpleCollector;

public class SimpleCollectorBuilder implements CollectorBuilder {
	private final SimpleCollector collector;

	private String initialPage;
	private String finderProduct;
	private String match;

	public SimpleCollectorBuilder() {
		this.collector = new SimpleCollector();
	} 

	@Override
	public SimpleCollectorBuilder setInitial(final String page) {
		this.initialPage = page;
		return this;
	}

	@Override
	public SimpleCollectorBuilder setFinder(final String product) {
		this.finderProduct = product;
		return this;
	}

	@Override
	public SimpleCollectorBuilder setMatch(final String match) {
		this.match = match;
		return this;
	}

	@Override
	public void verify() {
		if (this.initialPage == null || this.finderProduct == null || this.match == null
			|| this.initialPage.equals("") || this.finderProduct.equals("") || this.match.equals(""))
			throw new RuntimeException("Peat all data");
	}

	@Override
	public SimpleCollector build() {
		verify();
		this.collector.runItem(this.finderProduct, this.match, this.initialPage);
		return this.collector;
	}

}
