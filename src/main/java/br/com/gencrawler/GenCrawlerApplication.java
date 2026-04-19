package br.com.gencrawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GenCrawlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GenCrawlerApplication.class, args);
    }
}
