package br.com.gencrawler.crawler.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import br.com.gencrawler.crawler.Helper;

public class AjaxCollectorTest {

	private Helper helper = new Helper();

	@BeforeAll
	public static void systemVariable() {
		new Helper().setProperty();
	}
	
	@Test
	public void testAllCrawler() {
		final AjaxCollector bwc = new AjaxCollector();
		bwc.runItem(helper.getAjaxFinder(), "class",
				helper.getAjaxUrl());
		bwc.writeToConsole();
		
		Assertions.assertTrue(bwc.getItems().size() > 0);
	}

	@Test
	public void errorOnURL() {
		final AjaxCollector bwc = new AjaxCollector();
		Assertions.assertThrows(RuntimeException.class, () -> {
			bwc.runItem(helper.getAjaxFinder(), "class",
					"");
		});
	}

}
