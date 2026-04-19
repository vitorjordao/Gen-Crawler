package br.com.gencrawler.core.crawler;

import br.com.gencrawler.core.interfaces.DataExtractor;
import br.com.gencrawler.core.normalizer.PriceNormalizer;
import br.com.gencrawler.dto.CrawlRequest;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Website crawler (sourceType="site") — BFS from a seed URL, stays within host by default,
 * respects robots.txt, enforces max pages/depth and a politeness delay.
 *
 * <p>If the request has no ExtractorConfig entries, a default set is extracted from each page:
 * title, h1, metaDescription, and prices detected via regex.
 *
 * <p>Result shape:
 * <pre>{@code
 * {
 *   "seed": "https://example.com",
 *   "host": "example.com",
 *   "visitedCount": 12,
 *   "pages": [
 *     { "url": "...", "depth": 0, "data": { "title": "...", "prices": ["R$ 99,00", ...] } },
 *     ...
 *   ]
 * }
 * }</pre>
 */
@Slf4j
public class WebsiteCrawler implements DataExtractor {

    private static final int DEFAULT_MAX_PAGES = 50;
    private static final int DEFAULT_MAX_DEPTH = 3;
    private static final int DEFAULT_DELAY_MS = 500;
    private static final int DEFAULT_TIMEOUT_MS = 10_000;
    private static final String USER_AGENT = "Gen-Crawler/1.0";

    @Override
    public boolean supports(String sourceType) {
        return "site".equalsIgnoreCase(sourceType);
    }

    @Override
    public int getPriority() {
        return 95;
    }

    @Override
    public Map<String, Object> extract(CrawlRequest request) throws Exception {
        URI seed = new URI(request.getSourceUrl()).normalize();
        if (seed.getHost() == null) {
            throw new IllegalArgumentException("sourceUrl must be an absolute URL with a host: " + request.getSourceUrl());
        }

        int maxPages = or(request.getMaxPages(), DEFAULT_MAX_PAGES);
        int maxDepth = or(request.getMaxDepth(), DEFAULT_MAX_DEPTH);
        int delayMs = or(request.getDelayMs(), DEFAULT_DELAY_MS);
        int timeoutMs = request.getTimeoutSeconds() != null
                ? request.getTimeoutSeconds() * 1000
                : DEFAULT_TIMEOUT_MS;
        boolean sameHost = request.getSameHost() == null || request.getSameHost();
        boolean respectRobots = request.getRespectRobotsTxt() == null || request.getRespectRobotsTxt();
        Pattern pathPattern = (request.getPathPattern() != null && !request.getPathPattern().isBlank())
                ? Pattern.compile(request.getPathPattern())
                : null;

        boolean validateCerts = request.getValidateCertificates() == null || request.getValidateCertificates();
        javax.net.ssl.SSLSocketFactory sslFactory = validateCerts ? null : InsecureTrust.trustAllSocketFactory();

        RobotsTxtChecker robots = respectRobots ? new RobotsTxtChecker(timeoutMs, sslFactory) : null;

        Set<String> visited = new HashSet<>();
        Deque<CrawlTarget> queue = new ArrayDeque<>();
        queue.add(new CrawlTarget(seed, 0));

        List<Map<String, Object>> pages = new ArrayList<>();

        while (!queue.isEmpty() && pages.size() < maxPages) {
            CrawlTarget target = queue.poll();
            String key = normalizeKey(target.url);
            if (!visited.add(key)) continue;
            if (target.depth > maxDepth) continue;
            if (robots != null && !robots.isAllowed(target.url)) {
                log.debug("Skipping (robots.txt): {}", target.url);
                continue;
            }

            Document doc;
            try {
                var conn = Jsoup.connect(target.url.toString())
                        .userAgent(USER_AGENT)
                        .timeout(timeoutMs)
                        .followRedirects(request.getFollowRedirects() == null || request.getFollowRedirects())
                        .headers(request.getHeaders() != null ? request.getHeaders() : Map.of());
                if (sslFactory != null) conn.sslSocketFactory(sslFactory);
                doc = conn.get();
            } catch (Exception e) {
                log.warn("Failed to fetch {}: {}", target.url, e.getMessage());
                continue;
            }

            Map<String, Object> pageData = (request.getExtractors() == null || request.getExtractors().isEmpty())
                    ? extractDefaults(doc)
                    : extractConfigured(doc, request.getExtractors());

            Map<String, Object> pageEntry = new LinkedHashMap<>();
            pageEntry.put("url", target.url.toString());
            pageEntry.put("depth", target.depth);
            pageEntry.put("data", pageData);
            pages.add(pageEntry);

            if (target.depth < maxDepth) {
                enqueueLinks(doc, target, seed, sameHost, pathPattern, visited, queue);
            }

            if (delayMs > 0) {
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("seed", seed.toString());
        result.put("host", seed.getHost());
        result.put("visitedCount", pages.size());
        result.put("pages", pages);
        log.info("Crawl finished: seed={} visited={} (maxPages={}, maxDepth={})",
                seed, pages.size(), maxPages, maxDepth);
        return result;
    }

    private Map<String, Object> extractDefaults(Document doc) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("title", doc.title());

        Element h1 = doc.selectFirst("h1");
        if (h1 != null) data.put("h1", h1.text());

        Element meta = doc.selectFirst("meta[name=description]");
        if (meta != null) data.put("metaDescription", meta.attr("content"));

        List<String> prices = new ArrayList<>();
        for (String line : doc.text().split("\\s+")) {
            String price = PriceNormalizer.extractPrice(line);
            if (price != null && !prices.contains(price)) prices.add(price);
            if (prices.size() >= 10) break;
        }
        if (!prices.isEmpty()) data.put("prices", prices);

        return data;
    }

    private Map<String, Object> extractConfigured(Document doc, List<CrawlRequest.ExtractorConfig> configs) {
        Map<String, Object> data = new LinkedHashMap<>();
        for (CrawlRequest.ExtractorConfig config : configs) {
            try {
                Object value = extractField(doc, config);
                if (value != null) data.put(config.getFieldName(), value);
            } catch (Exception e) {
                log.debug("Field {} extraction failed: {}", config.getFieldName(), e.getMessage());
            }
        }
        return data;
    }

    private Object extractField(Document doc, CrawlRequest.ExtractorConfig config) {
        Elements elements = doc.select(config.getSelector());
        if (elements.isEmpty()) return null;
        Element element = elements.first();

        String value = (config.getAttribute() == null || "text".equalsIgnoreCase(config.getAttribute()))
                ? element.text()
                : element.attr(config.getAttribute());

        if (config.getRegex() != null && !config.getRegex().isBlank()) {
            var matcher = Pattern.compile(config.getRegex()).matcher(value);
            value = matcher.find() ? matcher.group() : null;
        }
        if (value == null) return null;

        if (config.getCurrency() != null) {
            try {
                BigDecimal normalized = PriceNormalizer.normalize(value, config.getCurrency().name());
                return normalized;
            } catch (Exception e) {
                log.debug("Failed to normalize price '{}': {}", value, e.getMessage());
                return value;
            }
        }
        return value;
    }

    private void enqueueLinks(Document doc, CrawlTarget parent, URI seed, boolean sameHost,
                              Pattern pathPattern, Set<String> visited, Deque<CrawlTarget> queue) {
        for (Element link : doc.select("a[href]")) {
            String href = link.absUrl("href");
            if (href.isEmpty()) continue;
            URI next;
            try {
                next = new URI(href).normalize();
            } catch (URISyntaxException e) {
                continue;
            }
            if (next.getScheme() == null || (!next.getScheme().equals("http") && !next.getScheme().equals("https"))) continue;
            if (sameHost && !seed.getHost().equalsIgnoreCase(next.getHost())) continue;
            if (pathPattern != null && (next.getPath() == null || !pathPattern.matcher(next.getPath()).find())) continue;
            if (visited.contains(normalizeKey(next))) continue;
            queue.add(new CrawlTarget(next, parent.depth + 1));
        }
    }

    private static String normalizeKey(URI uri) {
        String path = uri.getRawPath() == null || uri.getRawPath().isEmpty() ? "/" : uri.getRawPath();
        String query = uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery();
        return uri.getScheme() + "://" + uri.getAuthority().toLowerCase() + path + query;
    }

    private static int or(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private record CrawlTarget(URI url, int depth) {}
}
