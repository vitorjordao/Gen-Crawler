package br.com.gencrawler.core.extractor.telegram;

import br.com.gencrawler.dto.CrawlRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TelegramExtractorTest {

    private final TelegramExtractor extractor = new TelegramExtractor();

    @Test
    void supportsTelegramSourceType() {
        assertTrue(extractor.supports("telegram"));
        assertTrue(extractor.supports("Telegram"));
        assertFalse(extractor.supports("web"));
    }

    @Test
    void parsesChannelFromFullUrl() {
        assertEquals("mychannel", extractor.extractChannelUsername("https://t.me/mychannel"));
    }

    @Test
    void parsesChannelFromTelegramMeUrl() {
        assertEquals("mychannel", extractor.extractChannelUsername("https://telegram.me/mychannel"));
    }

    @Test
    void parsesChannelFromAtHandle() {
        assertEquals("mychannel", extractor.extractChannelUsername("@mychannel"));
    }

    @Test
    void blankUrlReturnsEmpty() {
        assertEquals("", extractor.extractChannelUsername(""));
        assertEquals("", extractor.extractChannelUsername("   "));
        assertEquals("", extractor.extractChannelUsername(null));
    }

    @Test
    void extractWithoutTokenThrows() {
        CrawlRequest request = CrawlRequest.builder()
                .sourceType("telegram")
                .sourceUrl("@mychannel")
                .extractors(List.of())
                .build();

        assertThrows(IllegalStateException.class, () -> extractor.extract(request));
    }
}
