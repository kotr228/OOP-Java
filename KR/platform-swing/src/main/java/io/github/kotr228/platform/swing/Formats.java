package io.github.kotr228.platform.swing;

import io.github.kotr228.platform.core.model.InvalidDataException;
import io.github.kotr228.platform.core.model.Money;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/** Перетворення дат і грошових сум між текстом форми та значеннями. */
public final class Formats {
    /** Дата у звичному для користувача вигляді: 25.07.2001. */
    public static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("dd.MM.uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);

    private Formats() {
    }

    public static String date(LocalDate date) {
        return date.format(DATE);
    }

    public static LocalDate parseDate(String text, String field) {
        try {
            return LocalDate.parse(text.strip(), DATE);
        } catch (DateTimeParseException e) {
            throw new InvalidDataException("Поле «" + field
                    + "»: введіть дату у форматі ДД.ММ.РРРР");
        }
    }

    /**
     * Сума, введена користувачем: допускаються пробіли між розрядами
     * і кома або крапка перед копійками («12 345,50»).
     */
    public static BigDecimal parseMoney(String text, String field) {
        String normalized = text.replaceAll("[\\s\\u00A0\\u202F]", "")
                .replace(',', '.');
        if (!normalized.matches("-?\\d+(\\.\\d+)?")) {
            throw new InvalidDataException("Поле «" + field + "»: введіть число");
        }
        return Money.parse(normalized);
    }

    /** Сума для поля введення: «12345,50» (без пробілів між розрядами). */
    public static String moneyForInput(BigDecimal value) {
        return value.toPlainString().replace('.', ',');
    }

    public static String money(BigDecimal value) {
        return Money.format(value);
    }

    /** Текст значення так, як його бачить користувач у таблицях. */
    public static String display(Object value) {
        if (value instanceof LocalDate d) {
            return date(d);
        }
        if (value instanceof BigDecimal m) {
            return money(m);
        }
        return value == null ? "" : value.toString();
    }
}
