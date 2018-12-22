package br.com.gencrawler.crawler.build;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import br.com.gencrawler.crawler.core.Extractor;

public class ExtractorBuilderTest {
	@Test
	public void buildCrawler() {
		final ExtractorBuilder cb = new ExtractorBuilder();
		final Extractor crawler = cb
				.setInitial("https://www.americanas.com.br/categoria/livros/didaticos-e-educacao/ensino-fundamental-6o-ao-9o-ano?ordenacao=topSelling&origem=omega&chave=brd_hs_dt_0_livros-6-ao-9_material-escolar18&pfm_carac=BLOCO%201&pfm_index=0&pfm_page=special&pfm_pos=contenttop3&pfm_type=vit_spacey")
				.setPaginator("a[href^=\"/categoria/livros/didaticos-e-educacao/ensino-fundamental-6o-ao-9o-ano?\"]")
				.setFinder("div[class^=\"product-grid-item\"] a[class^=\"card-product-url\"]")
				.setMatch("^.*?().*$").build();
		Assertions.assertTrue(crawler.getLinks().size() > 0);
		Assertions.assertTrue(crawler.getItems().size() > 0);
	}

	@Test
	public void errorCrawlerBuild() {
		final ExtractorBuilder cb = new ExtractorBuilder();
		Assertions.assertThrows(RuntimeException.class,
				() -> cb.setInitial("https://www.americanas.com.br/categoria/livros/didaticos-e-educacao/ensino-fundamental-6o-ao-9o-ano?ordenacao=topSelling&origem=omega&chave=brd_hs_dt_0_livros-6-ao-9_material-escolar18&pfm_carac=BLOCO%201&pfm_index=0&pfm_page=special&pfm_pos=contenttop3&pfm_type=vit_spacey")
						.setPaginator(
								"a[href^=\"/categoria/livros/didaticos-e-educacao/ensino-fundamental-6o-ao-9o-ano?\"]")
						.setMatch("^.*?().*$").build());
	}
}
