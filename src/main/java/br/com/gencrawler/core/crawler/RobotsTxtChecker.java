package br.com.gencrawler.core.crawler;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;

import javax.net.ssl.SSLSocketFactory;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minimal robots.txt parser. Reads Disallow rules for User-agent "*" and caches per host.
 * Not a full implementation (no Allow, no wildcards beyond simple prefix match) — enough to be polite.
 */
@Slf4j
public class RobotsTxtChecker {

    private final ConcurrentHashMap<String, List<String>> disallowCache = new ConcurrentHashMap<>();
    private final int timeoutMs;
    private final SSLSocketFactory sslSocketFactory;

    public RobotsTxtChecker(int timeoutMs) {
        this(timeoutMs, null);
    }

    public RobotsTxtChecker(int timeoutMs, SSLSocketFactory sslSocketFactory) {
        this.timeoutMs = timeoutMs;
        this.sslSocketFactory = sslSocketFactory;
    }

    public boolean isAllowed(URI url) {
        String host = url.getScheme() + "://" + url.getAuthority();
        List<String> disallows = disallowCache.computeIfAbsent(host, this::fetchDisallow);
        String path = url.getRawPath() == null || url.getRawPath().isEmpty() ? "/" : url.getRawPath();
        for (String rule : disallows) {
            if (!rule.isEmpty() && path.startsWith(rule)) {
                return false;
            }
        }
        return true;
    }

    private List<String> fetchDisallow(String host) {
        List<String> rules = new ArrayList<>();
        try {
            var conn = Jsoup.connect(host + "/robots.txt")
                    .ignoreContentType(true)
                    .timeout(timeoutMs);
            if (sslSocketFactory != null) conn.sslSocketFactory(sslSocketFactory);
            String body = conn.execute().body();
            boolean applies = false;
            for (String raw : body.split("\\R")) {
                String line = raw.split("#", 2)[0].trim();
                if (line.isEmpty()) continue;
                String[] kv = line.split(":", 2);
                if (kv.length != 2) continue;
                String key = kv[0].trim().toLowerCase();
                String value = kv[1].trim();
                if ("user-agent".equals(key)) {
                    applies = "*".equals(value);
                } else if (applies && "disallow".equals(key)) {
                    rules.add(value);
                }
            }
        } catch (Exception e) {
            log.debug("Could not fetch robots.txt for {}: {}", host, e.getMessage());
        }
        return rules;
    }
}
