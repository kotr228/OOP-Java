package insurance.model;

import java.text.Collator;
import java.util.Locale;

/** ПІБ людини (допоміжний клас для композиції). */
public class FullName implements Comparable<FullName> {
    // Порівняння за правилами української абетки (Є, І, Ї стоять на своїх місцях)
    private static final Collator UKRAINIAN =
            Collator.getInstance(Locale.forLanguageTag("uk-UA"));

    private final String lastName;
    private final String firstName;
    private final String middleName;

    public FullName(String lastName, String firstName, String middleName) {
        this.lastName = checkPart(lastName, "Прізвище");
        this.firstName = checkPart(firstName, "Ім'я");
        this.middleName = middleName == null ? "" : middleName.trim();
    }

    /** Створює ПІБ з рядка «Прізвище Ім'я По батькові». */
    public static FullName parse(String text) {
        if (text == null || text.isBlank()) {
            throw new InvalidDataException("ПІБ не може бути порожнім");
        }
        String[] parts = text.trim().split("\\s+");
        if (parts.length < 2) {
            throw new InvalidDataException(
                    "ПІБ має містити щонайменше прізвище та ім'я");
        }
        return new FullName(parts[0], parts[1], parts.length > 2 ? parts[2] : "");
    }

    private static String checkPart(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidDataException(fieldName + " не може бути порожнім");
        }
        String trimmed = value.trim();
        if (!trimmed.matches("[\\p{L}'’-]+")) {
            throw new InvalidDataException(
                    fieldName + " може містити лише літери, апостроф і дефіс");
        }
        return trimmed;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    /** Прізвище та ініціали, наприклад «Коваленко О. П.». */
    public String getShortForm() {
        String result = lastName + " " + firstName.charAt(0) + ".";
        if (!middleName.isEmpty()) {
            result += " " + middleName.charAt(0) + ".";
        }
        return result;
    }

    @Override
    public int compareTo(FullName other) {
        return UKRAINIAN.compare(toString(), other.toString());
    }

    @Override
    public String toString() {
        return middleName.isEmpty()
                ? lastName + " " + firstName
                : lastName + " " + firstName + " " + middleName;
    }
}
