package br.com.gencrawler.crawler.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public final class SimpleExtractor implements Crawler {
	private final Set<String> links;
	private final List<List<String>> items;

	private String url; 
	private String paginator;
	private String linkFinded;
	private String match;

	public SimpleExtractor() {
		this.links = new HashSet<>();
		this.items = new ArrayList<>();
	}

	@Deprecated
	public SimpleExtractor(final Set<String> links, final List<List<String>> products) {
		this.links = links;
		this.items = products;
	}

	public SimpleExtractor(final String url, final String paginator, final String linkFinded, final String match) {
		this.links = new HashSet<>();
		this.items = new ArrayList<>();

		this.url = url;
		this.paginator = paginator;
		this.linkFinded = linkFinded;
		this.match = match;
	}

	@Deprecated
	public SimpleExtractor(final String... filds) {
		this.links = new HashSet<>();
		this.items = new ArrayList<>();

		this.url = filds[0];
		this.paginator = filds[1];
		this.linkFinded = filds[2];
		this.match = filds[3];
	}

	public void verify() {
		if (this.url == null || this.paginator == null || this.linkFinded == null || this.match == null
			|| this.url.equals("") || this.paginator.equals("") || this.linkFinded.equals("") || this.match.equals(""))
			throw new RuntimeException("Peat all data");
	}

	public final void runPagesLinks() {
		if (!this.links.contains(this.url)) {
			try {
				final Document document = Jsoup.connect(this.url).timeout(120000).get();
				final Elements otherLinks = document.select(this.paginator);

				for (final Element page : otherLinks) {
					this.links.add(this.url);
					runPagesLinks(page.attr("abs:href"), this.paginator);
				}
			} catch (final IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			} catch (final Exception e) {
				throw new RuntimeException("No expected error - " + e.getMessage(), e);
			}
		}
	}

	public final void runPagesLinks(final String URL, final String paginator) {
		this.url = URL;
		this.paginator = paginator;
		runPagesLinks();
	}

	@Override
	public final void runItem() {
		verify();
		this.links.parallelStream().forEach(x -> {
			Document document;
			try {
				document = Jsoup.connect(x).get();
				final Elements itemsLinks = document.select(this.linkFinded);
				for (final Element item : itemsLinks) {
					if (item.text().matches(this.match)) {
						final ArrayList<String> temporary = new ArrayList<>();
						temporary.add(item.text());
						temporary.add(item.attr("abs:href"));
						this.items.add(temporary);
					}
				}
			} catch (final IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			} catch (final Exception e) {
				throw new RuntimeException("No expected error - " + e.getMessage(), e);
			}
		});
	}
 
	@Override
	@Deprecated
	public final void runItem(final String... filds) {
		
		this.url = filds[2];
		this.linkFinded = filds[0];
		this.match = filds[1];
		runItem();
	}

	public final void writeToConsole() {
		this.items.parallelStream().forEach(a -> {
			System.out.println("----");
			a.forEach(query -> System.out.println(query));
		});
	}

	@Override
	public void run() {
		runPagesLinks();
		runItem();
	}

	public Set<String> getLinks() {
		return Collections.unmodifiableSet(this.links);
	}

	public List<List<String>> getItems() {
		return Collections.unmodifiableList(this.items);
	}
}
