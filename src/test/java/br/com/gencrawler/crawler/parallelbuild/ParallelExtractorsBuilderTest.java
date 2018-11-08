package br.com.gencrawler.crawler.parallelbuild;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import br.com.gencrawler.crawler.core.Extractor;

public class ParallelExtractorsBuilderTest {

	@Test
	public void buildCrawler() {
		final ParallelExtractorsBuilder pcb = new ParallelExtractorsBuilder();
		final List<String> initialPageLinks = new ArrayList<>();
		final List<String> paginatorsLinks = new ArrayList<>();
		final List<String> findersTags = new ArrayList<>();
		final List<String> matchs = new ArrayList<>();

		initialPageLinks.add("https://www.casa.center/loja/catalogo.php?loja=577838&categoria=132&pg=1");
		paginatorsLinks.add("a[href^=\"https://www.casa.center/loja/catalogo.php?loja=577838&categoria=132&pg=\"]");
		findersTags.add("ul li div[class^=\"product\"] a[data-tray-tst^=\"vitrine_produto_link_imagem\"]");
		matchs.add("^.*?(R$).*$");

		initialPageLinks.add("https://www.casa.center/loja/catalogo.php?loja=577838&categoria=132&pg=1");
		paginatorsLinks.add("a[href^=\"https://www.casa.center/loja/catalogo.php?loja=577838&categoria=132&pg=\"]");
		findersTags.add("ul li div[class^=\"product\"] a[data-tray-tst^=\"vitrine_produto_link_imagem\"]");
		matchs.add("^.*?(R$).*$");

		initialPageLinks.add("https://www.casa.center/loja/catalogo.php?loja=577838&categoria=132&pg=1");
		paginatorsLinks.add("a[href^=\"https://www.casa.center/loja/catalogo.php?loja=577838&categoria=132&pg=\"]");
		findersTags.add("ul li div[class^=\"product\"] a[data-tray-tst^=\"vitrine_produto_link_imagem\"]");
		matchs.add("^.*?(R$).*$");

		final List<Extractor> crawlers = pcb.addAllUrl(initialPageLinks).addAllPaginators(paginatorsLinks)
				.addAllFinder(findersTags).addAllMatchs(matchs).build();
		Assertions.assertTrue(crawlers.size() > 0);
		Assertions.assertTrue(crawlers.get(0).getLinks().size() > 0);
		Assertions.assertTrue(crawlers.get(0).getItems().size() > 0);
	}

	@Test
	public void errorCrawlerBuild() {
		final ParallelExtractorsBuilder pcb = new ParallelExtractorsBuilder();

		final List<String> initialPageLinks = new ArrayList<>();
		final List<String> paginatorsLinks = new ArrayList<>();
		final List<String> findersTags = new ArrayList<>();
		final List<String> matchs = new ArrayList<>();

		initialPageLinks.add("https://www.casa.center/loja/catalogo.php?loja=577838&categoria=132&pg=1");
		paginatorsLinks.add("a[href^=\"https://www.casa.center/loja/catalogo.php?loja=577838&categoria=132&pg=\"]");
		findersTags.add("ul li div[class^=\"product\"] a[data-tray-tst^=\"vitrine_produto_link_imagem\"]");
		matchs.add("^.*?(R$).*$");

		initialPageLinks.add("https://www.casa.center/loja/catalogo.php?loja=577838&categoria=132&pg=1");
		paginatorsLinks.add("a[href^=\"https://www.casa.center/loja/catalogo.php?loja=577838&categoria=132&pg=\"]");
		matchs.add("^.*?(R$).*$");

		initialPageLinks.add("https://www.casa.center/loja/catalogo.php?loja=577838&categoria=132&pg=1");
		paginatorsLinks.add("a[href^=\"https://www.casa.center/loja/catalogo.php?loja=577838&categoria=132&pg=\"]");
		findersTags.add("ul li div[class^=\"product\"] a[data-tray-tst^=\"vitrine_produto_link_imagem\"]");
		matchs.add("^.*?(R$).*$");

		Assertions.assertThrows(RuntimeException.class, () -> pcb.addAllUrl(initialPageLinks)
				.addAllPaginators(paginatorsLinks).addAllFinder(findersTags).addAllMatchs(matchs).build());
	}
}
