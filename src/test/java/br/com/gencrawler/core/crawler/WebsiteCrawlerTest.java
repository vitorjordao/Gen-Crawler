package br.com.gencrawler.core.crawler;

import br.com.gencrawler.dto.CrawlRequest;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

class WebsiteCrawlerTest {

    private WireMockServer wireMock;
    private WebsiteCrawler crawler;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        configureFor("localhost", wireMock.port());
        crawler = new WebsiteCrawler();
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void supportsSiteSourceType() {
        assertTrue(crawler.supports("site"));
        assertTrue(crawler.supports("SITE"));
        assertFalse(crawler.supports("web"));
    }

    @Test
    void discoversLinksAndRespectsMaxPages() throws Exception {
        stubFor(get("/robots.txt").willReturn(aResponse().withStatus(404)));
        stubFor(get("/").willReturn(ok(
                "<html><head><title>Home</title></head><body>" +
                        "<h1>Welcome</h1>" +
                        "<a href='/a'>A</a><a href='/b'>B</a><a href='/c'>C</a>" +
                        "</body></html>").withHeader("Content-Type", "text/html")));
        stubFor(get("/a").willReturn(ok(
                "<html><head><title>A page</title></head><body>" +
                        "<a href='/d'>D</a></body></html>").withHeader("Content-Type", "text/html")));
        stubFor(get("/b").willReturn(ok(
                "<html><head><title>B page</title></head><body></body></html>")
                .withHeader("Content-Type", "text/html")));
        stubFor(get("/c").willReturn(ok(
                "<html><head><title>C page</title></head><body></body></html>")
                .withHeader("Content-Type", "text/html")));
        stubFor(get("/d").willReturn(ok(
                "<html><head><title>D page</title></head><body></body></html>")
                .withHeader("Content-Type", "text/html")));

        CrawlRequest request = CrawlRequest.builder()
                .sourceType("site")
                .sourceUrl(wireMock.baseUrl() + "/")
                .extractors(Collections.emptyList())
                .maxPages(3)
                .maxDepth(5)
                .delayMs(0)
                .respectRobotsTxt(false)
                .build();

        @SuppressWarnings("unchecked")
        Map<String, Object> result = crawler.extract(request);

        assertEquals(3, result.get("visitedCount"));
        assertEquals("localhost", result.get("host"));
        List<Map<String, Object>> pages = (List<Map<String, Object>>) result.get("pages");
        assertEquals(3, pages.size());
        assertEquals(wireMock.baseUrl() + "/", pages.get(0).get("url"));
        assertEquals(0, pages.get(0).get("depth"));
    }

    @Test
    void respectsMaxDepth() throws Exception {
        stubFor(get("/").willReturn(ok(
                "<html><head><title>Root</title></head><body>" +
                        "<a href='/level1'>L1</a></body></html>")
                .withHeader("Content-Type", "text/html")));
        stubFor(get("/level1").willReturn(ok(
                "<html><head><title>L1</title></head><body>" +
                        "<a href='/level2'>L2</a></body></html>")
                .withHeader("Content-Type", "text/html")));
        stubFor(get("/level2").willReturn(ok(
                "<html><head><title>L2</title></head></html>")
                .withHeader("Content-Type", "text/html")));

        CrawlRequest request = CrawlRequest.builder()
                .sourceType("site")
                .sourceUrl(wireMock.baseUrl() + "/")
                .extractors(Collections.emptyList())
                .maxPages(10)
                .maxDepth(1)
                .delayMs(0)
                .respectRobotsTxt(false)
                .build();

        @SuppressWarnings("unchecked")
        Map<String, Object> result = crawler.extract(request);

        // depth=0 (root) and depth=1 (level1) only; level2 is not fetched
        assertEquals(2, result.get("visitedCount"));
    }

    @Test
    void doesNotCrossHostWhenSameHostIsTrue() throws Exception {
        stubFor(get("/").willReturn(ok(
                "<html><head><title>Home</title></head><body>" +
                        "<a href='https://external.example.com/page'>external</a>" +
                        "<a href='/inner'>inner</a></body></html>")
                .withHeader("Content-Type", "text/html")));
        stubFor(get("/inner").willReturn(ok(
                "<html><head><title>Inner</title></head></html>")
                .withHeader("Content-Type", "text/html")));

        CrawlRequest request = CrawlRequest.builder()
                .sourceType("site")
                .sourceUrl(wireMock.baseUrl() + "/")
                .extractors(Collections.emptyList())
                .maxPages(10)
                .maxDepth(3)
                .delayMs(0)
                .respectRobotsTxt(false)
                .sameHost(true)
                .build();

        Map<String, Object> result = crawler.extract(request);

        assertEquals(2, result.get("visitedCount"));
    }

    @Test
    void extractsDefaultFieldsWhenNoExtractorsConfigured() throws Exception {
        stubFor(get("/").willReturn(ok(
                "<html><head>" +
                        "<title>Home Page</title>" +
                        "<meta name='description' content='An example site'>" +
                        "</head><body>" +
                        "<h1>Welcome</h1>" +
                        "<p>Preço: R$ 1.299,00 ou USD 250.00</p>" +
                        "</body></html>")
                .withHeader("Content-Type", "text/html")));

        CrawlRequest request = CrawlRequest.builder()
                .sourceType("site")
                .sourceUrl(wireMock.baseUrl() + "/")
                .extractors(Collections.emptyList())
                .maxPages(1)
                .maxDepth(0)
                .delayMs(0)
                .respectRobotsTxt(false)
                .build();

        @SuppressWarnings("unchecked")
        Map<String, Object> result = crawler.extract(request);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> pages = (List<Map<String, Object>>) result.get("pages");
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) pages.get(0).get("data");

        assertEquals("Home Page", data.get("title"));
        assertEquals("Welcome", data.get("h1"));
        assertEquals("An example site", data.get("metaDescription"));
        @SuppressWarnings("unchecked")
        List<String> prices = (List<String>) data.get("prices");
        assertNotNull(prices);
        assertTrue(prices.stream().anyMatch(p -> p.contains("1.299,00")));
    }

    @Test
    void respectsRobotsTxtDisallow() throws Exception {
        stubFor(get("/robots.txt").willReturn(ok("User-agent: *\nDisallow: /private")));
        stubFor(get("/").willReturn(ok(
                "<html><head><title>Home</title></head><body>" +
                        "<a href='/public'>public</a>" +
                        "<a href='/private/secret'>private</a></body></html>")
                .withHeader("Content-Type", "text/html")));
        stubFor(get("/public").willReturn(ok(
                "<html><head><title>Public</title></head></html>")
                .withHeader("Content-Type", "text/html")));
        stubFor(get("/private/secret").willReturn(ok(
                "<html><head><title>Should not visit</title></head></html>")
                .withHeader("Content-Type", "text/html")));

        CrawlRequest request = CrawlRequest.builder()
                .sourceType("site")
                .sourceUrl(wireMock.baseUrl() + "/")
                .extractors(Collections.emptyList())
                .maxPages(10)
                .maxDepth(3)
                .delayMs(0)
                .respectRobotsTxt(true)
                .build();

        Map<String, Object> result = crawler.extract(request);

        assertEquals(2, result.get("visitedCount"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> pages = (List<Map<String, Object>>) result.get("pages");
        assertTrue(pages.stream().noneMatch(p -> ((String) p.get("url")).contains("/private")));
    }

    @Test
    void pathPatternFiltersEnqueuedLinks() throws Exception {
        stubFor(get("/").willReturn(ok(
                "<html><head><title>Home</title></head><body>" +
                        "<a href='/blog/post-1'>a</a>" +
                        "<a href='/about'>b</a>" +
                        "<a href='/blog/post-2'>c</a></body></html>")
                .withHeader("Content-Type", "text/html")));
        stubFor(get("/blog/post-1").willReturn(ok(
                "<html><head><title>p1</title></head></html>")
                .withHeader("Content-Type", "text/html")));
        stubFor(get("/blog/post-2").willReturn(ok(
                "<html><head><title>p2</title></head></html>")
                .withHeader("Content-Type", "text/html")));
        stubFor(get("/about").willReturn(ok(
                "<html><head><title>about</title></head></html>")
                .withHeader("Content-Type", "text/html")));

        CrawlRequest request = CrawlRequest.builder()
                .sourceType("site")
                .sourceUrl(wireMock.baseUrl() + "/")
                .extractors(Collections.emptyList())
                .maxPages(10)
                .maxDepth(2)
                .delayMs(0)
                .respectRobotsTxt(false)
                .pathPattern("^/blog/")
                .build();

        Map<String, Object> result = crawler.extract(request);

        // seed (/) + 2 blog posts; /about filtered out
        assertEquals(3, result.get("visitedCount"));
    }
}
