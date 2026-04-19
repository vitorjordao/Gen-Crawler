package br.com.gencrawler.core.extractor.web;

import br.com.gencrawler.dto.CrawlRequest;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

class JsoupWebExtractorTest {

    private WireMockServer wireMock;
    private JsoupWebExtractor extractor;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        configureFor("localhost", wireMock.port());
        extractor = new JsoupWebExtractor();
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void supportsWebSourceType() {
        assertTrue(extractor.supports("web"));
        assertTrue(extractor.supports("WEB"));
        assertFalse(extractor.supports("telegram"));
    }

    @Test
    void extractsTextAndHrefFromStaticHtml() throws Exception {
        stubFor(get("/").willReturn(ok(
                "<html><body>" +
                        "<h1>Hello</h1>" +
                        "<a href='/next'>link</a>" +
                        "</body></html>").withHeader("Content-Type", "text/html")));

        CrawlRequest request = CrawlRequest.builder()
                .sourceType("web")
                .sourceUrl(wireMock.baseUrl() + "/")
                .extractors(List.of(
                        CrawlRequest.ExtractorConfig.builder().fieldName("title").selector("h1").build(),
                        CrawlRequest.ExtractorConfig.builder().fieldName("link").selector("a").attribute("href").build()
                ))
                .build();

        Map<String, Object> result = extractor.extract(request);

        assertEquals("Hello", result.get("title"));
        assertEquals("/next", result.get("link"));
    }

    @Test
    void normalizesPriceWhenCurrencyConfigured() throws Exception {
        stubFor(get("/price").willReturn(ok(
                "<html><body><span class='price'>R$ 1.299,00</span></body></html>")
                .withHeader("Content-Type", "text/html")));

        CrawlRequest request = CrawlRequest.builder()
                .sourceType("web")
                .sourceUrl(wireMock.baseUrl() + "/price")
                .extractors(List.of(CrawlRequest.ExtractorConfig.builder()
                        .fieldName("price")
                        .selector(".price")
                        .currency(CrawlRequest.Currency.BRL)
                        .build()))
                .build();

        Map<String, Object> result = extractor.extract(request);

        assertEquals(new BigDecimal("1299.00"), result.get("price"));
    }

    @Test
    void skipsMissingOptionalFieldAndIncludesRequiredMisses() throws Exception {
        stubFor(get("/missing").willReturn(ok(
                "<html><body><h1>Hello</h1></body></html>")
                .withHeader("Content-Type", "text/html")));

        CrawlRequest request = CrawlRequest.builder()
                .sourceType("web")
                .sourceUrl(wireMock.baseUrl() + "/missing")
                .extractors(List.of(CrawlRequest.ExtractorConfig.builder()
                        .fieldName("subtitle")
                        .selector("h2")
                        .build()))
                .build();

        Map<String, Object> result = extractor.extract(request);
        assertFalse(result.containsKey("subtitle"));
    }

    @Test
    void throwsWhenRequiredSelectorHasNoMatch() {
        stubFor(get("/noreq").willReturn(ok(
                "<html><body><h1>Hi</h1></body></html>")
                .withHeader("Content-Type", "text/html")));

        CrawlRequest request = CrawlRequest.builder()
                .sourceType("web")
                .sourceUrl(wireMock.baseUrl() + "/noreq")
                .extractors(List.of(CrawlRequest.ExtractorConfig.builder()
                        .fieldName("missing")
                        .selector("article.does-not-exist")
                        .required(true)
                        .build()))
                .build();

        assertThrows(Exception.class, () -> extractor.extract(request));
    }
}
