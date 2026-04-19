package br.com.gencrawler.core.interfaces;

import br.com.gencrawler.dto.CrawlRequest;
import java.util.Map;

/**
 * Interface for data extractors. Implementations can be added via SPI.
 */
public interface DataExtractor {
    
    /**
     * Check if this extractor can handle the given source type.
     */
    boolean supports(String sourceType);
    
    /**
     * Extract data from the source based on the request configuration.
     */
    Map<String, Object> extract(CrawlRequest request) throws Exception;
    
    /**
     * Get the priority of this extractor (higher = more preferred).
     */
    int getPriority();
}
