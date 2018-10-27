package br.com.projetox.crawler.core;

import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExtractorTest {

//	public static void main(final String[] args) {
//		final long tempoInicio = System.currentTimeMillis();
//		final Extractor bwc = new Extractor();
//		bwc.runPagesLinks("https://www.casa.center/loja/catalogo.php?loja=577838&categoria=132&pg=1", "a[href^=\"https://www.casa.center/loja/catalogo.php?loja=577838&categoria=132&pg=\"]");
//		bwc.runProducts("ul li div[class^=\"product\"] a[data-tray-tst^=\"vitrine_produto_link_imagem\"]", "^.*?(R$).*$");
//		bwc.writeToConsole();
//		System.out.println("\n\nTempo Total: " + (System.currentTimeMillis() - tempoInicio));
//	}

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
		bwc.runItems("ul li div[class^=\"product\"] a[data-tray-tst^=\"vitrine_produto_link_imagem\"]",
				"^.*?(R$).*$");
		Assertions.assertTrue(bwc.getItems().size() > 0);
	}
	
	@Test
	public void errorOnURL() {
		final Extractor bwc = new Extractor();
		Assertions.assertThrows(RuntimeException.class,
				() -> {
					bwc.runPagesLinks("falsa url",
							"a[href^=\"https://www.casa.center/loja/catalogo.php?loja=577838&categoria=132&pg=\"]");
				});
	}

}
