package br.com.gencrawler.crawler.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public final class SimpleCollector {
	private final List<String> items;

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
			|| this.url.equals("") || this.find.equals("") || this.match.equals(""))
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
	}

	public final void writeToConsole() {
		this.items.parallelStream().forEach(a -> {
			System.out.println("----");
			System.out.println(a);
		});
	}

	public List<String> getItems() { 
		
		return Collections.unmodifiableList(this.items);
	}
}