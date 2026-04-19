package br.com.gencrawler.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "crawl_jobs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlJob {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String sourceType;
    
    @Column(nullable = false)
    private String sourceUrl;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private JobStatus status;
    
    @Column(columnDefinition = "TEXT")
    private String requestConfig;
    
    @Column(columnDefinition = "TEXT")
    private String resultData;
    
    private String errorMessage;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime completedAt;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer retryCount = 0;
    
    public enum JobStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }
}
