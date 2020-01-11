package br.com.gencrawler.crawler.core;

import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import br.com.gencrawler.crawler.Helper;

public class SimpleExtractorTest {
	
	private Helper helper = new Helper();

	@Test
	public void testGetRunPagesLinks() {
		final SimpleExtractor bwc = new SimpleExtractor();
		bwc.runPagesLinks(helper.getUrl(),
				helper.getPaginator());
		final Set<String> links = bwc.getLinks();
		Assertions.assertTrue(links.size() > 0);
	}

	@Test
	public void testAllCrawler() {
		final SimpleExtractor bwc = new SimpleExtractor();
		bwc.runPagesLinks(helper.getUrl(),
				helper.getPaginator());
		bwc.runItem(helper.getFinder(),
				helper.getMatch(), helper.getUrl());
		Assertions.assertTrue(bwc.getItems().size() > 0);
	}
	
	@Test
	public void errorOnURL() {
		final SimpleExtractor bwc = new SimpleExtractor();
		Assertions.assertThrows(RuntimeException.class,
				() -> {
					bwc.runPagesLinks("not work url", "a");
				});
	}

}
