package br.com.gencrawler.crawler.core;

import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SimpleExtractorTest {

	@Test
	public void testGetRunPagesLinks() {
		final SimpleExtractor bwc = new SimpleExtractor();
		bwc.runPagesLinks("https://www.americanas.com.br/categoria/livros/didaticos-e-educacao/ensino-fundamental-6o-ao-9o-ano?ordenacao=topSelling&origem=omega&chave=brd_hs_dt_0_livros-6-ao-9_material-escolar18&pfm_carac=BLOCO%201&pfm_index=0&pfm_page=special&pfm_pos=contenttop3&pfm_type=vit_spacey",
				"a[href^=\"/categoria/livros/didaticos-e-educacao/ensino-fundamental-6o-ao-9o-ano?\"]");
		final Set<String> links = bwc.getLinks();
		Assertions.assertTrue(links.size() > 0);
	}

	@Test
	public void testAllCrawler() {
		final SimpleExtractor bwc = new SimpleExtractor();
		bwc.runPagesLinks("https://www.americanas.com.br/categoria/livros/didaticos-e-educacao/ensino-fundamental-6o-ao-9o-ano?ordenacao=topSelling&origem=omega&chave=brd_hs_dt_0_livros-6-ao-9_material-escolar18&pfm_carac=BLOCO%201&pfm_index=0&pfm_page=special&pfm_pos=contenttop3&pfm_type=vit_spacey",
				"a[href^=\"/categoria/livros/didaticos-e-educacao/ensino-fundamental-6o-ao-9o-ano?\"]");
		bwc.runItem("div[class^=\"product-grid-item\"] a[class^=\"card-product-url\"]",
				"^.*?().*$", "https://www.americanas.com.br/categoria/livros/didaticos-e-educacao/ensino-fundamental-6o-ao-9o-ano?ordenacao=topSelling&origem=omega&chave=brd_hs_dt_0_livros-6-ao-9_material-escolar18&pfm_carac=BLOCO%201&pfm_index=0&pfm_page=special&pfm_pos=contenttop3&pfm_type=vit_spacey");
		Assertions.assertTrue(bwc.getItems().size() > 0);
	}
	
	@Test
	public void errorOnURL() {
		final SimpleExtractor bwc = new SimpleExtractor();
		Assertions.assertThrows(RuntimeException.class,
				() -> {
					bwc.runPagesLinks("not work url",
							"a[href^=\"https://www.casa.center/loja/catalogo.php?loja=577838&categoria=132&pg=\"]");
				});
	}

}
