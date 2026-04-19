package br.com.gencrawler.service;

import br.com.gencrawler.core.interfaces.DataExtractor;
import br.com.gencrawler.dto.CrawlRequest;
import br.com.gencrawler.dto.CrawlResponse;
import br.com.gencrawler.model.CrawlJob;
import br.com.gencrawler.repository.CrawlJobRepository;
import br.com.gencrawler.repository.ExtractedDataRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class CrawlerServiceTest {

    @Autowired
    private CrawlerService crawlerService;

    @Autowired
    private CrawlJobRepository jobRepository;

    @Autowired
    private ExtractedDataRepository dataRepository;

    @MockBean(name = "jsoupWebExtractor")
    private DataExtractor jsoupWebExtractor;

    @BeforeEach
    void setUp() throws Exception {
        dataRepository.deleteAll();
        jobRepository.deleteAll();
        when(jsoupWebExtractor.supports("web")).thenReturn(true);
        when(jsoupWebExtractor.getPriority()).thenReturn(100);
        when(jsoupWebExtractor.extract(any())).thenReturn(Map.of("title", "Example Domain"));
    }

    @Test
    void submitCrawlJobReturnsAcceptedAndPersists() {
        CrawlRequest request = buildRequest();
        CrawlResponse response = crawlerService.submitCrawlJob(request);

        assertNotNull(response.getJobId());
        assertEquals("ACCEPTED", response.getStatus());
        UUID jobId = UUID.fromString(response.getJobId());
        assertTrue(jobRepository.findById(jobId).isPresent());

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .until(() -> {
                    CrawlJob.JobStatus status = jobRepository.findById(jobId)
                            .map(CrawlJob::getStatus).orElse(null);
                    return status == CrawlJob.JobStatus.COMPLETED
                            || status == CrawlJob.JobStatus.FAILED;
                });
    }

    @Test
    void getCrawlJobStatusEventuallyReturnsCompleted() {
        CrawlRequest request = buildRequest();
        String jobId = crawlerService.submitCrawlJob(request).getJobId();

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    CrawlResponse status = crawlerService.getCrawlJobStatus(jobId);
                    assertEquals(CrawlJob.JobStatus.COMPLETED.name(), status.getStatus());
                    assertNotNull(status.getData());
                    assertEquals("Example Domain", status.getData().get("title"));
                });
    }

    @Test
    void getCrawlJobStatusOfUnknownIdThrowsJobNotFound() {
        String unknown = UUID.randomUUID().toString();
        assertThrows(JobNotFoundException.class, () -> crawlerService.getCrawlJobStatus(unknown));
    }

    @Test
    void getCrawlJobStatusOfInvalidUuidThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> crawlerService.getCrawlJobStatus("not-a-uuid"));
    }

    private CrawlRequest buildRequest() {
        CrawlRequest.ExtractorConfig config = CrawlRequest.ExtractorConfig.builder()
                .fieldName("title")
                .selector("h1")
                .build();
        return CrawlRequest.builder()
                .sourceType("web")
                .sourceUrl("https://example.com")
                .extractors(List.of(config))
                .build();
    }
}
