package br.com.gencrawler.controller;

import br.com.gencrawler.dto.CrawlRequest;
import br.com.gencrawler.dto.CrawlResponse;
import br.com.gencrawler.service.CrawlerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/crawler")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Crawler", description = "Web crawling API")
public class CrawlerController {

    private final CrawlerService crawlerService;

    @PostMapping("/crawl")
    @Operation(summary = "Submit a crawl job", description = "Submit a new crawl job for processing")
    public ResponseEntity<CrawlResponse> crawl(@Valid @RequestBody CrawlRequest request) {
        log.info("Received crawl request: sourceType={}, url={}", request.getSourceType(), request.getSourceUrl());
        CrawlResponse response = crawlerService.submitCrawlJob(request);
        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/status/{jobId}")
    @Operation(summary = "Get crawl job status", description = "Get the status and result of a crawl job")
    public ResponseEntity<CrawlResponse> getStatus(@PathVariable String jobId) {
        return ResponseEntity.ok(crawlerService.getCrawlJobStatus(jobId));
    }

    @GetMapping("/test")
    @Operation(summary = "Test endpoint", description = "Simple health-check endpoint")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("API is working");
    }
}
