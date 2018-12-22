package br.com.gencrawler.crawler.parallelbuild;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import br.com.gencrawler.crawler.core.Collector;

public class ParallelCollectorsBuilderTest {
	
	@Test
	public void buildCrawler() {
		final ParallelCollectorsBuilder pcb = new ParallelCollectorsBuilder();
		final List<String> initialPageLinks = new ArrayList<>();
		final List<String> findersTags = new ArrayList<>();
		final List<String> matchs = new ArrayList<>();
		
		initialPageLinks.add("https://www.casa.center/prato-de-sobremesa-com-estampa-de-flores-e-borda-bambu-magnolia/p");
		findersTags.add("strong[class^=\"skuPrice\"]");
		matchs.add("^.*?().*$");

		initialPageLinks.add("https://www.casa.center/prato-de-sobremesa-com-estampa-de-flores-e-borda-bambu-magnolia/p");
		findersTags.add("strong[class^=\"skuPrice\"]");
		matchs.add("^.*?().*$");
		
		initialPageLinks.add("https://www.casa.center/prato-de-sobremesa-com-estampa-de-flores-e-borda-bambu-magnolia/p");
		findersTags.add("strong[class^=\"skuPrice\"]");
		matchs.add("^.*?().*$");
		
		final List<Collector> crawlers = pcb
				.addAllUrl(initialPageLinks)
				.addAllFinder(findersTags)
				.addAllMatchs(matchs).build();
		Assertions.assertTrue(crawlers.size() > 0);
		Assertions.assertTrue(crawlers.get(0).getItems().size() > 0);
	}

	@Test
	public void errorCrawlerBuild() {
		final ParallelCollectorsBuilder pcb = new ParallelCollectorsBuilder();
		
		final List<String> initialPageLinks = new ArrayList<>();
		final List<String> findersTags = new ArrayList<>();
		final List<String> matchs = new ArrayList<>();
		
		initialPageLinks.add("https://www.casa.center/prato-de-sobremesa-com-estampa-de-flores-e-borda-bambu-magnolia/p");
		findersTags.add("strong[class^=\"skuPrice\"]");
		matchs.add("^.*?().*$");

		initialPageLinks.add("https://www.casa.center/prato-de-sobremesa-com-estampa-de-flores-e-borda-bambu-magnolia/p");
		matchs.add("^.*?().*$");
		
		initialPageLinks.add("https://www.casa.center/prato-de-sobremesa-com-estampa-de-flores-e-borda-bambu-magnolia/p");
		findersTags.add("strong[class^=\"skuPrice\"]");
		matchs.add("^.*?().*$");
		
		Assertions.assertThrows(RuntimeException.class,
				() -> pcb.addAllUrl(initialPageLinks)
				.addAllFinder(findersTags)
				.addAllMatchs(matchs).build());
	}
}
