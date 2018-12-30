package br.com.gencrawler.crawler.build;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import br.com.gencrawler.crawler.core.AjaxCollector;

public class AjaxCollectorBuilderTest {

	@BeforeAll
	public static void systemVariable() {
		System.setProperty("webdriver.gecko.driver", /*Here add your path -> */"/home/vitor/path/geckodriver");
	}
	
	@Test
	public void buildCrawler() {
		final AjaxCollectorBuilder cb = new AjaxCollectorBuilder();
		final AjaxCollector crawler = cb
				.setInitial("https://www.casa.center/prato-de-sobremesa-com-estampa-de-flores-e-borda-bambu-magnolia/p")
				.setFinder("bf-price__best")
				.setMatch("class").build();
		Assertions.assertTrue(crawler.getItems().size() > 0);
	}

	@Test
	public void errorCrawlerBuild() {
		final AjaxCollectorBuilder cb = new AjaxCollectorBuilder();
		Assertions.assertThrows(RuntimeException.class,
				() -> cb.setInitial("https://www.casa.center/prato-de-sobremesa-com-estampa-de-flores-e-borda-bambu-magnolia/p")
						.setMatch("^.*?(R$).*$").build());
	}
}
