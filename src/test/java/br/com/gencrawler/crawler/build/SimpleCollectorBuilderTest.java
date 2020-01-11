package br.com.gencrawler.crawler.build;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import br.com.gencrawler.crawler.Helper;
import br.com.gencrawler.crawler.core.SimpleCollector;

public class SimpleCollectorBuilderTest {

	private Helper helper = new Helper();

	@Test
	public void buildCrawler() {
		final SimpleCollectorBuilder cb = new SimpleCollectorBuilder();
		final SimpleCollector crawler = cb
				.setInitial(helper.getAjaxUrl())
				.setFinder(helper.getAjaxFinder2())
				.setMatch(helper.getMatch()).build();
		Assertions.assertTrue(crawler.getItems().size() > 0);
	}

	@Test
	public void errorCrawlerBuild() {
		final SimpleCollectorBuilder cb = new SimpleCollectorBuilder();
		Assertions.assertThrows(RuntimeException.class,
				() -> cb.setInitial(helper.getAjaxUrl())
						.setMatch(helper.getAjaxMatch()).build());
	}
}
