package br.com.gencrawler.crawler.build;

import br.com.gencrawler.crawler.core.SimpleExtractor;

public class SimpleExtractorBuilder implements CrawlerBuilder {
	private final SimpleExtractor ex;

	private String initialPage;
	private String paginator;
	private String finderProduct;
	private String match;

	public SimpleExtractorBuilder() {
		this.ex = new SimpleExtractor();
	}

	public SimpleExtractorBuilder setInitial(final String page) {
		this.initialPage = page;
		return this;
	}

	public SimpleExtractorBuilder setPaginator(final String paginator) {
		this.paginator = paginator;
		return this;
	}

	@Override
	public SimpleExtractorBuilder setFinder(final String product) {
		this.finderProduct = product;
		return this;
	}

	@Override
	public SimpleExtractorBuilder setMatch(final String match) {
		this.match = match;
		return this;
	}

	@Override
	public void verify() {
		if (this.initialPage == null || this.paginator == null || this.finderProduct == null || this.match == null)
			throw new RuntimeException("Peat all data");
	}

	@Override
	public SimpleExtractor build() {
		verify();
		this.ex.runPagesLinks(this.initialPage, this.paginator);
		this.ex.runItem(this.finderProduct, this.match, this.initialPage);
		return this.ex;
	}

}
