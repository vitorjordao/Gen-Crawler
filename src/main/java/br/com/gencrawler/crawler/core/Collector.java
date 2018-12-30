package br.com.gencrawler.crawler.core;

import java.util.List;

public interface Collector extends Crawler{
	
	List<String> getItems();
}
