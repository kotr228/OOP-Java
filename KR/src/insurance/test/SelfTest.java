package insurance.test;

import insurance.app.DemoData;
import insurance.model.Agent;
import insurance.model.Client;
import insurance.model.FullName;
import insurance.model.InsurancePeriod;
import insurance.model.InsuranceType;
import insurance.model.InvalidDataException;
import insurance.model.PhoneNumber;
import insurance.model.Policy;
import insurance.service.InsuranceCompany;
import insurance.storage.CsvStorage;
import insurance.storage.DataFileException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Автоматична перевірка програми без графічного інтерфейсу.
 * Кожен тест кидає AssertionError, якщо фактичний результат
 * не збігається з очікуваним.
 */
public final class SelfTest {
    private int passed;
    private int failed;

    public static void main(String[] args) throws Exception {
        SelfTest t = new SelfTest();
        t.run("Т1. Запити 1–6 на демонстраційних даних", SelfTest::queries);
        t.run("Т2. Порожня компанія: запити без результату",
                SelfTest::emptyCompany);
        t.run("Т3. Перевірка значень під час створення", SelfTest::validation);
        t.run("Т4. Редагування агента, клієнта і поліса", SelfTest::editing);
        t.run("Т5. Невдале редагування не змінює поліс", SelfTest::atomicEdit);
        t.run("Т6. Зв'язки та унікальність телефону", SelfTest::integrity);
        t.run("Т7. Видалення з правилами цілісності", SelfTest::removal);
        t.run("Т8. Збереження й читання CSV-файлів", SelfTest::csvRoundTrip);
        t.run("Т9. Пошкоджений CSV-файл", SelfTest::csvDamaged);
        System.out.println();
        System.out.println("Пройдено: " + t.passed + ", не пройдено: " + t.failed);
        if (t.failed > 0) {
            System.exit(1);
        }
    }

    interface Check {
        void run() throws Exception;
    }

    private void run(String name, Check check) {
        try {
            check.run();
            passed++;
            System.out.println("[OK]   " + name);
        } catch (Throwable e) {
            failed++;
            System.out.println("[FAIL] " + name + ": " + e);
        }
    }

    // ---------- Допоміжні перевірки ----------

    static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    static void equal(Object expected, Object actual, String what) {
        if (!expected.equals(actual)) {
            throw new AssertionError(what + ": очікувалось " + expected
                    + ", отримано " + actual);
        }
    }

    /** Дія має завершитися винятком InvalidDataException. */
    static void rejected(Runnable action, String what) {
        try {
            action.run();
        } catch (InvalidDataException e) {
            return;
        }
        throw new AssertionError("не відхилено: " + what);
    }

    /** Пошук за прізвищем: id залежать від порядку створення об'єктів. */
    static Agent agent(InsuranceCompany c, String lastName) {
        return c.getAgents().findAll(a -> a.getFullName().getLastName()
                .equals(lastName)).get(0);
    }

    static Client client(InsuranceCompany c, String lastName) {
        return c.getClients().findAll(a -> a.getFullName().getLastName()
                .equals(lastName)).get(0);
    }

    /** Поліс за порядковим номером у списку (з 0). */
    static Policy policy(InsuranceCompany c, int index) {
        return c.getPolicies().getAll().get(index);
    }

    // ---------- Тести ----------

    static void queries() {
        InsuranceCompany c = DemoData.create();
        List<Client> clients = c.getClientList();
        equal(4, clients.size(), "кількість клієнтів");
        equal("Лисенко", clients.get(0).getFullName().getLastName(), "перший");
        equal("Шевченко", clients.get(3).getFullName().getLastName(), "останній");
        equal(6, c.getPolicyCount(), "кількість полісів");
        equal(25000.0, c.getMostExpensivePolicy().orElseThrow().getCost(),
                "найдорожчий поліс");
        equal("8891.67", String.format("%.2f", c.getAverageCost())
                .replace(',', '.'), "середня вартість");
        equal("Коваленко", c.getAgentWithMostClients().orElseThrow()
                .getFullName().getLastName(), "агент із найбільшою кількістю");
        equal(InsuranceType.AUTO, c.getMostPopularType().orElseThrow(),
                "найпопулярніший тип");
    }

    static void emptyCompany() {
        InsuranceCompany c = new InsuranceCompany("Порожня");
        check(c.getClientList().isEmpty(), "список клієнтів має бути порожнім");
        equal(0, c.getPolicyCount(), "кількість полісів");
        check(c.getMostExpensivePolicy().isEmpty(), "найдорожчого поліса немає");
        equal(0.0, c.getAverageCost(), "середня вартість");
        check(c.getAgentWithMostClients().isEmpty(), "агента немає");
        check(c.getMostPopularType().isEmpty(), "типу немає");
    }

    static void validation() {
        rejected(() -> new PhoneNumber("050-123"), "короткий телефон");
        rejected(() -> FullName.parse("Іван"), "ПІБ без імені");
        rejected(() -> FullName.parse("Іван 2Петров"), "цифра в імені");
        rejected(() -> new InsurancePeriod(LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 1, 1)), "переплутані дати");
        InsuranceCompany c = DemoData.create();
        Agent agent = agent(c, "Коваленко");
        rejected(() -> new Client(FullName.parse("Мала Олена"),
                new PhoneNumber("+380500000001"),
                LocalDate.now().minusYears(17), agent), "вік 17 років");
        Policy p = policy(c, 0);
        rejected(() -> p.setCost(0), "нульова вартість");
        rejected(() -> p.setCost(1_000_001), "вартість понад максимум");
        rejected(() -> Agent.setCommissionRate(0.9), "комісія 90 %");
        equal("+38 (050) 123-45-67",
                new PhoneNumber("+38 (050) 123-45-67").getFormatted(),
                "нормалізація телефону");
    }

    static void editing() {
        InsuranceCompany c = DemoData.create();
        Agent melnyk = agent(c, "Мельник");
        c.updateAgent(melnyk.getId(), melnyk.getFullName(),
                new PhoneNumber("+380674445566"), 4);
        equal("+380674445566", melnyk.getPhone().getNumber(), "телефон агента");
        equal(4, melnyk.getExperienceYears(), "стаж агента");

        Client tkachuk = client(c, "Ткачук");
        c.updateClient(tkachuk.getId(), tkachuk.getFullName(), tkachuk.getPhone(),
                tkachuk.getBirthDate(), melnyk.getId());
        equal(melnyk, tkachuk.getAgent(), "новий агент клієнта");
        equal(melnyk, c.getAgentWithMostClients().orElseThrow(),
                "запит 5 після передачі клієнта");

        Policy p = policy(c, 3);
        String number = p.getPolicyNumber();
        c.updatePolicy(p.getId(), InsuranceType.AUTO, p.getPeriod(),
                p.getInsuredEvent(), 5200, p.getClient().getId());
        equal(5200.0, p.getCost(), "нова вартість");
        equal(number, p.getPolicyNumber(), "номер поліса не змінюється");
        equal("8991.67", String.format("%.2f", c.getAverageCost())
                .replace(',', '.'), "середня вартість після змін");
    }

    static void atomicEdit() {
        InsuranceCompany c = DemoData.create();
        Policy p = policy(c, 3);
        int clientId = p.getClient().getId();
        // вартість коректна, а опис страхового випадку порожній
        rejected(() -> c.updatePolicy(p.getId(), InsuranceType.LIFE, p.getPeriod(),
                "", 3000, clientId), "порожній страховий випадок");
        equal(4600.0, p.getCost(), "вартість не змінилася");
        equal(InsuranceType.AUTO, p.getInsuranceType(), "тип не змінився");
        equal("ДТП з вини третіх осіб", p.getInsuredEvent(), "опис не змінився");
    }

    static void integrity() {
        InsuranceCompany c = DemoData.create();
        Client savchenko = client(c, "Савченко");
        rejected(() -> c.updateClient(savchenko.getId(), savchenko.getFullName(),
                savchenko.getPhone(), savchenko.getBirthDate(), -1),
                "неіснуючий агент");
        rejected(() -> c.updatePolicy(-1, InsuranceType.AUTO,
                new InsurancePeriod(LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 12, 31)), "ДТП", 3000,
                savchenko.getId()), "неіснуючий поліс");
        rejected(() -> c.addAgent(new Agent(FullName.parse("Новий Агент"),
                new PhoneNumber("+380687654321"), 1)), "чужий телефон");
        Agent foreign = new Agent(FullName.parse("Сторонній Агент"),
                new PhoneNumber("+380990000000"), 1);
        rejected(() -> c.addClient(new Client(FullName.parse("Новий Клієнт"),
                new PhoneNumber("+380990000001"), LocalDate.of(1990, 1, 1),
                foreign)), "агент не з компанії");
    }

    static void removal() {
        InsuranceCompany c = DemoData.create();
        int kovalenko = agent(c, "Коваленко").getId();
        rejected(() -> c.removeAgent(kovalenko), "агент із клієнтами");
        check(c.removeClient(client(c, "Ткачук").getId()), "клієнта видалено");
        equal(4, c.getPolicyCount(), "разом із клієнтом видалено 2 поліси");
        int first = policy(c, 0).getId();
        check(c.removePolicy(first), "поліс видалено");
        check(!c.removePolicy(first), "повторне видалення");
        equal(3, c.getPolicyCount(), "кількість полісів");
    }

    static void csvRoundTrip() throws Exception {
        InsuranceCompany original = DemoData.create();
        Path dir = Files.createTempDirectory("insurance-test");
        CsvStorage.save(original, dir);
        InsuranceCompany loaded = CsvStorage.load(dir, "Копія");
        equal(original.getAgents().size(), loaded.getAgents().size(), "агенти");
        equal(original.getClients().size(), loaded.getClients().size(), "клієнти");
        equal(original.getPolicyCount(), loaded.getPolicyCount(), "поліси");
        for (Policy p : original.getPolicies().getAll()) {
            Policy q = loaded.getPolicies().findById(p.getId()).orElseThrow();
            equal(p.toCsv(), q.toCsv(), "поліс " + p.getPolicyNumber());
            equal(p.getPolicyNumber(), q.getPolicyNumber(), "номер поліса");
        }
        equal(original.getAverageCost(), loaded.getAverageCost(),
                "середня вартість");
        equal("Коваленко", loaded.getAgentWithMostClients().orElseThrow()
                .getFullName().getLastName(), "запит 5 після читання");
    }

    static void csvDamaged() throws Exception {
        Path dir = Files.createTempDirectory("insurance-bad");
        CsvStorage.save(DemoData.create(), dir);
        Path clients = dir.resolve(CsvStorage.CLIENTS_FILE);
        List<String> lines = Files.readAllLines(clients, StandardCharsets.UTF_8);
        lines.set(2, lines.get(2).replace("1985-11-02", "02.11.1985"));
        Files.write(clients, lines, StandardCharsets.UTF_8);
        try {
            CsvStorage.load(dir, "Пошкоджена");
        } catch (DataFileException e) {
            equal(CsvStorage.CLIENTS_FILE, e.getFileName(), "файл з помилкою");
            equal(3, e.getLineNumber(), "номер рядка");
            System.out.println("       повідомлення: " + e.getMessage());
            return;
        }
        throw new AssertionError("пошкоджений файл не відхилено");
    }
}
