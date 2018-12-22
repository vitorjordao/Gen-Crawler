package br.com.gencrawler.crawler.core;

public interface Crawler extends Runnable {

	void runItem();

	void runItem(String... filds);

}
