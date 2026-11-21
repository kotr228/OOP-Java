package io.github.kotr228.platform.core.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    @Test
    void sumsAreExact() {
        // з double тут вийшло б 0.30000000000000004
        assertEquals(Money.parse("0.30"),
                Money.parse("0.1").add(Money.parse("0.2")));
    }

    @Test
    void normalizesToKopecks() {
        assertEquals(new BigDecimal("12.50"), Money.parse("12.5"));
        assertEquals(new BigDecimal("9800.00"), Money.of(9800));
    }

    @Test
    void rejectsFractionsOfKopeck() {
        assertThrows(InvalidDataException.class, () -> Money.parse("10.005"));
        assertThrows(InvalidDataException.class, () -> Money.parse("abc"));
        assertThrows(InvalidDataException.class, () -> Money.normalize(null));
    }

    @Test
    void averageRoundsToKopecks() {
        assertEquals(new BigDecimal("3.33"), Money.average(Money.of(10), 3));
        assertEquals(Money.ZERO, Money.average(Money.ZERO, 0));
    }

    @Test
    void formatsForUkrainianUsers() {
        String formatted = Money.format(new BigDecimal("12345.5"))
                .replace(' ', ' ').replace(' ', ' ');
        assertEquals("12 345,50", formatted);
        assertEquals("0,00 грн", Money.formatWithCurrency(Money.ZERO));
    }
}
