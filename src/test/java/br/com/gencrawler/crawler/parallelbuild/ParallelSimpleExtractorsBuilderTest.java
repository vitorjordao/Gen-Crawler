package br.com.gencrawler.crawler.parallelbuild;

import java.util.ArrayList;
import java.util.List;

import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import br.com.gencrawler.crawler.Helper;
import br.com.gencrawler.crawler.core.SimpleExtractor;

@Tag("NonAJAX")
public class ParallelSimpleExtractorsBuilderTest {

	private Helper helper = new Helper();

	@Test
	public void buildCrawler() {
		final ParallelSimpleExtractorsBuilder pcb = new ParallelSimpleExtractorsBuilder();
		final List<String> initialPageLinks = new ArrayList<>();
		final List<String> paginatorsLinks = new ArrayList<>();
		final List<String> findersTags = new ArrayList<>();
		final List<String> matchs = new ArrayList<>();

		initialPageLinks.add(helper.getUrl());
		paginatorsLinks.add(helper.getPaginator());
		findersTags.add(helper.getFinder());
		matchs.add(helper.getMatch());

		initialPageLinks.add(helper.getUrl());
		paginatorsLinks.add(helper.getPaginator());
		findersTags.add(helper.getFinder());
		matchs.add(helper.getMatch());

		initialPageLinks.add(helper.getUrl());
		paginatorsLinks.add(helper.getPaginator());
		findersTags.add(helper.getFinder());
		matchs.add(helper.getMatch());

		final List<SimpleExtractor> crawlers = pcb.addAllUrl(initialPageLinks).addAllPaginators(paginatorsLinks)
				.addAllFinder(findersTags).addAllMatchs(matchs).build();
		Assertions.assertTrue(crawlers.size() > 0);
		Assertions.assertTrue(crawlers.get(0).getLinks().size() > 0);
		Assertions.assertTrue(crawlers.get(0).getItems().size() > 0);
	}

	@Test
	public void errorCrawlerBuild() {
		final ParallelSimpleExtractorsBuilder pcb = new ParallelSimpleExtractorsBuilder();

		final List<String> initialPageLinks = new ArrayList<>();
		final List<String> paginatorsLinks = new ArrayList<>();
		final List<String> findersTags = new ArrayList<>();
		final List<String> matchs = new ArrayList<>();

		initialPageLinks.add(helper.getUrl());
		paginatorsLinks.add(helper.getPaginator());
		findersTags.add(helper.getFinder());
		matchs.add(helper.getMatch());

		initialPageLinks.add(helper.getUrl());
		paginatorsLinks.add(helper.getPaginator());
		matchs.add(helper.getMatch());

		initialPageLinks.add(helper.getUrl());
		paginatorsLinks.add(helper.getPaginator());
		findersTags.add(helper.getFinder());
		matchs.add(helper.getMatch());

		Assertions.assertThrows(RuntimeException.class, () -> pcb.addAllUrl(initialPageLinks)
				.addAllPaginators(paginatorsLinks).addAllFinder(findersTags).addAllMatchs(matchs).build());
	}
}
