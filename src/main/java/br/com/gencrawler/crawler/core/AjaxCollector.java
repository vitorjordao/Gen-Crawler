package br.com.gencrawler.crawler.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public final class AjaxCollector {
	private final List<String> items;

	private String url;
	private String find;
	private String match;

	private final WebDriver driver;

	public AjaxCollector() {
		this.items = new ArrayList<>();
		this.driver = new ChromeDriver();
	}

	public AjaxCollector(final String url, final String find, final String match) {
		this.items = new ArrayList<>();
		this.driver = new ChromeDriver();

		this.url = url;
		this.find = find;
		this.match = match;
	}

	public void openBrowser(final int await) {
		this.driver.manage().timeouts().implicitlyWait(await, TimeUnit.SECONDS);
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
		if(this.match.toLowerCase().contains("class"))
			elements = this.driver.findElements(By.className(this.find));
		else if(this.match.toLowerCase().contains("id"))
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

	public final void writeToConsole() {
		this.items.parallelStream().forEach(a -> {
			System.out.println("----");
			System.out.println(a);
		});
	}

	public List<String> getItems() { 
		
		return Collections.unmodifiableList(this.items);
	}

	public void clearItems() { 
		this.items.clear();
	}
}