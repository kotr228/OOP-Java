package io.github.kotr228.platform.swing;

import io.github.kotr228.platform.core.model.InvalidDataException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FormatsTest {

    @Test
    void datesUseDayMonthYear() {
        assertEquals("05.03.2026", Formats.date(LocalDate.of(2026, 3, 5)));
        assertEquals(LocalDate.of(2001, 7, 25), Formats.parseDate(" 25.07.2001 ", "Д"));
    }

    @Test
    void impossibleDatesAreRejected() {
        assertThrows(InvalidDataException.class, () -> Formats.parseDate("31.02.2000", "Д"));
        assertThrows(InvalidDataException.class, () -> Formats.parseDate("2000-01-01", "Д"));
    }

    @Test
    void moneyInputIsForgiving() {
        assertEquals(new BigDecimal("12345.50"), Formats.parseMoney("12 345,5", "В"));
        assertEquals(new BigDecimal("4600.00"), Formats.parseMoney("4600", "В"));
        assertEquals(new BigDecimal("1.25"), Formats.parseMoney("1.25", "В"));
    }

    @Test
    void invalidMoneyIsRejected() {
        assertThrows(InvalidDataException.class, () -> Formats.parseMoney("", "В"));
        assertThrows(InvalidDataException.class, () -> Formats.parseMoney("12 грн", "В"));
        assertThrows(InvalidDataException.class, () -> Formats.parseMoney("1e5", "В"));
        assertThrows(InvalidDataException.class, () -> Formats.parseMoney("1,005", "В"));
    }

    @Test
    void moneyForInputRoundTrips() {
        BigDecimal cost = new BigDecimal("4600.50");
        assertEquals(cost, Formats.parseMoney(Formats.moneyForInput(cost), "В"));
    }

    @Test
    void displayFormatsKnownTypes() {
        assertEquals("01.01.2026", Formats.display(LocalDate.of(2026, 1, 1)));
        assertEquals("", Formats.display(null));
        assertEquals("42", Formats.display(42));
    }
}
