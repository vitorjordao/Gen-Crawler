package br.com.gencrawler.crawler.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import br.com.gencrawler.CommonLinks;

/**
 * Simple Collector helps to crawl web pages with AJAX
*/
public final class AjaxCollector {
	private List<String> items = List.of();
	private Set<String> urlsOfThisSite = Set.of();

	private String url;
	private String find;
	private String match;

	private final WebDriver driver;

	public AjaxCollector() {
		this.driver = new ChromeDriver();
	}

	public AjaxCollector(final String url, final String find, final String match) {
		this.driver = new ChromeDriver();

		this.url = url;
		this.find = find;
		this.match = match;
	}

	/**
	 * Configure the browser
	 * @param await configure web page timeout
	 */
	public void openBrowser(final int await) {
		this.driver.manage().timeouts().implicitlyWait(await, TimeUnit.SECONDS);
	}
	
	/**
	 * Close the browser
	 */
	public void closeBrowser() {
		this.driver.close();
	}
	
	/**
	 * After set "url", "find" and "match" fields
	 */
	public void runBrowser() {
		if(this.url == null || this.find == null || this.match == null
			|| this.url.isBlank() || this.find.isBlank() || this.match.isBlank())
		throw new RuntimeException("Peat all data");
		
		List<WebElement> elements;

		this.driver.get(this.url);
		if(this.match.toLowerCase().contains("class"))
			elements = this.driver.findElements(By.className(this.find));
		else if(this.match.toLowerCase().contains("id"))
			elements = this.driver.findElements(By.id(this.find));
		else
			throw new RuntimeException("Invalid match error");
		
		this.items = elements.stream()
			.map(element -> element.getText())
			.collect(Collectors.toList());

		final String baseUrl = CommonLinks.baseUrl(driver.getCurrentUrl());

		this.urlsOfThisSite = this.driver.findElements(By.tagName("a"))
			.parallelStream()
			.map(element -> element.getAttribute("href"))
			.filter(uri -> CommonLinks.is(baseUrl, uri))
			.collect(Collectors.toSet());
	}

	/**
	 * Set mandatory fields
	 * @param find ...
	 * @param match ...
	 * @param url is a url web page
	 */
	public final void set(final String find, final String match, final String url) {
		this.find = find;
		this.match = match;
		this.url = url;
	}

	public final void run() {
		openBrowser(2);
		runBrowser();
		closeBrowser();
	}

	public final void run(final String find, final String match, final String url) {
		this.find = find;
		this.match = match;
		this.url = url;
		run();
	}

	public List<String> getItems() { 
		
		return Collections.unmodifiableList(this.items);
	}

	public Set<String> getURLs() { 
		return Collections.unmodifiableSet(this.urlsOfThisSite);
	}

	public void clearItems() { 
		this.items.clear();
	}
}