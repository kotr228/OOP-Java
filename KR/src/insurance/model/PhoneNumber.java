package insurance.model;

/** Номер телефону у форматі +380XXXXXXXXX (допоміжний клас для композиції). */
public class PhoneNumber {
    private static final String PATTERN = "\\+380\\d{9}";

    private final String number;

    public PhoneNumber(String number) {
        if (number == null) {
            throw new InvalidDataException("Телефон не може бути порожнім");
        }
        // дозволяємо вводити пробіли, дужки та дефіси: «+38 (050) 123-45-67»
        String normalized = number.replaceAll("[\\s()-]", "");
        if (!normalized.matches(PATTERN)) {
            throw new InvalidDataException(
                    "Телефон має бути у форматі +380XXXXXXXXX");
        }
        this.number = normalized;
    }

    public String getNumber() {
        return number;
    }

    /** Номер у зручному для читання вигляді: +38 (050) 123-45-67. */
    public String getFormatted() {
        return String.format("+38 (%s) %s-%s-%s",
                number.substring(3, 6), number.substring(6, 9),
                number.substring(9, 11), number.substring(11));
    }

    @Override
    public String toString() {
        return number;
    }
}
