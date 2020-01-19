package br.com.gencrawler.crawler.core;

import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import br.com.gencrawler.crawler.Helper;

@Tag("AJAX")
public class AjaxCollectorTest {

	private Helper helper = new Helper();

	@BeforeAll
	public static void systemVariable() {
		new Helper().setProperty();
	}
	
	@Test
	public void testAllCrawler() {
		final AjaxCollector collector = new AjaxCollector();
		collector.runItem(helper.getAjaxFinder(), "class",
				helper.getAjaxUrl());
		collector.writeToConsole();
		
		Assertions.assertTrue(collector.getItems().size() > 0);
	}

	@Test
	public void errorOnURL() {
		final AjaxCollector collector = new AjaxCollector();
		Assertions.assertThrows(RuntimeException.class, () -> {
			collector.runItem(helper.getAjaxFinder(), "class",
					"");
		});
	}

}
