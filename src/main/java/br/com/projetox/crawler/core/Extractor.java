package br.com.projetox.crawler.core;

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

public final class Extractor implements Crawler {
	private final Set<String> links;
	private final List<List<String>> items;

	private String url; 
	private String paginator;
	private String linkFinded;
	private String match;

	public Extractor() {
		this.links = new HashSet<>();
		this.items = new ArrayList<>();
	}

	public Extractor(final Set<String> links, final List<List<String>> products) {
		this.links = links;
		this.items = products;
	}

	public Extractor(final String url, final String paginator, final String linkFinded, final String match) {
		this.links = new HashSet<>();
		this.items = new ArrayList<>();

		this.url = url;
		this.paginator = paginator;
		this.linkFinded = linkFinded;
		this.match = match;
	}

	public final void runPagesLinks() {
		if (!this.links.contains(this.url)) {
			try {
				final Document document = Jsoup.connect(this.url).timeout(120000).get();
				final Elements otherLinks = document.select(this.paginator);

				for (final Element page : otherLinks) {
					if (this.links.add(this.url)) {
						System.out.println(this.url);
					}
					runPagesLinks(page.attr("abs:href"), this.paginator);
				}
			} catch (final IOException e) {
				throw new RuntimeException(e.getMessage());
			} catch (final Exception e) {
				throw new RuntimeException("No expected error - " + e.getMessage());
			}
		}
	}

	public final void runPagesLinks(final String URL, final String paginator) {
		this.url = URL;
		this.paginator = paginator;
		runPagesLinks();
	}

	@Override
	public final void runItems() {
		this.links.parallelStream().forEach(x -> {
			Document document;
			try {
				document = Jsoup.connect(x).get();
				final Elements itemsLinks = document.select(this.linkFinded);
				for (final Element item : itemsLinks) {
					if (item.text().matches(this.match)) {
						System.out.println(item.attr("abs:href"));

						final ArrayList<String> temporary = new ArrayList<>();
						temporary.add(item.text());
						temporary.add(item.attr("abs:href"));
						this.items.add(temporary);
					}
				}
			} catch (final IOException e) {
				throw new RuntimeException(e.getMessage());
			} catch (final Exception e) {
				throw new RuntimeException("No expected error - " + e.getMessage());
			}
		});
	}
 
	@Override
	public final void runItems(final String linkFinded, final String match) {
		this.linkFinded = linkFinded;
		this.match = match;
		runItems();
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
		runItems();
	}

	public Set<String> getLinks() {
		return Collections.unmodifiableSet(this.links);
	}

	public List<List<String>> getItems() {
		return Collections.unmodifiableList(this.items);
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	public void setPaginator(final String paginator) {
		this.paginator = paginator;
	}

	public void setLinkFinded(final String linkFinded) {
		this.linkFinded = linkFinded;
	}

	public void setMatch(final String match) {
		this.match = match;
	}

}
