package br.com.gencrawler.crawler.core;

public interface Crawler extends Runnable {

	void runItem();

	@Deprecated
	void runItem(String... filds);

}
