package br.com.gencrawler.crawler.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import br.com.gencrawler.crawler.Helper;

public class SimpleCollectorTest {

	private Helper helper = new Helper();

	@Test
	public void testAllCrawler() {
		final SimpleCollector bwc = new SimpleCollector();
		bwc.runItem(helper.getAjaxFinder2(), helper.getMatch(),
				helper.getAjaxUrl());
		bwc.writeToConsole();
		
		Assertions.assertTrue(bwc.getItems().size() > 0);
	}

	@Test
	public void errorOnURL() {
		final SimpleCollector bwc = new SimpleCollector();
		Assertions.assertThrows(RuntimeException.class, () -> {
			bwc.runItem(helper.getAjaxFinder2(), helper.getMatch(),
					"");
		});
	}

}
