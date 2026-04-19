package br.com.gencrawler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlResponse {
    
    private String jobId;
    private String status; // "ACCEPTED", "PROCESSING", "COMPLETED", "FAILED"
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private Map<String, Object> data;
    
    public static CrawlResponse accepted(String jobId) {
        return CrawlResponse.builder()
                .jobId(jobId)
                .status("ACCEPTED")
                .message("Crawl job accepted and is being processed")
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    public static CrawlResponse processing(String jobId) {
        return CrawlResponse.builder()
                .jobId(jobId)
                .status("PROCESSING")
                .message("Crawl job is being processed")
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    public static CrawlResponse completed(String jobId, Map<String, Object> data) {
        return CrawlResponse.builder()
                .jobId(jobId)
                .status("COMPLETED")
                .message("Crawl job completed successfully")
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .data(data)
                .build();
    }
    
    public static CrawlResponse failed(String jobId, String message) {
        return CrawlResponse.builder()
                .jobId(jobId)
                .status("FAILED")
                .message(message)
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();
    }
}
