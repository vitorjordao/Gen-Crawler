package br.com.gencrawler.core.crawler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

/**
 * Builds an SSL socket factory that skips certificate validation.
 * Only use when {@code CrawlRequest.validateCertificates} is explicitly false
 * (typical case: a site with an expired cert that you still need to scrape).
 */
public final class InsecureTrust {

    private InsecureTrust() {}

    public static SSLSocketFactory trustAllSocketFactory() {
        try {
            TrustManager[] trustAll = {
                    new X509TrustManager() {
                        @Override public void checkClientTrusted(X509Certificate[] c, String a) {}
                        @Override public void checkServerTrusted(X509Certificate[] c, String a) {}
                        @Override public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    }
            };
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, trustAll, new java.security.SecureRandom());
            return ctx.getSocketFactory();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build trust-all SSL factory", e);
        }
    }
}
