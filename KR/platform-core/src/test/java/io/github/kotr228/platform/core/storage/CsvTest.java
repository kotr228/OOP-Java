package io.github.kotr228.platform.core.storage;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvTest {

    @Test
    void plainFieldsAreNotQuoted() {
        assertEquals("1;Коваленко Олена;+380501234567",
                Csv.join("1", "Коваленко Олена", "+380501234567"));
    }

    @Test
    void specialCharactersAreQuoted() {
        assertEquals("\"ДТП; пожежа\";\"так звана \"\"зелена карта\"\"\"",
                Csv.join("ДТП; пожежа", "так звана \"зелена карта\""));
    }

    @Test
    void roundTrip() {
        String[] fields = {"", "a;b", "\"", "звичайне", "кінець;"};
        assertEquals(List.of(fields), Csv.split(Csv.join(fields)));
    }

    @Test
    void emptyFieldsArePreserved() {
        assertEquals(List.of("", "", ""), Csv.split(";;"));
    }

    @Test
    void unclosedQuoteIsAnError() {
        assertThrows(IllegalArgumentException.class, () -> Csv.split("\"abc;d"));
    }
}
