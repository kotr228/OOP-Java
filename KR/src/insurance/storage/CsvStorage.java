package insurance.storage;

import insurance.model.Agent;
import insurance.model.Client;
import insurance.model.CsvSerializable;
import insurance.model.FullName;
import insurance.model.InsurancePeriod;
import insurance.model.InsuranceType;
import insurance.model.PhoneNumber;
import insurance.model.Policy;
import insurance.service.InsuranceCompany;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Збереження даних компанії у три CSV-файли (кодування UTF-8,
 * роздільник «;», перший рядок — заголовок) та їх читання.
 */
public final class CsvStorage {
    public static final String AGENTS_FILE = "agents.csv";
    public static final String CLIENTS_FILE = "clients.csv";
    public static final String POLICIES_FILE = "policies.csv";

    private static final String AGENTS_HEADER = "id;ПІБ;телефон;стаж";
    private static final String CLIENTS_HEADER =
            "id;ПІБ;телефон;дата народження;id агента";
    private static final String POLICIES_HEADER = "id;тип;дата початку;"
            + "дата завершення;страховий випадок;вартість;id клієнта";

    private CsvStorage() {
    }

    /** Чи є в каталозі збережені дані. */
    public static boolean hasData(Path dir) {
        return Files.exists(dir.resolve(AGENTS_FILE))
                && Files.exists(dir.resolve(CLIENTS_FILE))
                && Files.exists(dir.resolve(POLICIES_FILE));
    }

    // ---------- Збереження ----------

    public static void save(InsuranceCompany company, Path dir)
            throws IOException {
        Files.createDirectories(dir);
        write(dir.resolve(AGENTS_FILE), AGENTS_HEADER,
                company.getAgents().getAll());
        write(dir.resolve(CLIENTS_FILE), CLIENTS_HEADER,
                company.getClients().getAll());
        write(dir.resolve(POLICIES_FILE), POLICIES_HEADER,
                company.getPolicies().getAll());
    }

    /** Запис через тимчасовий файл: старий файл замінюється лише цілком. */
    private static void write(Path file, String header,
                              List<? extends CsvSerializable> items)
            throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(header);
        for (CsvSerializable item : items) {
            lines.add(item.toCsv());
        }
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.write(temp, lines, StandardCharsets.UTF_8);
        Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
    }

    // ---------- Читання ----------

    /**
     * Читає дані в нову компанію. Якщо хоча б один рядок некоректний,
     * генерується DataFileException, а поточні дані програми не змінюються.
     */
    public static InsuranceCompany load(Path dir, String companyName)
            throws IOException, DataFileException {
        InsuranceCompany company = new InsuranceCompany(companyName);

        read(dir.resolve(AGENTS_FILE), 4, f -> company.addAgent(new Agent(
                Integer.parseInt(f[0]), FullName.parse(f[1]),
                new PhoneNumber(f[2]), Integer.parseInt(f[3]))));

        read(dir.resolve(CLIENTS_FILE), 5, f -> company.addClient(new Client(
                Integer.parseInt(f[0]), FullName.parse(f[1]),
                new PhoneNumber(f[2]), LocalDate.parse(f[3]),
                findAgent(company, Integer.parseInt(f[4])))));

        read(dir.resolve(POLICIES_FILE), 7, f -> company.addPolicy(new Policy(
                Integer.parseInt(f[0]), parseType(f[1]),
                new InsurancePeriod(LocalDate.parse(f[2]), LocalDate.parse(f[3])),
                f[4], Double.parseDouble(f[5]),
                findClient(company, Integer.parseInt(f[6])))));
        return company;
    }

    /** Розбирає рядки файлу; кожен рядок передається обробнику як масив полів. */
    private static void read(Path file, int fieldCount, Consumer<String[]> handler)
            throws IOException, DataFileException {
        String name = file.getFileName().toString();
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (int i = 1; i < lines.size(); i++) {         // рядок 0 — заголовок
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            String[] fields = line.split(CsvSerializable.SEPARATOR, -1);
            if (fields.length != fieldCount) {
                throw new DataFileException(name, i + 1, "очікувалось "
                        + fieldCount + " полів, знайдено " + fields.length);
            }
            try {
                handler.accept(fields);
            } catch (NumberFormatException e) {
                throw new DataFileException(name, i + 1, "некоректне число");
            } catch (DateTimeParseException e) {
                throw new DataFileException(name, i + 1,
                        "некоректна дата (очікується формат РРРР-ММ-ДД)");
            } catch (IllegalArgumentException e) {
                // InvalidDataException, невідомий тип страхування, відсутній id
                throw new DataFileException(name, i + 1, e.getMessage());
            }
        }
    }

    private static InsuranceType parseType(String value) {
        try {
            return InsuranceType.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "невідомий тип страхування «" + value + "»");
        }
    }

    private static Agent findAgent(InsuranceCompany company, int id) {
        return company.getAgents().findById(id).orElseThrow(() ->
                new IllegalArgumentException(
                        "агента з id " + id + " не знайдено"));
    }

    private static Client findClient(InsuranceCompany company, int id) {
        return company.getClients().findById(id).orElseThrow(() ->
                new IllegalArgumentException(
                        "клієнта з id " + id + " не знайдено"));
    }
}
