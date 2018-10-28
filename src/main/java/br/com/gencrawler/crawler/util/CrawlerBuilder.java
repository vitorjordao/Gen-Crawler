package br.com.gencrawler.crawler.util;

import br.com.gencrawler.crawler.core.Crawler;

public interface CrawlerBuilder {
	
	CrawlerBuilder setFinder(final String product);
	
	CrawlerBuilder setMatch(final String match);
	
	void verify();

	Crawler build();
}
