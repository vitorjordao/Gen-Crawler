package br.com.gencrawler.crawler.core;

import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExtractorTest {

	@Test
	public void testGetRunPagesLinks() {
		final Extractor bwc = new Extractor();
		bwc.runPagesLinks("https://www.casa.center/loja/catalogo.php?loja=577838&categoria=132&pg=1",
				"a[href^=\"https://www.casa.center/loja/catalogo.php?loja=577838&categoria=132&pg=\"]");
		final Set<String> links = bwc.getLinks();
		Assertions.assertTrue(links.size() > 0);
	}

	@Test
	public void testAllCrawler() {
		final Extractor bwc = new Extractor();
		bwc.runPagesLinks("https://www.casa.center/loja/catalogo.php?loja=577838&categoria=132&pg=1",
				"a[href^=\"https://www.casa.center/loja/catalogo.php?loja=577838&categoria=132&pg=\"]");
		bwc.runItem("ul li div[class^=\"product\"] a[data-tray-tst^=\"vitrine_produto_link_imagem\"]",
				"^.*?(R$).*$", "https://www.casa.center/loja/catalogo.php?loja=577838&categoria=132&pg=1");
		Assertions.assertTrue(bwc.getItems().size() > 0);
	}
	
	@Test
	public void errorOnURL() {
		final Extractor bwc = new Extractor();
		Assertions.assertThrows(RuntimeException.class,
				() -> {
					bwc.runPagesLinks("not work url",
							"a[href^=\"https://www.casa.center/loja/catalogo.php?loja=577838&categoria=132&pg=\"]");
				});
	}

}
