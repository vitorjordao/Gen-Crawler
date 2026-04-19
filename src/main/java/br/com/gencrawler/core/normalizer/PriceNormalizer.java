package br.com.gencrawler.core.normalizer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Normalizes price strings from various formats to BigDecimal.
 * Examples: "R$ 1.299,00" (BRL) -> 1299.00, "$1,299.00" (USD) -> 1299.00.
 */
public class PriceNormalizer {

    private static final Pattern PRICE_PATTERN = Pattern.compile("[0-9]+([.,][0-9]+)*");

    private static final Map<String, Locale> CURRENCY_LOCALES = Map.ofEntries(
            Map.entry("BRL", Locale.forLanguageTag("pt-BR")),
            Map.entry("USD", Locale.US),
            Map.entry("EUR", Locale.GERMANY),
            Map.entry("GBP", Locale.UK),
            Map.entry("JPY", Locale.JAPAN),
            Map.entry("CNY", Locale.CHINA),
            Map.entry("AUD", Locale.forLanguageTag("en-AU")),
            Map.entry("CAD", Locale.CANADA),
            Map.entry("CHF", Locale.forLanguageTag("de-CH")),
            Map.entry("ARS", Locale.forLanguageTag("es-AR")),
            Map.entry("CLP", Locale.forLanguageTag("es-CL")),
            Map.entry("COP", Locale.forLanguageTag("es-CO")),
            Map.entry("MXN", Locale.forLanguageTag("es-MX")),
            Map.entry("PEN", Locale.forLanguageTag("es-PE")),
            Map.entry("UYU", Locale.forLanguageTag("es-UY"))
    );

    public static BigDecimal normalize(String priceStr, String currencyCode) throws ParseException {
        if (priceStr == null || priceStr.isBlank()) {
            throw new IllegalArgumentException("Price string cannot be null or blank");
        }

        String cleaned = priceStr.replaceAll("[^0-9.,-]", "").trim();
        if (cleaned.isBlank() || cleaned.equals("-") || cleaned.equals(".") || cleaned.equals(",")) {
            throw new ParseException("No numeric value found in: " + priceStr, 0);
        }

        Locale locale = getLocaleForCurrency(currencyCode);
        NumberFormat format = NumberFormat.getNumberInstance(locale);
        Number number = format.parse(cleaned);

        return BigDecimal.valueOf(number.doubleValue()).setScale(2, RoundingMode.HALF_UP);
    }

    private static Locale getLocaleForCurrency(String currencyCode) {
        if (currencyCode == null) {
            return Locale.US;
        }
        return CURRENCY_LOCALES.getOrDefault(currencyCode.toUpperCase(), Locale.US);
    }

    public static String extractPrice(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        var matcher = PRICE_PATTERN.matcher(text);
        return matcher.find() ? matcher.group() : null;
    }
}
