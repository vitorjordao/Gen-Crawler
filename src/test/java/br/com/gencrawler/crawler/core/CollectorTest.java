package br.com.gencrawler.crawler.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CollectorTest {

	@Test
	public void testAllCrawler() {
		final Collector bwc = new Collector();
		bwc.runItem("span[id^=\"variacaoPreco\"]", "^.*?().*$",
				"https://www.casa.center/mesa-posta/sousplat/sousplat-de-plastico-com-detalhes-na-borda-prata");
		bwc.writeToConsole();
		Assertions.assertTrue(bwc.getItems().size() > 0);
	}

	@Test
	public void errorOnURL() {
		final Collector bwc = new Collector();
		Assertions.assertThrows(RuntimeException.class, () -> {
			bwc.runItem("div[id^=\"conteudo-principal\"]", "^.*?().*$",
					"");
		});
	}

}
