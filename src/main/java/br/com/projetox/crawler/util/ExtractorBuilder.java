package br.com.projetox.crawler.util;

import br.com.projetox.crawler.core.Extractor;

public class ExtractorBuilder implements CrawlerBuilder {
	private final Extractor ex;

	private String initialPage;
	private String paginator;
	private String finderProduct;
	private String match;

	public ExtractorBuilder() {
		this.ex = new Extractor();
	}

	public ExtractorBuilder setInitial(final String page) {
		this.initialPage = page;
		return this;
	}

	public ExtractorBuilder setPaginator(final String paginator) {
		this.paginator = paginator;
		return this;
	}

	@Override
	public ExtractorBuilder setFinder(final String product) {
		this.finderProduct = product;
		return this;
	}

	@Override
	public ExtractorBuilder setMatch(final String match) {
		this.match = match;
		return this;
	}

	@Override
	public void verify() {
		if (this.initialPage == null || this.paginator == null || this.finderProduct == null || this.match == null)
			throw new RuntimeException("Peat all data");
	}

	@Override
	public Extractor build() {
		verify();
		this.ex.runPagesLinks(this.initialPage, this.paginator);
		this.ex.runItems(this.finderProduct, this.match);
		return this.ex;
	}

}
