package br.com.gencrawler.crawler.core;

import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import br.com.gencrawler.crawler.Helper;

@Tag("NonAJAX")
public class SimpleCollectorTest {

	private Helper helper = new Helper();

	@Test
	public void testAllCrawler() {
		final SimpleCollector collector = new SimpleCollector();
		collector.runItem(helper.getAjaxFinder2(), helper.getMatch(),
				helper.getAjaxUrl());
		collector.writeToConsole();
		
		Assertions.assertTrue(collector.getItems().size() > 0);
	}

	@Test
	public void errorOnURL() {
		final SimpleCollector collector = new SimpleCollector();
		Assertions.assertThrows(RuntimeException.class, () -> {
			collector.runItem(helper.getAjaxFinder2(), helper.getMatch(),
					"");
		});
	}

}
