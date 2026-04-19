package br.com.gencrawler.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlRequest {

    @NotBlank(message = "Source type is required")
    private String sourceType; // "web" | "spa" | "telegram" | "site"

    @NotBlank(message = "Source URL is required")
    private String sourceUrl;

    /**
     * Optional. When absent for sourceType="site", a default set of page fields is extracted
     * (title, h1, metaDescription, prices). Required for "web"/"spa"/"telegram".
     */
    @NotNull(message = "Extractors configuration is required (can be an empty list for sourceType=site)")
    private List<ExtractorConfig> extractors;

    private Map<String, String> headers;
    private Integer timeoutSeconds;
    private Boolean followRedirects;

    // --- Site crawl parameters (used when sourceType="site") ---
    /** Max number of pages to visit. Defaults to 50. */
    private Integer maxPages;
    /** Max link depth from the seed URL. Defaults to 3. */
    private Integer maxDepth;
    /** Restrict the crawl to the seed URL's host. Defaults to true. */
    private Boolean sameHost;
    /** Honor robots.txt Disallow directives. Defaults to true. */
    private Boolean respectRobotsTxt;
    /** Politeness delay in milliseconds between requests to the same host. Defaults to 500. */
    private Integer delayMs;
    /** Optional URL path prefix (regex) — only URLs whose path matches are enqueued. */
    private String pathPattern;
    /** Opt-in: skip TLS certificate validation. Defaults to true (validate). */
    private Boolean validateCertificates;

    // --- API extractor parameters (used when sourceType="api") ---
    /** HTTP method for API calls. Defaults to GET. */
    private String method;
    /** Request body (raw string, typically JSON). Only used for POST/PUT. */
    private String body;

    // --- Playwright (sourceType=spa) ---
    /**
     * Per-selector timeout in milliseconds for the SPA extractor. Defaults to 3000.
     * A missing selector (e.g. a page without an h1) will skip after this delay instead
     * of consuming the full page timeout.
     */
    private Integer selectorTimeoutMs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtractorConfig {
        @NotBlank(message = "Field name is required")
        private String fieldName;

        @NotBlank(message = "Selector is required")
        private String selector;

        private String regex;
        private String attribute;
        private Currency currency;
        private Boolean required;
    }

    public enum Currency {
        BRL, USD, EUR, GBP, JPY, AUD, CAD, CHF, CNY, ARS, CLP, COP, MXN, PEN, UYU
    }
}
