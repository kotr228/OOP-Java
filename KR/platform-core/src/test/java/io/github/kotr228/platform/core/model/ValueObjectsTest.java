package io.github.kotr228.platform.core.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValueObjectsTest {

    @Test
    void fullNameParsesAndFormats() {
        FullName name = FullName.parse("  Коваленко   Олена  Петрівна ");
        assertEquals("Коваленко", name.getLastName());
        assertEquals("Олена", name.getFirstName());
        assertEquals("Петрівна", name.getMiddleName());
        assertEquals("Коваленко О. П.", name.getShortForm());
        assertEquals("Коваленко Олена Петрівна", name.toString());
    }

    @Test
    void fullNameWithoutMiddleName() {
        FullName name = FullName.parse("Д'Арк-Сміт Жанна");
        assertEquals("Д'Арк-Сміт Ж.", name.getShortForm());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "Іван", "Іван 2Петров", "Петров Іван;"})
    void fullNameRejectsInvalid(String text) {
        assertThrows(InvalidDataException.class, () -> FullName.parse(text));
    }

    @Test
    void fullNameIsValueObject() {
        assertEquals(FullName.parse("Мельник Андрій"), FullName.parse("Мельник  Андрій"));
        assertEquals(FullName.parse("Мельник Андрій").hashCode(),
                FullName.parse("Мельник Андрій").hashCode());
        assertNotEquals(FullName.parse("Мельник Андрій"),
                FullName.parse("Мельник Андрій Іванович"));
    }

    @Test
    void fullNamesSortByUkrainianAlphabet() {
        List<String> sorted = Stream.of("Яковенко Ія", "Єрмак Олег", "Іваненко Ія",
                        "Ґонта Іван", "Гнатюк Оля", "Їжак Петро")
                .map(FullName::parse).sorted()
                .map(FullName::getLastName).collect(Collectors.toList());
        assertEquals(List.of("Гнатюк", "Ґонта", "Єрмак", "Іваненко", "Їжак",
                "Яковенко"), sorted);
    }

    @Test
    void phoneNormalizesAndFormats() {
        PhoneNumber phone = new PhoneNumber("+38 (050) 123-45-67");
        assertEquals("+380501234567", phone.getNumber());
        assertEquals("+38 (050) 123-45-67", phone.getFormatted());
        assertEquals(new PhoneNumber("+380501234567"), phone);
    }

    @ParameterizedTest
    @ValueSource(strings = {"050-123", "0501234567", "+3805012345678", "+38050123456a"})
    void phoneRejectsInvalid(String text) {
        assertThrows(InvalidDataException.class, () -> new PhoneNumber(text));
    }
}
