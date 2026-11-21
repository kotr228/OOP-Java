package insurance.gui;

import insurance.model.InvalidDataException;

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
            return LocalDate.parse(text.trim(), DATE);
        } catch (DateTimeParseException e) {
            throw new InvalidDataException("Поле «" + field
                    + "»: введіть дату у форматі ДД.ММ.РРРР");
        }
    }

    public static double parseMoney(String text, String field) {
        try {
            String normalized = text.trim().replace(" ", "").replace(',', '.');
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Поле «" + field + "»: введіть число");
        }
    }

    public static String money(double value) {
        return String.format("%,.2f", value);
    }
}
