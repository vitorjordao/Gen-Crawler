package br.com.gencrawler.core.extractor.web;

import br.com.gencrawler.core.interfaces.DataExtractor;
import br.com.gencrawler.core.normalizer.PriceNormalizer;
import br.com.gencrawler.dto.CrawlRequest;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Web extractor for dynamic SPA pages using Playwright.
 */
@Slf4j
public class PlaywrightWebExtractor implements DataExtractor {

    private static final int DEFAULT_TIMEOUT_MS = 15_000;
    private static final int DEFAULT_SELECTOR_TIMEOUT_MS = 3_000;

    @Override
    public boolean supports(String sourceType) {
        return "spa".equalsIgnoreCase(sourceType) || "dynamic".equalsIgnoreCase(sourceType);
    }

    @Override
    public Map<String, Object> extract(CrawlRequest request) throws Exception {
        log.info("Extracting data from SPA URL: {}", request.getSourceUrl());

        int timeout = request.getTimeoutSeconds() != null
                ? request.getTimeoutSeconds() * 1000
                : DEFAULT_TIMEOUT_MS;
        int selectorTimeout = request.getSelectorTimeoutMs() != null
                ? request.getSelectorTimeoutMs()
                : DEFAULT_SELECTOR_TIMEOUT_MS;

        Map<String, Object> result = new HashMap<>();

        try (Playwright pw = Playwright.create();
             Browser browser = pw.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
             BrowserContext context = browser.newContext()) {

            Page page = context.newPage();
            page.setDefaultTimeout(timeout);
            page.navigate(request.getSourceUrl());
            page.waitForLoadState(LoadState.NETWORKIDLE);

            for (CrawlRequest.ExtractorConfig config : request.getExtractors()) {
                try {
                    Object value = extractField(page, config, selectorTimeout);
                    if (value != null || (config.getRequired() != null && config.getRequired())) {
                        result.put(config.getFieldName(), value);
                    }
                } catch (Exception e) {
                    log.warn("Field {} extraction failed: {}", config.getFieldName(), e.getMessage());
                    if (config.getRequired() != null && config.getRequired()) {
                        throw e;
                    }
                }
            }
        }

        log.info("Extracted {} fields from SPA", result.size());
        return result;
    }

    @Override
    public int getPriority() {
        return 90;
    }

    private Object extractField(Page page, CrawlRequest.ExtractorConfig config, int selectorTimeoutMs) {
        Locator loc = page.locator(config.getSelector()).first();
        try {
            loc.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.ATTACHED)
                    .setTimeout(selectorTimeoutMs));
        } catch (TimeoutError e) {
            if (config.getRequired() != null && config.getRequired()) {
                throw new IllegalStateException("No element found for selector: " + config.getSelector());
            }
            return null;
        }

        String value;
        if (config.getAttribute() == null || "text".equalsIgnoreCase(config.getAttribute())) {
            value = loc.textContent(new Locator.TextContentOptions().setTimeout(selectorTimeoutMs));
        } else {
            value = loc.getAttribute(config.getAttribute(),
                    new Locator.GetAttributeOptions().setTimeout(selectorTimeoutMs));
        }

        if (value == null) {
            if (config.getRequired() != null && config.getRequired()) {
                throw new IllegalStateException("No element found for selector: " + config.getSelector());
            }
            return null;
        }

        if (config.getRegex() != null && !config.getRegex().isBlank()) {
            var matcher = java.util.regex.Pattern.compile(config.getRegex()).matcher(value);
            if (matcher.find()) {
                value = matcher.group();
            } else {
                if (config.getRequired() != null && config.getRequired()) {
                    throw new IllegalStateException("Regex match failed for: " + value);
                }
                return null;
            }
        }

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
