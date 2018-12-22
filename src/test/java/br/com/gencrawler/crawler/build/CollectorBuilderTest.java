package br.com.gencrawler.crawler.build;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import br.com.gencrawler.crawler.core.Collector;

public class CollectorBuilderTest {
	@Test
	public void buildCrawler() {
		final CollectorBuilder cb = new CollectorBuilder();
		final Collector crawler = cb
				.setInitial("https://www.casa.center/prato-de-sobremesa-com-estampa-de-flores-e-borda-bambu-magnolia/p")
				.setFinder("strong[class^=\"skuPrice\"]")
				.setMatch("^.*?().*$").build();
		Assertions.assertTrue(crawler.getItems().size() > 0);
	}

	@Test
	public void errorCrawlerBuild() {
		final CollectorBuilder cb = new CollectorBuilder();
		Assertions.assertThrows(RuntimeException.class,
				() -> cb.setInitial("https://www.casa.center/prato-de-sobremesa-com-estampa-de-flores-e-borda-bambu-magnolia/p")
						.setMatch("^.*?(R$).*$").build());
	}
}
