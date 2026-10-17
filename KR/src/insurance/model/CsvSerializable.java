package insurance.model;

/** Об'єкт, який можна записати в рядок CSV-файлу (для збереження даних). */
public interface CsvSerializable {

    /** Роздільник полів у CSV-файлах програми. */
    String SEPARATOR = ";";

    String toCsv();
}
