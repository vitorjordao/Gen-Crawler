package br.com.gencrawler.core.extractor.api;

import br.com.gencrawler.dto.CrawlRequest;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

class ApiExtractorTest {

    private WireMockServer wireMock;
    private ApiExtractor extractor;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        configureFor("localhost", wireMock.port());
        extractor = new ApiExtractor();
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void supportsApiSourceType() {
        assertTrue(extractor.supports("api"));
        assertTrue(extractor.supports("API"));
        assertFalse(extractor.supports("web"));
    }

    @Test
    void extractsSimpleField() throws Exception {
        stubFor(get("/api/item/1").willReturn(okJson("{\"id\":1,\"name\":\"Sword\",\"price\":42.5}")));

        CrawlRequest req = CrawlRequest.builder()
                .sourceType("api")
                .sourceUrl(wireMock.baseUrl() + "/api/item/1")
                .extractors(List.of(
                        CrawlRequest.ExtractorConfig.builder().fieldName("name").selector("$.name").build(),
                        CrawlRequest.ExtractorConfig.builder().fieldName("price").selector("$.price").build()))
                .build();

        Map<String, Object> result = extractor.extract(req);
        assertEquals("Sword", result.get("name"));
        assertEquals(42.5, ((Number) result.get("price")).doubleValue());
        assertEquals(200, result.get("_statusCode"));
    }

    @Test
    void extractsListWithWildcard() throws Exception {
        stubFor(get("/api/search").willReturn(okJson(
                "{\"items\":[{\"name\":\"A\",\"price\":10},{\"name\":\"B\",\"price\":20}]}")));

        CrawlRequest req = CrawlRequest.builder()
                .sourceType("api")
                .sourceUrl(wireMock.baseUrl() + "/api/search")
                .extractors(List.of(
                        CrawlRequest.ExtractorConfig.builder().fieldName("names").selector("$.items[*].name").build()))
                .build();

        Map<String, Object> result = extractor.extract(req);
        @SuppressWarnings("unchecked")
        List<String> names = (List<String>) result.get("names");
        assertEquals(List.of("A", "B"), names);
    }

    @Test
    void postWithJsonBody() throws Exception {
        stubFor(post("/api/graphql")
                .withRequestBody(containing("myQuery"))
                .willReturn(okJson("{\"data\":{\"result\":\"ok\"}}")));

        CrawlRequest req = CrawlRequest.builder()
                .sourceType("api")
                .sourceUrl(wireMock.baseUrl() + "/api/graphql")
                .method("POST")
                .body("{\"query\":\"myQuery\"}")
                .headers(Map.of("Content-Type", "application/json"))
                .extractors(List.of(
                        CrawlRequest.ExtractorConfig.builder().fieldName("r").selector("$.data.result").build()))
                .build();

        Map<String, Object> result = extractor.extract(req);
        assertEquals("ok", result.get("r"));
    }

    @Test
    void throwsOnNon2xxStatus() {
        stubFor(get("/api/broken").willReturn(aResponse().withStatus(500).withBody("boom")));

        CrawlRequest req = CrawlRequest.builder()
                .sourceType("api")
                .sourceUrl(wireMock.baseUrl() + "/api/broken")
                .extractors(List.of(
                        CrawlRequest.ExtractorConfig.builder().fieldName("x").selector("$.x").build()))
                .build();

        assertThrows(IllegalStateException.class, () -> extractor.extract(req));
    }

    @Test
    void missingFieldReturnsNull() throws Exception {
        stubFor(get("/api/one").willReturn(okJson("{\"a\":1}")));

        CrawlRequest req = CrawlRequest.builder()
                .sourceType("api")
                .sourceUrl(wireMock.baseUrl() + "/api/one")
                .extractors(List.of(
                        CrawlRequest.ExtractorConfig.builder().fieldName("missing").selector("$.nope").build()))
                .build();

        Map<String, Object> result = extractor.extract(req);
        assertFalse(result.containsKey("missing"));
    }

    @Test
    void returnsWholePayloadWhenNoExtractors() throws Exception {
        stubFor(get("/api/info").willReturn(okJson("{\"version\":\"1.0\"}")));

        CrawlRequest req = CrawlRequest.builder()
                .sourceType("api")
                .sourceUrl(wireMock.baseUrl() + "/api/info")
                .extractors(List.of())
                .build();

        Map<String, Object> result = extractor.extract(req);
        assertNotNull(result.get("payload"));
    }
}
