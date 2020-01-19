package br.com.gencrawler.crawler.parallelbuild;

import java.util.ArrayList;
import java.util.List;

import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import br.com.gencrawler.crawler.Helper;
import br.com.gencrawler.crawler.core.AjaxCollector;

@Tag("AJAX")
public class ParallelAjaxCollectorsBuilderTest {

	private Helper helper = new Helper();
	
	@BeforeAll
	public static void systemVariable() {
		new Helper().setProperty();
	}
	
	@Test
	public void buildCrawler() {
		final ParallelAjaxCollectorsBuilder pcb = new ParallelAjaxCollectorsBuilder();
		final List<String> initialPageLinks = new ArrayList<>();
		final List<String> findersTags = new ArrayList<>();
		final List<String> matchs = new ArrayList<>();
		
		initialPageLinks.add(helper.getAjaxUrl());
		findersTags.add(helper.getAjaxFinder());
		matchs.add("class");

		initialPageLinks.add(helper.getAjaxUrl());
		findersTags.add(helper.getAjaxFinder());
		matchs.add("class");
		
		initialPageLinks.add(helper.getAjaxUrl());
		findersTags.add(helper.getAjaxFinder());
		matchs.add("class");
		
		final List<AjaxCollector> crawlers = pcb
				.addAllUrl(initialPageLinks)
				.addAllFinder(findersTags)
				.addAllMatchs(matchs).build();
		Assertions.assertTrue(crawlers.size() > 0);
		Assertions.assertTrue(crawlers.get(0).getItems().size() > 0);
	}

	@Test
	public void errorCrawlerBuild() {
		final ParallelAjaxCollectorsBuilder pcb = new ParallelAjaxCollectorsBuilder();
		
		final List<String> initialPageLinks = new ArrayList<>();
		final List<String> findersTags = new ArrayList<>();
		final List<String> matchs = new ArrayList<>();
		
		initialPageLinks.add(helper.getAjaxUrl());
		findersTags.add(helper.getAjaxFinder());
		matchs.add("class");

		initialPageLinks.add(helper.getAjaxUrl());
		matchs.add("class");
		
		initialPageLinks.add(helper.getAjaxUrl());
		findersTags.add(helper.getAjaxFinder());
		matchs.add("class");
		
		Assertions.assertThrows(RuntimeException.class,
				() -> pcb.addAllUrl(initialPageLinks)
				.addAllFinder(findersTags)
				.addAllMatchs(matchs).build());
	}
}
