package br.com.gencrawler.crawler.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public final class Collector implements Crawler {
	private final List<String> items;

	private String url;
	private String find;
	private String match;

	public Collector() {
		this.items = new ArrayList<>();
	}

	public Collector(final List<String> products) {
		this.items = products;
	}

	public Collector(final String url, final String find, final String match) {
		this.items = new ArrayList<>();

		this.url = url;
		this.find = find;
		this.match = match;
	}

	@Override
	public final void runItem() {
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
			throw new RuntimeException(e.getMessage());
		} catch (final Exception e) {
			throw new RuntimeException("No expected error - " + e.getMessage());
		}
	}

	@Override
	public final void runItem(final String find, final String match, final String url) {
		this.find = find;
		this.match = match;
		this.url = url;
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

	public List<String> getItems() {
		return Collections.unmodifiableList(this.items);
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	public void setFind(final String find) {
		this.find = find;
	}

	public void setMatch(final String match) {
		this.match = match;
	}
}