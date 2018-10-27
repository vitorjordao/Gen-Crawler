package br.com.projetox.crawler.util;

import br.com.projetox.crawler.core.Crawler;

public interface CrawlerBuilder {
	
	CrawlerBuilder setFinder(final String product);
	
	CrawlerBuilder setMatch(final String match);
	
	void verify();

	Crawler build();
}
