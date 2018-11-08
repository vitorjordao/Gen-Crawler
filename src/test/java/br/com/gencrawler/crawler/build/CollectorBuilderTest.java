package br.com.gencrawler.crawler.build;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import br.com.gencrawler.crawler.core.Collector;

public class CollectorBuilderTest {
	@Test
	public void buildCrawler() {
		final CollectorBuilder cb = new CollectorBuilder();
		final Collector crawler = cb
				.setInitial("https://www.casa.center/mesa-posta/sousplat/sousplat-de-plastico-com-detalhes-na-borda-prata")
				.setFinder("span[id^=\"variacaoPreco\"]")
				.setMatch("^.*?().*$").build();
		Assertions.assertTrue(crawler.getItems().size() > 0);
	}

	@Test
	public void errorCrawlerBuild() {
		final CollectorBuilder cb = new CollectorBuilder();
		Assertions.assertThrows(RuntimeException.class,
				() -> cb.setInitial("https://www.casa.center/loja/catalogo.php?loja=577838&categoria=132&pg=1")
						.setMatch("^.*?(R$).*$").build());
	}
}
