package io.github.kotr228.platform.core.storage;

/** Помилка у файлі даних: містить назву файлу та номер рядка. */
public class DataFileException extends Exception {
    private static final long serialVersionUID = 1L;

    private final String fileName;
    private final int lineNumber;

    public DataFileException(String fileName, int lineNumber, String message) {
        super("Файл " + fileName + ", рядок " + lineNumber + ": " + message);
        this.fileName = fileName;
        this.lineNumber = lineNumber;
    }

    public String getFileName() {
        return fileName;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
