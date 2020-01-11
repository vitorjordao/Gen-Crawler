package br.com.gencrawler.crawler.build;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import br.com.gencrawler.crawler.Helper;
import br.com.gencrawler.crawler.core.AjaxCollector;

public class AjaxCollectorBuilderTest {

	private Helper helper = new Helper();

	@BeforeAll
	public static void systemVariable() {
		new Helper().setProperty();
	}
	
	@Test
	public void buildCrawler() {
		final AjaxCollectorBuilder cb = new AjaxCollectorBuilder();
		final AjaxCollector crawler = cb
				.setInitial(helper.getAjaxUrl())
				.setFinder(helper.getAjaxFinder())
				.setMatch("class").build();
		Assertions.assertTrue(crawler.getItems().size() > 0);
	}

	@Test
	public void errorCrawlerBuild() {
		final AjaxCollectorBuilder cb = new AjaxCollectorBuilder();
		Assertions.assertThrows(RuntimeException.class,
				() -> cb.setInitial(helper.getAjaxUrl())
						.setMatch(helper.getAjaxMatch()).build());
	}
}
