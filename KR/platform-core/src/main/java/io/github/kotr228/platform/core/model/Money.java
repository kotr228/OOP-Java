package io.github.kotr228.platform.core.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Грошові суми. Усі суми в програмах пакета зберігаються як BigDecimal
 * з двома знаками після коми (копійки): double дає похибки округлення
 * (0.1 + 0.2 != 0.3), неприпустимі для фінансових даних.
 */
public final class Money {
    public static final String CURRENCY = "грн";
    public static final int SCALE = 2;
    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(SCALE);

    private static final Locale UKRAINIAN = Locale.forLanguageTag("uk-UA");

    private Money() {
    }

    /** Сума з цілої кількості гривень (для коду й тестів). */
    public static BigDecimal of(long hryvnias) {
        return BigDecimal.valueOf(hryvnias).setScale(SCALE);
    }

    /** Сума з рядка «1234.50» (формат файлів даних). */
    public static BigDecimal parse(String text) {
        try {
            return normalize(new BigDecimal(text.trim()));
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Некоректна грошова сума «" + text + "»");
        }
    }

    /**
     * Приводить суму до копійок. Сума з більш ніж двома знаками після коми
     * відхиляється, а не округлюється мовчки.
     */
    public static BigDecimal normalize(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidDataException("Сума має бути задана");
        }
        try {
            return amount.setScale(SCALE, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new InvalidDataException(
                    "Сума може мати не більше двох знаків після коми");
        }
    }

    /** Середнє арифметичне з округленням до копійок за банківськими правилами. */
    public static BigDecimal average(BigDecimal total, int count) {
        if (count == 0) {
            return ZERO;
        }
        return total.divide(BigDecimal.valueOf(count), SCALE,
                RoundingMode.HALF_EVEN);
    }

    /** Сума для показу користувачу: «12 345,00». */
    public static String format(BigDecimal amount) {
        DecimalFormat format = new DecimalFormat("#,##0.00",
                DecimalFormatSymbols.getInstance(UKRAINIAN));
        return format.format(amount);
    }

    /** Сума з валютою: «12 345,00 грн». */
    public static String formatWithCurrency(BigDecimal amount) {
        return format(amount) + " " + CURRENCY;
    }
}
