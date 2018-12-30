package br.com.gencrawler.crawler.build;

import br.com.gencrawler.crawler.core.AjaxCollector;

public class AjaxCollectorBuilder implements CollectorBuilder {
	private final AjaxCollector ex;

	private String initialPage;
	private String finderProduct;
	private String match;

	public AjaxCollectorBuilder() {
		this.ex = new AjaxCollector();
	} 

	@Override
	public AjaxCollectorBuilder setInitial(final String page) {
		this.initialPage = page;
		return this;
	}

	@Override
	public AjaxCollectorBuilder setFinder(final String product) {
		this.finderProduct = product;
		return this;
	}

	@Override
	public AjaxCollectorBuilder setMatch(final String match) {
		this.match = match;
		return this;
	}

	@Override
	public void verify() {
		if (this.initialPage == null || this.finderProduct == null || this.match == null)
			throw new RuntimeException("Peat all data");
	}

	@Override
	public AjaxCollector build() {
		verify();
		this.ex.runItem(this.finderProduct, this.match, this.initialPage);
		return this.ex;
	}

}
