package br.com.gencrawler.crawler.build;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import br.com.gencrawler.crawler.core.SimpleCollector;

public class SimpleCollectorBuilderTest {
	@Test
	public void buildCrawler() {
		final SimpleCollectorBuilder cb = new SimpleCollectorBuilder();
		final SimpleCollector crawler = cb
				.setInitial("https://www.casa.center/prato-de-sobremesa-com-estampa-de-flores-e-borda-bambu-magnolia/p")
				.setFinder("strong[class^=\"skuPrice\"]")
				.setMatch("^.*?().*$").build();
		Assertions.assertTrue(crawler.getItems().size() > 0);
	}

	@Test
	public void errorCrawlerBuild() {
		final SimpleCollectorBuilder cb = new SimpleCollectorBuilder();
		Assertions.assertThrows(RuntimeException.class,
				() -> cb.setInitial("https://www.casa.center/prato-de-sobremesa-com-estampa-de-flores-e-borda-bambu-magnolia/p")
						.setMatch("^.*?(R$).*$").build());
	}
}
