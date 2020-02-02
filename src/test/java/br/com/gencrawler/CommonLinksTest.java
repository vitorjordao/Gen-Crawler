package br.com.gencrawler;

import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import br.com.gencrawler.crawler.Helper;

@Tag("Helper")
public class CommonLinksTest{
    
    private Helper helper = new Helper();

	@Test
	public void testBaseUrl() {
        final String baseUrl = CommonLinks.baseUrl(helper.getUrl());
		
		Assertions.assertEquals(baseUrl, "https://www.americanas.com.br");
    }
    
	@Test
	public void testFullUrlIsTheSameOne() {
        final String baseUrl = CommonLinks.baseUrl(helper.getUrl());
        final boolean isTheSameUrl = CommonLinks.is(baseUrl, helper.getUrl());
        
		Assertions.assertTrue(isTheSameUrl);
	}
}