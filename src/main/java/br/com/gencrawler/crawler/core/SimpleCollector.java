package br.com.gencrawler.crawler.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public final class SimpleCollector implements Collector {
	private final List<String> items;

	private String url;
	private String find;
	private String match;

	public SimpleCollector() {
		this.items = new ArrayList<>();
	}
	
	public SimpleCollector(final List<String> items) {
		this.items = items;
	}

	public SimpleCollector(final String url, final String find, final String match) {
		this.items = new ArrayList<>();

		this.url = url;
		this.find = find;
		this.match = match;
	}

	@Deprecated
	public SimpleCollector(final String... filds) {
		this.items = new ArrayList<>();

		this.url = filds[0];
		this.find = filds[1];
		this.match = filds[2];
	}

	public void verify() {
		if (this.url == null || this.find == null || this.match == null
			|| this.url.equals("") || this.find.equals("") || this.match.equals(""))
			throw new RuntimeException("Peat all data");
	}

	@Override
	public final void runItem() {
		verify();
		Document document;
		try {
			document = Jsoup.connect(this.url).get();
			final Elements itemsLinks = document.select(this.find);
			for (final Element item : itemsLinks) {
				if (item.text().matches(this.match)) {
					System.out.println(item.attr("abs:href"));

					this.items.add(item.text());
					this.items.add(item.attr("abs:href"));
				}
			}
		} catch (final IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (final Exception e) {
			throw new RuntimeException("No expected error - " + e.getMessage(), e);
		}
	}

	@Override
	public final void runItem(final String... filds) {
		this.find = filds[0];
		this.match = filds[1];
		this.url = filds[2];
		runItem();
	}

	public final void writeToConsole() {
		this.items.parallelStream().forEach(a -> {
			System.out.println("----");
			System.out.println(a);
		});
	}

	@Override
	public void run() {
		runItem();
	}

	@Override
	public List<String> getItems() { 
		
		return Collections.unmodifiableList(this.items);
	}
}