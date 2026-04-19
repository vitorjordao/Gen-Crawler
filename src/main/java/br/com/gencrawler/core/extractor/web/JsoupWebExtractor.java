package br.com.gencrawler.core.extractor.web;

import br.com.gencrawler.core.crawler.InsecureTrust;
import br.com.gencrawler.core.interfaces.DataExtractor;
import br.com.gencrawler.core.normalizer.PriceNormalizer;
import br.com.gencrawler.dto.CrawlRequest;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Web extractor for static HTML pages using Jsoup.
 */
@Slf4j
public class JsoupWebExtractor implements DataExtractor {
    
    private static final int DEFAULT_TIMEOUT = 10000;
    
    @Override
    public boolean supports(String sourceType) {
        return "web".equalsIgnoreCase(sourceType);
    }
    
    @Override
    public Map<String, Object> extract(CrawlRequest request) throws Exception {
        log.info("Extracting data from URL: {}", request.getSourceUrl());
        
        Map<String, Object> result = new HashMap<>();
        
        var conn = Jsoup.connect(request.getSourceUrl())
                .timeout(request.getTimeoutSeconds() != null ? request.getTimeoutSeconds() * 1000 : DEFAULT_TIMEOUT)
                .followRedirects(request.getFollowRedirects() != null ? request.getFollowRedirects() : true)
                .headers(request.getHeaders() != null ? request.getHeaders() : Map.of());
        if (Boolean.FALSE.equals(request.getValidateCertificates())) {
            conn.sslSocketFactory(InsecureTrust.trustAllSocketFactory());
        }
        Document document = conn.get();
        
        // Extract data using configured extractors
        for (CrawlRequest.ExtractorConfig config : request.getExtractors()) {
            try {
                Object value = extractField(document, config);
                if (value != null || (config.getRequired() != null && config.getRequired())) {
                    result.put(config.getFieldName(), value);
                }
            } catch (Exception e) {
                log.error("Error extracting field {}: {}", config.getFieldName(), e.getMessage());
                if (config.getRequired() != null && config.getRequired()) {
                    throw e;
                }
            }
        }
        
        log.info("Extracted {} fields", result.size());
        return result;
    }
    
    @Override
    public int getPriority() {
        return 100;
    }
    
    private Object extractField(Document document, CrawlRequest.ExtractorConfig config) throws Exception {
        Elements elements = document.select(config.getSelector());
        
        if (elements.isEmpty()) {
            if (config.getRequired() != null && config.getRequired()) {
                throw new RuntimeException("No elements found for selector: " + config.getSelector());
            }
            return null;
        }
        
        Element element = elements.first();
        String value;
        
        // Get value based on attribute type
        if (config.getAttribute() == null || "text".equalsIgnoreCase(config.getAttribute())) {
            value = element.text();
        } else {
            value = element.attr(config.getAttribute());
        }
        
        // Apply regex if configured
        if (config.getRegex() != null && !config.getRegex().isBlank()) {
            var matcher = java.util.regex.Pattern.compile(config.getRegex()).matcher(value);
            if (matcher.find()) {
                value = matcher.group();
            } else {
                if (config.getRequired() != null && config.getRequired()) {
                    throw new RuntimeException("Regex match failed for: " + value);
                }
                return null;
            }
        }
        
        // Normalize to currency if configured
        if (config.getCurrency() != null) {
            try {
                BigDecimal normalizedPrice = PriceNormalizer.normalize(value, config.getCurrency().name());
                return normalizedPrice;
            } catch (Exception e) {
                log.warn("Failed to normalize price '{}': {}", value, e.getMessage());
                return value;
            }
        }
        
        return value;
    }
}
