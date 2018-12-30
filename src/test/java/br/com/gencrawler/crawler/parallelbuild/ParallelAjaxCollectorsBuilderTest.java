package br.com.gencrawler.crawler.parallelbuild;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import br.com.gencrawler.crawler.core.AjaxCollector;

public class ParallelAjaxCollectorsBuilderTest {
	
	@BeforeAll
	public static void systemVariable() {
		System.setProperty("webdriver.gecko.driver", /*Here add your path -> */"/home/vitor/path/geckodriver");
	}
	
	@Test
	public void buildCrawler() {
		final ParallelAjaxCollectorsBuilder pcb = new ParallelAjaxCollectorsBuilder();
		final List<String> initialPageLinks = new ArrayList<>();
		final List<String> findersTags = new ArrayList<>();
		final List<String> matchs = new ArrayList<>();
		
		initialPageLinks.add("https://www.casa.center/prato-de-sobremesa-com-estampa-de-flores-e-borda-bambu-magnolia/p");
		findersTags.add("bf-price__best");
		matchs.add("class");

		initialPageLinks.add("https://www.casa.center/prato-de-sobremesa-com-estampa-de-flores-e-borda-bambu-magnolia/p");
		findersTags.add("bf-price__best");
		matchs.add("class");
		
		initialPageLinks.add("https://www.casa.center/prato-de-sobremesa-com-estampa-de-flores-e-borda-bambu-magnolia/p");
		findersTags.add("bf-price__best");
		matchs.add("class");
		
		final List<AjaxCollector> crawlers = pcb
				.addAllUrl(initialPageLinks)
				.addAllFinder(findersTags)
				.addAllMatchs(matchs).build();
		Assertions.assertTrue(crawlers.size() > 0);
		Assertions.assertTrue(crawlers.get(0).getItems().size() > 0);
	}

	@Test
	public void errorCrawlerBuild() {
		final ParallelAjaxCollectorsBuilder pcb = new ParallelAjaxCollectorsBuilder();
		
		final List<String> initialPageLinks = new ArrayList<>();
		final List<String> findersTags = new ArrayList<>();
		final List<String> matchs = new ArrayList<>();
		
		initialPageLinks.add("https://www.casa.center/prato-de-sobremesa-com-estampa-de-flores-e-borda-bambu-magnolia/p");
		findersTags.add("bf-price__best");
		matchs.add("class");

		initialPageLinks.add("https://www.casa.center/prato-de-sobremesa-com-estampa-de-flores-e-borda-bambu-magnolia/p");
		matchs.add("class");
		
		initialPageLinks.add("https://www.casa.center/prato-de-sobremesa-com-estampa-de-flores-e-borda-bambu-magnolia/p");
		findersTags.add("bf-price__best");
		matchs.add("class");
		
		Assertions.assertThrows(RuntimeException.class,
				() -> pcb.addAllUrl(initialPageLinks)
				.addAllFinder(findersTags)
				.addAllMatchs(matchs).build());
	}
}
