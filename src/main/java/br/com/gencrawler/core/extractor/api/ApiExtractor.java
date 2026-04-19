package br.com.gencrawler.core.extractor.api;

import br.com.gencrawler.core.interfaces.DataExtractor;
import br.com.gencrawler.core.normalizer.PriceNormalizer;
import br.com.gencrawler.dto.CrawlRequest;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON API extractor — fetches a JSON endpoint and maps fields via JSONPath expressions.
 *
 * <p>Request config:
 * <ul>
 *   <li>{@code sourceType}: "api"</li>
 *   <li>{@code sourceUrl}: the JSON endpoint</li>
 *   <li>{@code method}: "GET" (default), "POST", "PUT"</li>
 *   <li>{@code body}: request body (typically JSON) for POST/PUT</li>
 *   <li>{@code headers}: HTTP headers (e.g., {@code User-Agent}, {@code Authorization})</li>
 *   <li>{@code extractors[].selector}: JSONPath expression (e.g., {@code $.items[*].name})</li>
 *   <li>{@code extractors[].currency}: if set, each matched value is passed to {@link PriceNormalizer}</li>
 * </ul>
 *
 * <p>If a selector returns a list with a single element, it's unwrapped.
 */
@Slf4j
public class ApiExtractor implements DataExtractor {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);
    private static final Configuration JSONPATH_CONFIG = Configuration.defaultConfiguration()
            .addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL, Option.SUPPRESS_EXCEPTIONS);

    @Override
    public boolean supports(String sourceType) {
        return "api".equalsIgnoreCase(sourceType);
    }

    @Override
    public int getPriority() {
        return 110;
    }

    @Override
    public Map<String, Object> extract(CrawlRequest request) throws Exception {
        Duration timeout = request.getTimeoutSeconds() != null
                ? Duration.ofSeconds(request.getTimeoutSeconds())
                : DEFAULT_TIMEOUT;

        HttpClient.Builder clientBuilder = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .followRedirects(Boolean.FALSE.equals(request.getFollowRedirects())
                        ? HttpClient.Redirect.NEVER : HttpClient.Redirect.NORMAL);
        if (Boolean.FALSE.equals(request.getValidateCertificates())) {
            clientBuilder.sslContext(trustAllSslContext());
        }
        HttpClient client = clientBuilder.build();

        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(request.getSourceUrl()))
                .timeout(timeout);

        String method = request.getMethod() == null ? "GET" : request.getMethod().toUpperCase();
        String body = request.getBody() == null ? "" : request.getBody();
        switch (method) {
            case "POST" -> reqBuilder.POST(HttpRequest.BodyPublishers.ofString(body));
            case "PUT" -> reqBuilder.PUT(HttpRequest.BodyPublishers.ofString(body));
            case "DELETE" -> reqBuilder.DELETE();
            default -> reqBuilder.GET();
        }

        Map<String, String> headers = request.getHeaders() != null ? request.getHeaders() : Map.of();
        if (!headers.containsKey("User-Agent") && !headers.containsKey("user-agent")) {
            reqBuilder.header("User-Agent", "Gen-Crawler/1.0");
        }
        headers.forEach(reqBuilder::header);

        log.info("API {} {}", method, request.getSourceUrl());
        HttpResponse<String> response = client.send(reqBuilder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IllegalStateException("API returned " + response.statusCode() + ": "
                    + truncate(response.body(), 200));
        }

        String trimmed = response.body().stripLeading();
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            throw new IllegalStateException(
                    "API did not return JSON (likely anti-bot page or HTML). First bytes: "
                            + truncate(trimmed, 200));
        }

        DocumentContext ctx = JsonPath.using(JSONPATH_CONFIG).parse(response.body());
        Map<String, Object> result = new HashMap<>();
        result.put("_statusCode", response.statusCode());

        if (request.getExtractors() == null || request.getExtractors().isEmpty()) {
            // no selectors → return the whole payload (small API probes)
            result.put("payload", ctx.read("$"));
            return result;
        }

        for (CrawlRequest.ExtractorConfig config : request.getExtractors()) {
            Object value = readJsonPath(ctx, config.getSelector());
            if (value instanceof List<?> list) {
                if (list.size() == 1) value = list.get(0);
                if (list.isEmpty()) value = null;
            }
            if (value != null && config.getCurrency() != null) {
                value = applyCurrency(value, config.getCurrency().name());
            }
            if (value != null || Boolean.TRUE.equals(config.getRequired())) {
                result.put(config.getFieldName(), value);
            }
        }

        return result;
    }

    private Object readJsonPath(DocumentContext ctx, String selector) {
        try {
            return ctx.read(selector);
        } catch (Exception e) {
            log.debug("JSONPath [{}] failed: {}", selector, e.getMessage());
            return null;
        }
    }

    private Object applyCurrency(Object value, String currencyCode) {
        if (value instanceof List<?> list) {
            List<Object> normalized = new java.util.ArrayList<>();
            for (Object v : list) normalized.add(applyCurrency(v, currencyCode));
            return normalized;
        }
        try {
            BigDecimal normalized = PriceNormalizer.normalize(String.valueOf(value), currencyCode);
            return normalized;
        } catch (Exception e) {
            return value;
        }
    }

    private SSLContext trustAllSslContext() throws Exception {
        TrustManager[] trustAll = {
                new X509TrustManager() {
                    @Override public void checkClientTrusted(X509Certificate[] c, String a) {}
                    @Override public void checkServerTrusted(X509Certificate[] c, String a) {}
                    @Override public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                }
        };
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, trustAll, new SecureRandom());
        HttpsURLConnection.setDefaultHostnameVerifier((h, s) -> true);
        return ctx;
    }

    private static String truncate(String s, int n) {
        if (s == null) return "";
        return s.length() <= n ? s : s.substring(0, n) + "…";
    }
}
