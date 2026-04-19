package br.com.gencrawler.config;

import br.com.gencrawler.core.crawler.WebsiteCrawler;
import br.com.gencrawler.core.extractor.api.ApiExtractor;
import br.com.gencrawler.core.extractor.web.JsoupWebExtractor;
import br.com.gencrawler.core.extractor.web.PlaywrightWebExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExtractorConfig {

    @Bean
    public JsoupWebExtractor jsoupWebExtractor() {
        return new JsoupWebExtractor();
    }

    @Bean
    public PlaywrightWebExtractor playwrightWebExtractor() {
        return new PlaywrightWebExtractor();
    }

    @Bean
    public WebsiteCrawler websiteCrawler() {
        return new WebsiteCrawler();
    }

    @Bean
    public ApiExtractor apiExtractor() {
        return new ApiExtractor();
    }
}
