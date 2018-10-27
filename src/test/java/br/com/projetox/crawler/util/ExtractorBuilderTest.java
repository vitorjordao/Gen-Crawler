package br.com.projetox.crawler.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import br.com.projetox.crawler.core.Extractor;

public class ExtractorBuilderTest {
	@Test
	public void buildCrawler() {
		final ExtractorBuilder cb = new ExtractorBuilder();
		final Extractor crawler = cb
				.setInitial("https://www.casa.center/loja/catalogo.php?loja=577838&categoria=132&pg=1")
				.setPaginator("a[href^=\"https://www.casa.center/loja/catalogo.php?loja=577838&categoria=132&pg=\"]")
				.setFinder("ul li div[class^=\"product\"] a[data-tray-tst^=\"vitrine_produto_link_imagem\"]")
				.setMatch("^.*?(R$).*$").build();
		Assertions.assertTrue(crawler.getLinks().size() > 0);
		Assertions.assertTrue(crawler.getItems().size() > 0);
	}

	@Test
	public void errorCrawlerBuild() {
		final ExtractorBuilder cb = new ExtractorBuilder();
		Assertions.assertThrows(RuntimeException.class,
				() -> cb.setInitial("https://www.casa.center/loja/catalogo.php?loja=577838&categoria=132&pg=1")
						.setPaginator(
								"a[href^=\"https://www.casa.center/loja/catalogo.php?loja=577838&categoria=132&pg=\"]")
						.setMatch("^.*?(R$).*$").build());
	}
}
