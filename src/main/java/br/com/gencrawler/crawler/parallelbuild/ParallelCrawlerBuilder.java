package br.com.gencrawler.crawler.parallelbuild;

import java.util.List;

import br.com.gencrawler.crawler.core.Crawler;

public interface ParallelCrawlerBuilder {
	
	ParallelCrawlerBuilder addAllUrl(final List<String> pages);
	
	ParallelCrawlerBuilder addUrl(final String page);

	ParallelCrawlerBuilder addAllFinder(final List<String> products);

	ParallelCrawlerBuilder addFinder(final String product);

	ParallelCrawlerBuilder addAllMatchs(final List<String> matchs);

	ParallelCrawlerBuilder addMatch(final String match);

	void verify();

	<T extends Crawler> List<T> build();

}
