package br.com.gencrawler.controller;

import br.com.gencrawler.dto.CrawlResponse;
import br.com.gencrawler.service.CrawlerService;
import br.com.gencrawler.service.JobNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CrawlerController.class)
@Import(GlobalExceptionHandler.class)
class CrawlerControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CrawlerService crawlerService;

    @Test
    void testEndpointReturns200() throws Exception {
        mvc.perform(get("/api/v1/crawler/test"))
                .andExpect(status().isOk());
    }

    @Test
    void crawlWithValidBodyReturns202() throws Exception {
        when(crawlerService.submitCrawlJob(any())).thenReturn(CrawlResponse.accepted("abc"));

        String body = objectMapper.writeValueAsString(Map.of(
                "sourceType", "web",
                "sourceUrl", "https://example.com",
                "extractors", java.util.List.of(Map.of(
                        "fieldName", "title",
                        "selector", "h1"))
        ));

        mvc.perform(post("/api/v1/crawler/crawl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").value("abc"))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void crawlWithMissingFieldReturns400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "sourceUrl", "https://example.com"
        ));

        mvc.perform(post("/api/v1/crawler/crawl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void statusOfUnknownJobReturns404() throws Exception {
        when(crawlerService.getCrawlJobStatus("missing"))
                .thenThrow(new JobNotFoundException("missing"));

        mvc.perform(get("/api/v1/crawler/status/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void statusWithInvalidUuidReturns400() throws Exception {
        when(crawlerService.getCrawlJobStatus("not-a-uuid"))
                .thenThrow(new IllegalArgumentException("Invalid UUID"));

        mvc.perform(get("/api/v1/crawler/status/not-a-uuid"))
                .andExpect(status().isBadRequest());
    }
}
