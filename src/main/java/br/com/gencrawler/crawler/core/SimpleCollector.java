package br.com.gencrawler.crawler.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import br.com.gencrawler.CommonLinks;

/**
 * Simple Collector helps to crawl web pages without AJAX
*/
public final class SimpleCollector {
	private final List<String> items;
	private Set<String> urlsOfThisSite = Set.of();

	private String url;
	private String find;
	private String match;

	public SimpleCollector() {
		this.items = new ArrayList<>();
	}

	public SimpleCollector(final String url, final String find, final String match) {
		this.items = new ArrayList<>();

		this.url = url;
		this.find = find;
		this.match = match;
	}

	public void verify() {
		if (this.url == null || this.find == null || this.match == null
			|| this.url.isBlank() || this.find.isBlank() || this.match.isBlank())
			throw new RuntimeException("Peat all data");
	}

	public final void run() {
		verify();
		Document document;
		try {
			document = Jsoup.connect(this.url).get();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			throw new RuntimeException(e);
		}

		final Elements itemsLinks = document.select(this.find);

		for (final Element item : itemsLinks) {
			if (item.text().matches(this.match)) {
				this.items.add(item.text());
				this.items.add(item.attr("abs:href"));
			}
		}
		
		final String fullUrl = document.baseUri();

		final String baseUrl = CommonLinks.baseUrl(fullUrl);

		final Elements newLinks = document.select("a");

		urlsOfThisSite = newLinks.parallelStream()
			.map(item -> item.attr("abs:href"))
			.filter(uri -> CommonLinks.is(baseUrl, uri))
			.collect(Collectors.toSet());

	}

	public List<String> getItems() { 
		return Collections.unmodifiableList(this.items);
	}

	public Set<String> getURLs() { 
		return Collections.unmodifiableSet(this.urlsOfThisSite);
	}
}