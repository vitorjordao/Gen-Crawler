package br.com.gencrawler.service;

import br.com.gencrawler.core.interfaces.DataExtractor;
import br.com.gencrawler.dto.CrawlRequest;
import br.com.gencrawler.model.CrawlJob;
import br.com.gencrawler.model.ExtractedData;
import br.com.gencrawler.repository.CrawlJobRepository;
import br.com.gencrawler.repository.ExtractedDataRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CrawlJobProcessor {

    private final List<DataExtractor> extractors;
    private final CrawlJobRepository jobRepository;
    private final ExtractedDataRepository dataRepository;
    private final ObjectMapper objectMapper;

    @Async
    @Transactional
    public void process(UUID jobId, CrawlRequest request) {
        CrawlJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException(jobId.toString()));

        try {
            job.setStatus(CrawlJob.JobStatus.PROCESSING);
            jobRepository.save(job);

            DataExtractor extractor = extractors.stream()
                    .filter(e -> e.supports(request.getSourceType()))
                    .max((e1, e2) -> Integer.compare(e1.getPriority(), e2.getPriority()))
                    .orElseThrow(() -> new IllegalStateException(
                            "No extractor found for source type: " + request.getSourceType()));

            Map<String, Object> data = extractor.extract(request);

            saveExtractedData(job, data);

            job.setStatus(CrawlJob.JobStatus.COMPLETED);
            job.setResultData(serializeData(data));
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);

            log.info("Crawl job {} completed successfully", jobId);
        } catch (Exception e) {
            log.error("Crawl job {} failed: {}", jobId, e.getMessage(), e);
            job.setStatus(CrawlJob.JobStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            job.setCompletedAt(LocalDateTime.now());
            job.setRetryCount(job.getRetryCount() + 1);
            jobRepository.save(job);
        }
    }

    private void saveExtractedData(CrawlJob job, Map<String, Object> data) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            Object value = entry.getValue();
            String stringValue = serializeValue(value);
            BigDecimal decimalValue = (value instanceof Number n) ? new BigDecimal(n.toString()) : null;

            ExtractedData extractedData = ExtractedData.builder()
                    .job(job)
                    .fieldName(entry.getKey())
                    .stringValue(stringValue)
                    .decimalValue(decimalValue)
                    .extractedAt(LocalDateTime.now())
                    .build();
            dataRepository.save(extractedData);
        }
    }

    private String serializeValue(Object value) {
        if (value == null) return null;
        if (value instanceof CharSequence || value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            log.warn("Could not serialize value as JSON: {}", e.getMessage());
            return value.toString();
        }
    }

    private String serializeData(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("Error serializing data: {}", e.getMessage());
            return "{}";
        }
    }
}
