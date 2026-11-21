package io.github.kotr228.insurance.storage;

import io.github.kotr228.insurance.model.Agent;
import io.github.kotr228.insurance.model.Client;
import io.github.kotr228.insurance.model.InsurancePeriod;
import io.github.kotr228.insurance.model.InsuranceType;
import io.github.kotr228.insurance.model.Policy;
import io.github.kotr228.insurance.service.InsuranceCompany;
import io.github.kotr228.platform.core.model.FullName;
import io.github.kotr228.platform.core.model.Money;
import io.github.kotr228.platform.core.model.PhoneNumber;
import io.github.kotr228.platform.core.storage.Csv;
import io.github.kotr228.platform.core.storage.DataFileException;
import io.github.kotr228.platform.core.storage.DataStore;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Сховище даних страхової компанії у трьох CSV-файлах каталогу
 * (UTF-8, роздільник «;», перший рядок — заголовок).
 * Дати записуються у форматі ISO (РРРР-ММ-ДД), суми — з крапкою.
 */
public final class CsvInsuranceStore implements DataStore<InsuranceCompany> {
    public static final String AGENTS_FILE = "agents.csv";
    public static final String CLIENTS_FILE = "clients.csv";
    public static final String POLICIES_FILE = "policies.csv";

    private static final Logger LOG =
            Logger.getLogger(CsvInsuranceStore.class.getName());

    private static final String[] AGENTS_HEADER = {"id", "ПІБ", "телефон", "стаж"};
    private static final String[] CLIENTS_HEADER =
            {"id", "ПІБ", "телефон", "дата народження", "id агента"};
    private static final String[] POLICIES_HEADER = {"id", "тип", "дата початку",
            "дата завершення", "страховий випадок", "вартість", "id клієнта"};

    private final Path dir;
    private final String companyName;

    /**
     * @param dir         каталог з файлами даних
     * @param companyName назва компанії, що створюється під час читання
     */
    public CsvInsuranceStore(Path dir, String companyName) {
        this.dir = dir;
        this.companyName = companyName;
    }

    public Path getDirectory() {
        return dir;
    }

    @Override
    public boolean exists() {
        return Files.exists(dir.resolve(AGENTS_FILE))
                && Files.exists(dir.resolve(CLIENTS_FILE))
                && Files.exists(dir.resolve(POLICIES_FILE));
    }

    @Override
    public String location() {
        return dir.toAbsolutePath().toString();
    }

    // ---------- Збереження ----------

    @Override
    public void save(InsuranceCompany company) throws IOException {
        Files.createDirectories(dir);
        write(AGENTS_FILE, AGENTS_HEADER, company.getAgents(), a -> Csv.join(
                String.valueOf(a.getId()), a.getFullName().toString(),
                a.getPhone().getNumber(), String.valueOf(a.getExperienceYears())));
        write(CLIENTS_FILE, CLIENTS_HEADER, company.getClients(), c -> Csv.join(
                String.valueOf(c.getId()), c.getFullName().toString(),
                c.getPhone().getNumber(), c.getBirthDate().toString(),
                String.valueOf(c.getAgent().getId())));
        write(POLICIES_FILE, POLICIES_HEADER, company.getPolicies(), p -> Csv.join(
                String.valueOf(p.getId()), p.getInsuranceType().name(),
                p.getPeriod().getStartDate().toString(),
                p.getPeriod().getEndDate().toString(),
                p.getInsuredEvent(), p.getCost().toPlainString(),
                String.valueOf(p.getClient().getId())));
        LOG.info(() -> "Дані збережено в " + location());
    }

    /** Запис через тимчасовий файл: старий файл замінюється лише цілком. */
    private <T> void write(String fileName, String[] header, List<T> items,
                           Function<T, String> toLine) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(Csv.join(header));
        for (T item : items) {
            lines.add(toLine.apply(item));
        }
        Path file = dir.resolve(fileName);
        Path temp = dir.resolve(fileName + ".tmp");
        Files.write(temp, lines, StandardCharsets.UTF_8);
        try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    // ---------- Читання ----------

    /**
     * Читає дані в нову компанію. Якщо хоча б один рядок некоректний,
     * генерується DataFileException, а поточні дані програми не змінюються.
     */
    @Override
    public InsuranceCompany load() throws IOException, DataFileException {
        InsuranceCompany company = new InsuranceCompany(companyName);

        read(AGENTS_FILE, AGENTS_HEADER.length, f -> company.addAgent(new Agent(
                Integer.parseInt(f.get(0)), FullName.parse(f.get(1)),
                new PhoneNumber(f.get(2)), Integer.parseInt(f.get(3)))));

        read(CLIENTS_FILE, CLIENTS_HEADER.length, f -> company.addClient(new Client(
                Integer.parseInt(f.get(0)), FullName.parse(f.get(1)),
                new PhoneNumber(f.get(2)), LocalDate.parse(f.get(3)),
                company.findAgent(Integer.parseInt(f.get(4))).orElseThrow(() ->
                        new IllegalArgumentException(
                                "агента з id " + f.get(4) + " не знайдено")))));

        read(POLICIES_FILE, POLICIES_HEADER.length, f -> company.addPolicy(new Policy(
                Integer.parseInt(f.get(0)), parseType(f.get(1)),
                new InsurancePeriod(LocalDate.parse(f.get(2)),
                        LocalDate.parse(f.get(3))),
                f.get(4), Money.parse(f.get(5)),
                company.findClient(Integer.parseInt(f.get(6))).orElseThrow(() ->
                        new IllegalArgumentException(
                                "клієнта з id " + f.get(6) + " не знайдено")))));
        LOG.info(() -> "Дані прочитано з " + location());
        return company;
    }

    /** Розбирає рядки файлу; кожен рядок передається обробнику як список полів. */
    private void read(String fileName, int fieldCount,
                      Consumer<List<String>> handler)
            throws IOException, DataFileException {
        List<String> lines =
                Files.readAllLines(dir.resolve(fileName), StandardCharsets.UTF_8);
        for (int i = 1; i < lines.size(); i++) {         // рядок 0 — заголовок
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            int lineNumber = i + 1;
            try {
                List<String> fields = Csv.split(line);
                if (fields.size() != fieldCount) {
                    throw new DataFileException(fileName, lineNumber, "очікувалось "
                            + fieldCount + " полів, знайдено " + fields.size());
                }
                handler.accept(fields);
            } catch (NumberFormatException e) {
                throw new DataFileException(fileName, lineNumber, "некоректне число");
            } catch (DateTimeParseException e) {
                throw new DataFileException(fileName, lineNumber,
                        "некоректна дата (очікується формат РРРР-ММ-ДД)");
            } catch (IllegalArgumentException e) {
                // InvalidDataException, невідомий тип, відсутній id, лапки
                throw new DataFileException(fileName, lineNumber, e.getMessage());
            }
        }
    }

    private static InsuranceType parseType(String value) {
        try {
            return InsuranceType.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "невідомий тип страхування «" + value + "»", e);
        }
    }
}
