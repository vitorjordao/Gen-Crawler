package br.com.gencrawler.crawler.parallelbuild;

import java.util.ArrayList;
import java.util.List;

import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import br.com.gencrawler.crawler.Helper;
import br.com.gencrawler.crawler.core.SimpleCollector;

@Tag("NonAJAX")
public class ParallelSimpleCollectorsBuilderTest {
	
	private Helper helper = new Helper();
	private final String url =
		"https://www.americanas.com.br/produto/130177939/liquidificador-philips-walita-problend-2136-0-com-jarra-de-vidro-branco-2l-e-5-velocidades-800w?chave=prf_hm_0_oh_4_txar_00&hl=lower&pfm_carac=1&pfm_index=3&pfm_page=home&pfm_pos=maintop4&pfm_type=vit_spacey&voltagem=220V";
	private final String finders = "span[class^=\"price__SalesPrice-ej7lo8-2\"]";


	@Test
	public void buildCrawler() {
		final ParallelSimpleCollectorsBuilder pcb = new ParallelSimpleCollectorsBuilder();
		final List<String> initialPageLinks = new ArrayList<>();
		final List<String> findersTags = new ArrayList<>();
		final List<String> matchs = new ArrayList<>();
		
		initialPageLinks.add(url);
		findersTags.add(finders);
		matchs.add(helper.getMatch());

		initialPageLinks.add(url);
		findersTags.add(finders);
		matchs.add(helper.getMatch());
		
		initialPageLinks.add(url);
		findersTags.add(finders);
		matchs.add(helper.getMatch());
		
		final List<SimpleCollector> crawlers = pcb
				.addAllUrl(initialPageLinks)
				.addAllFinder(findersTags)
				.addAllMatchs(matchs).build();
		Assertions.assertTrue(crawlers.size() > 0);
		Assertions.assertTrue(crawlers.get(0).getItems().size() > 0);
	}

	@Test
	public void errorCrawlerBuild() {
		final ParallelSimpleCollectorsBuilder pcb = new ParallelSimpleCollectorsBuilder();
		
		final List<String> initialPageLinks = new ArrayList<>();
		final List<String> findersTags = new ArrayList<>();
		final List<String> matchs = new ArrayList<>();
		
		initialPageLinks.add(url);
		findersTags.add(finders);
		matchs.add(helper.getMatch());

		initialPageLinks.add(url);
		matchs.add(helper.getMatch());
		
		initialPageLinks.add(url);
		findersTags.add(finders);
		matchs.add(helper.getMatch());
		
		Assertions.assertThrows(RuntimeException.class,
				() -> pcb.addAllUrl(initialPageLinks)
				.addAllFinder(findersTags)
				.addAllMatchs(matchs).build());
	}
}
