package br.com.gencrawler.crawler.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

public final class AjaxCollector implements Collector {
	private final List<String> items;

	private String url;
	private String find;
	private String match;

	private final WebDriver driver;

	public AjaxCollector() {
		this.items = new ArrayList<>();
		this.driver = new FirefoxDriver();
	}

	public AjaxCollector(final WebDriver driver) {
		this.items = new ArrayList<>();
		this.driver = driver;
	}
	
	public AjaxCollector(final List<String> items) {
		this.items = items;
		this.driver = new FirefoxDriver();
	}

	public AjaxCollector(final String url, final String find, final String match) {
		this.items = new ArrayList<>();
		this.driver = new FirefoxDriver();

		this.url = url;
		this.find = find;
		this.match = match;
	}

	@Deprecated
	public AjaxCollector(final String... filds) {
		this.items = new ArrayList<>();
		this.driver = new FirefoxDriver();

		this.url = filds[0];
		this.find = filds[1];
		this.match = filds[2];
	}

	public void openBrowser() {
		this.driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
	}
	
	public void closeBrowser() {
		this.driver.close();
	}
	
	public void runBrowser() {
		List<WebElement> elements;
		if(this.url == null || this.find == null || this.match == null
			|| this.url.equals("") || this.find.equals("") || this.match.equals(""))
			throw new RuntimeException("Peat all data");
		
		this.driver.get(this.url);
		if(this.match.toLowerCase().equals("class"))
			elements = this.driver.findElements(By.className(this.find));
		else if(this.match.toLowerCase().equals("id"))
			elements = this.driver.findElements(By.id(this.find));
		else
			throw new RuntimeException("Invalid match error");
		elements.forEach(element -> this.items.add(element.getText()));
	}

	public final void set(final String find, final String match, final String url) {
		this.find = find;
		this.match = match;
		this.url = url;
	}

	@Override
	public final void runItem() {
		openBrowser();
		runBrowser();
		closeBrowser();
	}

	@Override
	public final void runItem(final String... filds) {
		this.find = filds[0];
		this.match = filds[1];
		this.url = filds[2];
		runItem();
	}

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

	@Override
	public List<String> getItems() { 
		
		return Collections.unmodifiableList(this.items);
	}

	public void clearItems() { 
		this.items.clear();
	}
}