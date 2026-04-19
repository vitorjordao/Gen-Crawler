package br.com.gencrawler.service;

import br.com.gencrawler.dto.CrawlRequest;
import br.com.gencrawler.dto.CrawlResponse;
import br.com.gencrawler.model.CrawlJob;
import br.com.gencrawler.repository.CrawlJobRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CrawlerService {

    private final CrawlJobRepository jobRepository;
    private final CrawlJobProcessor processor;
    private final ObjectMapper objectMapper;

    public CrawlResponse submitCrawlJob(CrawlRequest request) {
        log.info("Submitting crawl job for URL: {}", request.getSourceUrl());

        CrawlJob job = CrawlJob.builder()
                .sourceType(request.getSourceType())
                .sourceUrl(request.getSourceUrl())
                .status(CrawlJob.JobStatus.PENDING)
                .requestConfig(serializeRequest(request))
                .createdAt(LocalDateTime.now())
                .retryCount(0)
                .build();

        CrawlJob saved = jobRepository.save(job);
        UUID jobId = saved.getId();
        log.info("Job saved with ID: {}", jobId);

        processor.process(jobId, request);

        return CrawlResponse.accepted(jobId.toString());
    }

    @Transactional(readOnly = true)
    public CrawlResponse getCrawlJobStatus(String jobId) {
        log.info("Fetching job with ID: {}", jobId);
        UUID uuid = UUID.fromString(jobId);
        CrawlJob job = jobRepository.findById(uuid)
                .orElseThrow(() -> new JobNotFoundException(jobId));

        Map<String, Object> data = null;
        if (job.getStatus() == CrawlJob.JobStatus.COMPLETED && job.getResultData() != null) {
            try {
                data = objectMapper.readValue(job.getResultData(), Map.class);
            } catch (Exception e) {
                log.error("Error parsing result data for job {}: {}", jobId, e.getMessage());
            }
        }

        return CrawlResponse.builder()
                .jobId(jobId)
                .status(job.getStatus().name())
                .message(job.getErrorMessage() != null
                        ? job.getErrorMessage()
                        : getStatusMessage(job.getStatus()))
                .createdAt(job.getCreatedAt())
                .completedAt(job.getCompletedAt())
                .data(data)
                .build();
    }

    private String serializeRequest(CrawlRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            log.error("Error serializing request: {}", e.getMessage());
            return "{}";
        }
    }

    private String getStatusMessage(CrawlJob.JobStatus status) {
        return switch (status) {
            case PENDING -> "Job is pending";
            case PROCESSING -> "Job is being processed";
            case COMPLETED -> "Job completed successfully";
            case FAILED -> "Job failed";
        };
    }
}
