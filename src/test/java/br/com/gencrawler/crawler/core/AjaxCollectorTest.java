package br.com.gencrawler.crawler.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class AjaxCollectorTest {

	@BeforeAll
	public static void systemVariable() {
		System.setProperty("webdriver.gecko.driver", /*Here add your path -> */"/home/vitor/path/geckodriver");
	}
	
	@Test
	public void testAllCrawler() {
		final AjaxCollector bwc = new AjaxCollector();
		bwc.runItem("bf-price__best", "class",
				"https://www.casa.center/prato-de-sobremesa-com-estampa-de-flores-e-borda-bambu-magnolia/p");
		bwc.writeToConsole();
		
		Assertions.assertTrue(bwc.getItems().size() > 0);
	}

	@Test
	public void errorOnURL() {
		final AjaxCollector bwc = new AjaxCollector();
		Assertions.assertThrows(RuntimeException.class, () -> {
			bwc.runItem("bf-price__best", "class",
					"");
		});
	}

}
