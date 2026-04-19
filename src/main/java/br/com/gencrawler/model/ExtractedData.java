package br.com.gencrawler.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "extracted_data")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractedData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private CrawlJob job;
    
    @Column(nullable = false)
    private String fieldName;
    
    @Column(columnDefinition = "TEXT")
    private String stringValue;
    
    private BigDecimal decimalValue;
    
    private String currency;
    
    @Column(nullable = false)
    private LocalDateTime extractedAt;
    
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
