package br.com.projetox.crawler.core;

public interface Crawler extends Runnable {
	
	void runItems();
	
	void runItems(final String linkFinded, final String match);
	
}
