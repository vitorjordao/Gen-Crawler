package br.com.gencrawler.crawler.build;

import br.com.gencrawler.crawler.core.SimpleExtractor;

public class SimpleExtractorBuilder implements CrawlerBuilder {
	private final SimpleExtractor extractor;

	private String initialPage;
	private String paginator;
	private String finderProduct;
	private String match;

	public SimpleExtractorBuilder() {
		this.extractor = new SimpleExtractor();
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
		if (this.initialPage == null || this.paginator == null || this.finderProduct == null || this.match == null
			|| this.initialPage.equals("") || this.paginator.equals("") || this.finderProduct.equals("") || this.match.equals(""))
			throw new RuntimeException("Peat all data");
	}

	@Override
	public SimpleExtractor build() {
		verify();
		this.extractor.runPagesLinks(this.initialPage, this.paginator);
		this.extractor.runItem(this.finderProduct, this.match, this.initialPage);
		return this.extractor;
	}

}
