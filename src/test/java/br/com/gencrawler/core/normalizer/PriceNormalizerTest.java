package br.com.gencrawler.core.normalizer;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;

class PriceNormalizerTest {

    @Test
    void normalizesBrlWithSymbolAndThousandsSeparator() throws ParseException {
        assertEquals(new BigDecimal("1299.00"), PriceNormalizer.normalize("R$ 1.299,00", "BRL"));
    }

    @Test
    void normalizesUsdWithSymbolAndThousandsSeparator() throws ParseException {
        assertEquals(new BigDecimal("1299.00"), PriceNormalizer.normalize("$1,299.00", "USD"));
    }

    @Test
    void normalizesEurWithGermanFormat() throws ParseException {
        assertEquals(new BigDecimal("1299.00"), PriceNormalizer.normalize("1.299,00 €", "EUR"));
    }

    @Test
    void normalizesIntegerValue() throws ParseException {
        assertEquals(new BigDecimal("500.00"), PriceNormalizer.normalize("R$ 500", "BRL"));
    }

    @Test
    void roundsHalfUpToTwoDecimals() throws ParseException {
        assertEquals(new BigDecimal("1.23"), PriceNormalizer.normalize("1.225", "USD"));
    }

    @Test
    void unknownCurrencyFallsBackToUsLocale() throws ParseException {
        assertEquals(new BigDecimal("99.99"), PriceNormalizer.normalize("99.99", "XXX"));
    }

    @Test
    void nullPriceThrows() {
        assertThrows(IllegalArgumentException.class, () -> PriceNormalizer.normalize(null, "USD"));
    }

    @Test
    void blankPriceThrows() {
        assertThrows(IllegalArgumentException.class, () -> PriceNormalizer.normalize("   ", "USD"));
    }

    @Test
    void noNumericValueThrowsParseException() {
        assertThrows(ParseException.class, () -> PriceNormalizer.normalize("R$", "BRL"));
    }

    @Test
    void extractPriceReturnsFirstMatch() {
        assertEquals("1.299,00", PriceNormalizer.extractPrice("Preço: R$ 1.299,00 (à vista)"));
    }

    @Test
    void extractPriceOnTextWithoutNumberReturnsNull() {
        assertNull(PriceNormalizer.extractPrice("sem número aqui"));
    }

    @Test
    void extractPriceOnNullReturnsNull() {
        assertNull(PriceNormalizer.extractPrice(null));
    }
}
