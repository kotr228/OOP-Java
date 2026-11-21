package io.github.kotr228.platform.core.storage;

import java.util.ArrayList;
import java.util.List;

/**
 * Запис і розбір рядків CSV (роздільник «;», як у Excel з українськими
 * налаштуваннями). Поля з роздільником, лапками або переносом рядка
 * беруться в лапки, лапки всередині поля подвоюються (RFC 4180).
 */
public final class Csv {
    public static final char SEPARATOR = ';';
    private static final char QUOTE = '"';

    private Csv() {
    }

    /** Склеює поля в один рядок CSV. */
    public static String join(String... fields) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                line.append(SEPARATOR);
            }
            line.append(quote(fields[i]));
        }
        return line.toString();
    }

    private static String quote(String field) {
        String value = field == null ? "" : field;
        boolean needsQuotes = value.indexOf(SEPARATOR) >= 0
                || value.indexOf(QUOTE) >= 0
                || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0;
        if (!needsQuotes) {
            return value;
        }
        return QUOTE + value.replace("\"", "\"\"") + QUOTE;
    }

    /**
     * Розбирає рядок CSV на поля.
     *
     * @throws IllegalArgumentException якщо лапки не закрито
     */
    public static List<String> split(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQuotes) {
                if (ch == QUOTE) {
                    if (i + 1 < line.length() && line.charAt(i + 1) == QUOTE) {
                        field.append(QUOTE);         // подвоєні лапки
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    field.append(ch);
                }
            } else if (ch == QUOTE && field.isEmpty()) {
                inQuotes = true;
            } else if (ch == SEPARATOR) {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(ch);
            }
        }
        if (inQuotes) {
            throw new IllegalArgumentException("незакриті лапки");
        }
        fields.add(field.toString());
        return fields;
    }
}
