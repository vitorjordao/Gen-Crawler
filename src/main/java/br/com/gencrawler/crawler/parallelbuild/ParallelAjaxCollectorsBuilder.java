package br.com.gencrawler.crawler.parallelbuild;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import br.com.gencrawler.crawler.core.AjaxCollector;
import br.com.gencrawler.crawler.core.Crawler;

public class ParallelAjaxCollectorsBuilder implements ParallelCollectorsBuilder {

	private final List<AjaxCollector> crawlers = new ArrayList<>();
	private final List<String> url = new ArrayList<>();
	private final List<String> finderProducts = new ArrayList<>();
	private final List<String> matchs = new ArrayList<>();

	@Override
	public ParallelAjaxCollectorsBuilder addAllUrl(final List<String> pages) {
		this.url.addAll(pages);
		return this;
	}

	@Override
	public ParallelAjaxCollectorsBuilder addUrl(final String page) {
		this.url.add(page);
		return this;
	}

	@Override
	public ParallelAjaxCollectorsBuilder addAllFinder(final List<String> products) {
		this.finderProducts.addAll(products);
		return this;
	}

	@Override
	public ParallelAjaxCollectorsBuilder addFinder(final String product) {
		this.finderProducts.add(product);
		return this;
	}

	@Override
	public ParallelAjaxCollectorsBuilder addAllMatchs(final List<String> matchs) {
		this.matchs.addAll(matchs);
		return this;
	}

	@Override
	public ParallelAjaxCollectorsBuilder addMatch(final String match) {
		this.matchs.add(match);
		return this;
	}

	@Override
	public void verify() {
		if (this.url.isEmpty() || this.finderProducts.isEmpty() || this.matchs.isEmpty())
			throw new RuntimeException("Peat all data");
		else if (this.url.size() != this.finderProducts.size() || this.url.size() != this.matchs.size())
			throw new RuntimeException("The amount of data is not known");
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Crawler> List<T> build() {
		verify();
		final ExecutorService executor = Executors.newCachedThreadPool();
		for (int i = 0; i < this.url.size(); i++) {
			this.crawlers.add(new AjaxCollector(this.url.get(i), this.finderProducts.get(i), this.matchs.get(i)));
			executor.execute(this.crawlers.get(i));
		}

		executor.shutdown();
		
		try{
			executor.awaitTermination(this.crawlers.size() * 10, 
				TimeUnit.SECONDS);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		return (List<T>) this.crawlers;
	}
}