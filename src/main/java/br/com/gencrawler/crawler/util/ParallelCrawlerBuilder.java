package br.com.gencrawler.crawler.util;

import java.util.List;

import br.com.gencrawler.crawler.core.Crawler;

public interface ParallelCrawlerBuilder {

	ParallelExtractorsBuilder addAllFinder(final List<String> products);

	ParallelExtractorsBuilder addFinder(final String product);

	ParallelExtractorsBuilder addAllMatchs(final List<String> matchs);

	ParallelExtractorsBuilder addMatch(final String match);

	void verify();

	<T extends Crawler> List<T> build();

}
