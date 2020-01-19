package br.com.gencrawler.crawler.core;

import java.util.Set;

import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import br.com.gencrawler.crawler.Helper;

@Tag("NonAJAX")
public class SimpleExtractorTest {
	
	private Helper helper = new Helper();

	@Test
	public void testGetRunPagesLinks() {
		final SimpleExtractor extractor = new SimpleExtractor();
		extractor.runPagesLinks(helper.getUrl(),
				helper.getPaginator());
		final Set<String> links = extractor.getLinks();
		Assertions.assertTrue(links.size() > 0);
	}

	@Test
	public void testAllCrawlersExtractors() {
		final SimpleExtractor extractor = new SimpleExtractor();
		extractor.runPagesLinks(helper.getUrl(),
				helper.getPaginator());
		extractor.runItem(helper.getFinder(),
				helper.getMatch(), helper.getUrl());
		
		Assertions.assertTrue(extractor.getItems().size() > 0);

		final Set<String> links = extractor.getLinks();
		Assertions.assertTrue(links.size() > 0);
	}

	@Test
	public void testRunRotine() {
		final SimpleExtractor extractor = 
			new SimpleExtractor(helper.getUrl(),
				helper.getPaginator(), helper.getFinder(), helper.getMatch());
		extractor.run();
		Assertions.assertTrue(extractor.getItems().size() > 0);
	}
	
	@Test
	public void errorOnURL() {
		final SimpleExtractor extractor = new SimpleExtractor();
		Assertions.assertThrows(RuntimeException.class,
				() -> {
					extractor.runPagesLinks("not work url", "a");
				});
	}

}
