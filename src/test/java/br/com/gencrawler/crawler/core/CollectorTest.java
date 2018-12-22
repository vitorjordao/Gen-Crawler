package br.com.gencrawler.crawler.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CollectorTest {

	@Test
	public void testAllCrawler() {
		final Collector bwc = new Collector();
		bwc.runItem("strong[class^=\"skuPrice\"]", "^.*?().*$",
				"https://www.casa.center/prato-de-sobremesa-com-estampa-de-flores-e-borda-bambu-magnolia/p");
		bwc.writeToConsole();
		
		Assertions.assertTrue(bwc.getItems().size() > 0);
	}

	@Test
	public void errorOnURL() {
		final Collector bwc = new Collector();
		Assertions.assertThrows(RuntimeException.class, () -> {
			bwc.runItem("strong[class^=\"skuPrice\"]", "^.*?().*$",
					"");
		});
	}

}
