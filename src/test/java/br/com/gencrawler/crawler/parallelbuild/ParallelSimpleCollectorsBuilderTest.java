package br.com.gencrawler.crawler.parallelbuild;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import br.com.gencrawler.crawler.core.SimpleCollector;

public class ParallelSimpleCollectorsBuilderTest {
	
	@Test
	public void buildCrawler() {
		final ParallelSimpleCollectorsBuilder pcb = new ParallelSimpleCollectorsBuilder();
		final List<String> initialPageLinks = new ArrayList<>();
		final List<String> findersTags = new ArrayList<>();
		final List<String> matchs = new ArrayList<>();
		
		initialPageLinks.add("https://www.americanas.com.br/produto/130177939/liquidificador-philips-walita-problend-2136-0-com-jarra-de-vidro-branco-2l-e-5-velocidades-800w?chave=prf_hm_0_oh_4_txar_00&hl=lower&pfm_carac=1&pfm_index=3&pfm_page=home&pfm_pos=maintop4&pfm_type=vit_spacey&voltagem=220V");
		findersTags.add("p[class^=\"sales-price\"]");
		matchs.add("^.*?().*$");

		initialPageLinks.add("https://www.americanas.com.br/produto/130177939/liquidificador-philips-walita-problend-2136-0-com-jarra-de-vidro-branco-2l-e-5-velocidades-800w?chave=prf_hm_0_oh_4_txar_00&hl=lower&pfm_carac=1&pfm_index=3&pfm_page=home&pfm_pos=maintop4&pfm_type=vit_spacey&voltagem=220V");
		findersTags.add("p[class^=\"sales-price\"]");
		matchs.add("^.*?().*$");
		
		initialPageLinks.add("https://www.americanas.com.br/produto/130177939/liquidificador-philips-walita-problend-2136-0-com-jarra-de-vidro-branco-2l-e-5-velocidades-800w?chave=prf_hm_0_oh_4_txar_00&hl=lower&pfm_carac=1&pfm_index=3&pfm_page=home&pfm_pos=maintop4&pfm_type=vit_spacey&voltagem=220V");
		findersTags.add("p[class^=\"sales-price\"]");
		matchs.add("^.*?().*$");
		
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
		
		initialPageLinks.add("https://www.americanas.com.br/produto/130177939/liquidificador-philips-walita-problend-2136-0-com-jarra-de-vidro-branco-2l-e-5-velocidades-800w?chave=prf_hm_0_oh_4_txar_00&hl=lower&pfm_carac=1&pfm_index=3&pfm_page=home&pfm_pos=maintop4&pfm_type=vit_spacey&voltagem=220V");
		findersTags.add("p[class^=\"sales-price\"]");
		matchs.add("^.*?().*$");

		initialPageLinks.add("https://www.americanas.com.br/produto/130177939/liquidificador-philips-walita-problend-2136-0-com-jarra-de-vidro-branco-2l-e-5-velocidades-800w?chave=prf_hm_0_oh_4_txar_00&hl=lower&pfm_carac=1&pfm_index=3&pfm_page=home&pfm_pos=maintop4&pfm_type=vit_spacey&voltagem=220V");
		matchs.add("^.*?().*$");
		
		initialPageLinks.add("https://www.americanas.com.br/produto/130177939/liquidificador-philips-walita-problend-2136-0-com-jarra-de-vidro-branco-2l-e-5-velocidades-800w?chave=prf_hm_0_oh_4_txar_00&hl=lower&pfm_carac=1&pfm_index=3&pfm_page=home&pfm_pos=maintop4&pfm_type=vit_spacey&voltagem=220V");
		findersTags.add("p[class^=\"sales-price\"]");
		matchs.add("^.*?().*$");
		
		Assertions.assertThrows(RuntimeException.class,
				() -> pcb.addAllUrl(initialPageLinks)
				.addAllFinder(findersTags)
				.addAllMatchs(matchs).build());
	}
}
