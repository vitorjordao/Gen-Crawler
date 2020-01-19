package br.com.gencrawler.crawler.build;

import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import br.com.gencrawler.crawler.Helper;
import br.com.gencrawler.crawler.core.SimpleExtractor;

@Tag("NonAJAX")
public class SimpleExtractorBuilderTest {
	private Helper helper = new Helper();

	@Test
	public void buildCrawler() {
		final SimpleExtractorBuilder cb = new SimpleExtractorBuilder();
		final SimpleExtractor crawler = cb
				.setInitial(helper.getUrl())
				.setPaginator(helper.getPaginator())
				.setFinder(helper.getFinder())
				.setMatch(helper.getMatch()).build();
		Assertions.assertTrue(crawler.getLinks().size() > 0);
		Assertions.assertTrue(crawler.getItems().size() > 0);
	}

	@Test
	public void errorCrawlerBuild() {
		final SimpleExtractorBuilder cb = new SimpleExtractorBuilder();
		Assertions.assertThrows(RuntimeException.class,
				() -> cb.setInitial(helper.getUrl())
						.setPaginator(
								helper.getPaginator())
						.setMatch(helper.getMatch()).build());
	}
}
