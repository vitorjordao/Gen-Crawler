package br.com.gencrawler.crawler.parallelbuild;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import br.com.gencrawler.crawler.core.SimpleExtractor;

public class ParallelSimpleExtractorsBuilderTest {

	@Test
	public void buildCrawler() {
		final ParallelSimpleExtractorsBuilder pcb = new ParallelSimpleExtractorsBuilder();
		final List<String> initialPageLinks = new ArrayList<>();
		final List<String> paginatorsLinks = new ArrayList<>();
		final List<String> findersTags = new ArrayList<>();
		final List<String> matchs = new ArrayList<>();

		initialPageLinks.add("https://www.americanas.com.br/categoria/livros/didaticos-e-educacao/ensino-fundamental-6o-ao-9o-ano?ordenacao=topSelling&origem=omega&chave=brd_hs_dt_0_livros-6-ao-9_material-escolar18&pfm_carac=BLOCO%201&pfm_index=0&pfm_page=special&pfm_pos=contenttop3&pfm_type=vit_spacey");
		paginatorsLinks.add("a[href^=\"/categoria/livros/didaticos-e-educacao/ensino-fundamental-6o-ao-9o-ano?\"]");
		findersTags.add("div[class^=\"product-grid-item\"] a[class^=\"card-product-url\"]");
		matchs.add("^.*?().*$");

		initialPageLinks.add("https://www.americanas.com.br/categoria/livros/didaticos-e-educacao/ensino-fundamental-6o-ao-9o-ano?ordenacao=topSelling&origem=omega&chave=brd_hs_dt_0_livros-6-ao-9_material-escolar18&pfm_carac=BLOCO%201&pfm_index=0&pfm_page=special&pfm_pos=contenttop3&pfm_type=vit_spacey");
		paginatorsLinks.add("a[href^=\"/categoria/livros/didaticos-e-educacao/ensino-fundamental-6o-ao-9o-ano?\"]");
		findersTags.add("div[class^=\"product-grid-item\"] a[class^=\"card-product-url\"]");
		matchs.add("^.*?().*$");

		initialPageLinks.add("https://www.americanas.com.br/categoria/livros/didaticos-e-educacao/ensino-fundamental-6o-ao-9o-ano?ordenacao=topSelling&origem=omega&chave=brd_hs_dt_0_livros-6-ao-9_material-escolar18&pfm_carac=BLOCO%201&pfm_index=0&pfm_page=special&pfm_pos=contenttop3&pfm_type=vit_spacey");
		paginatorsLinks.add("a[href^=\"/categoria/livros/didaticos-e-educacao/ensino-fundamental-6o-ao-9o-ano?\"]");
		findersTags.add("div[class^=\"product-grid-item\"] a[class^=\"card-product-url\"]");
		matchs.add("^.*?().*$");

		final List<SimpleExtractor> crawlers = pcb.addAllUrl(initialPageLinks).addAllPaginators(paginatorsLinks)
				.addAllFinder(findersTags).addAllMatchs(matchs).build();
		Assertions.assertTrue(crawlers.size() > 0);
		Assertions.assertTrue(crawlers.get(0).getLinks().size() > 0);
		Assertions.assertTrue(crawlers.get(0).getItems().size() > 0);
	}

	@Test
	public void errorCrawlerBuild() {
		final ParallelSimpleExtractorsBuilder pcb = new ParallelSimpleExtractorsBuilder();

		final List<String> initialPageLinks = new ArrayList<>();
		final List<String> paginatorsLinks = new ArrayList<>();
		final List<String> findersTags = new ArrayList<>();
		final List<String> matchs = new ArrayList<>();

		initialPageLinks.add("https://www.americanas.com.br/categoria/livros/didaticos-e-educacao/ensino-fundamental-6o-ao-9o-ano?ordenacao=topSelling&origem=omega&chave=brd_hs_dt_0_livros-6-ao-9_material-escolar18&pfm_carac=BLOCO%201&pfm_index=0&pfm_page=special&pfm_pos=contenttop3&pfm_type=vit_spacey");
		paginatorsLinks.add("a[href^=\"/categoria/livros/didaticos-e-educacao/ensino-fundamental-6o-ao-9o-ano?\"]");
		findersTags.add("div[class^=\"product-grid-item\"] a[class^=\"card-product-url\"]");
		matchs.add("^.*?().*$");

		initialPageLinks.add("https://www.americanas.com.br/categoria/livros/didaticos-e-educacao/ensino-fundamental-6o-ao-9o-ano?ordenacao=topSelling&origem=omega&chave=brd_hs_dt_0_livros-6-ao-9_material-escolar18&pfm_carac=BLOCO%201&pfm_index=0&pfm_page=special&pfm_pos=contenttop3&pfm_type=vit_spacey");
		paginatorsLinks.add("a[href^=\"/categoria/livros/didaticos-e-educacao/ensino-fundamental-6o-ao-9o-ano?\"]");
		matchs.add("^.*?().*$");

		initialPageLinks.add("https://www.americanas.com.br/categoria/livros/didaticos-e-educacao/ensino-fundamental-6o-ao-9o-ano?ordenacao=topSelling&origem=omega&chave=brd_hs_dt_0_livros-6-ao-9_material-escolar18&pfm_carac=BLOCO%201&pfm_index=0&pfm_page=special&pfm_pos=contenttop3&pfm_type=vit_spacey");
		paginatorsLinks.add("a[href^=\"/categoria/livros/didaticos-e-educacao/ensino-fundamental-6o-ao-9o-ano?\"]");
		findersTags.add("div[class^=\"product-grid-item\"] a[class^=\"card-product-url\"]");
		matchs.add("^.*?().*$");

		Assertions.assertThrows(RuntimeException.class, () -> pcb.addAllUrl(initialPageLinks)
				.addAllPaginators(paginatorsLinks).addAllFinder(findersTags).addAllMatchs(matchs).build());
	}
}
