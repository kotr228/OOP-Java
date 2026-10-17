package insurance.app;

import insurance.model.Agent;
import insurance.model.Client;
import insurance.model.FullName;
import insurance.model.InsurancePeriod;
import insurance.model.InsuranceType;
import insurance.model.InvalidDataException;
import insurance.model.PhoneNumber;
import insurance.model.Policy;
import insurance.service.InsuranceCompany;

import java.time.LocalDate;
import java.util.Map;

/** Демонстрація роботи класів і запитів (до створення графічного інтерфейсу). */
public class Main {

    public static void main(String[] args) {
        InsuranceCompany company = new InsuranceCompany("Надійний захист");
        fillDemoData(company);

        System.out.println("=== Страхова компанія «" + company.getName()
                + "» ===");
        System.out.println("Агентів: " + company.getAgents().size()
                + ", клієнтів: " + company.getClients().size()
                + ", полісів: " + company.getPolicies().size());

        System.out.println("\n1. Список клієнтів:");
        for (Client client : company.getClientList()) {
            System.out.println("   " + client.getFullName() + ", "
                    + client.getPhone().getFormatted()
                    + ", агент " + client.getAgent());
        }

        System.out.println("\n2. Кількість полісів: " + company.getPolicyCount());

        System.out.println("\n3. Найдорожчий поліс:");
        company.getMostExpensivePolicy()
                .ifPresent(p -> System.out.println("   " + p.getInfo()));

        System.out.printf("%n4. Середня вартість страхування: %.2f грн%n",
                company.getAverageCost());

        System.out.println("\n5. Кількість клієнтів у агентів:");
        Map<Agent, Integer> clientsByAgent = company.countClientsByAgent();
        for (Map.Entry<Agent, Integer> e : clientsByAgent.entrySet()) {
            System.out.println("   " + e.getKey() + " — " + e.getValue());
        }
        company.getAgentWithMostClients().ifPresent(a ->
                System.out.println("   Агент із найбільшою кількістю клієнтів: "
                        + a.getFullName()));

        System.out.println("\n6. Кількість полісів за типами страхування:");
        Map<InsuranceType, Integer> policiesByType = company.countPoliciesByType();
        for (Map.Entry<InsuranceType, Integer> e : policiesByType.entrySet()) {
            System.out.println("   " + e.getKey() + " — " + e.getValue());
        }
        company.getMostPopularType().ifPresent(t ->
                System.out.println("   Тип з найбільшим попитом: " + t));

        System.out.println("\n=== Перевірка некоректних даних ===");
        tryCreate(() -> new PhoneNumber("050-123"));
        tryCreate(() -> new InsurancePeriod(LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 1, 1)));
        tryCreate(() -> company.getPolicies().getAll().get(0).setCost(-500));
        tryCreate(() -> Agent.setCommissionRate(0.9));
    }

    private static void tryCreate(Runnable action) {
        try {
            action.run();
            System.out.println("   Дані прийнято");
        } catch (InvalidDataException e) {
            System.out.println("   Помилка: " + e.getMessage());
        }
    }

    private static void fillDemoData(InsuranceCompany company) {
        Agent kovalenko = new Agent(FullName.parse("Коваленко Олена Петрівна"),
                new PhoneNumber("+380501234567"), 8);
        Agent melnyk = new Agent(FullName.parse("Мельник Андрій Іванович"),
                new PhoneNumber("+380673456789"), 3);
        Agent bondar = new Agent(FullName.parse("Бондар Ірина Сергіївна"),
                new PhoneNumber("+380931112233"), 12);
        company.addAgent(kovalenko);
        company.addAgent(melnyk);
        company.addAgent(bondar);

        Client shevchenko = new Client(FullName.parse("Шевченко Максим Олегович"),
                new PhoneNumber("+380661234500"),
                LocalDate.of(1990, 3, 14), kovalenko);
        Client tkachuk = new Client(FullName.parse("Ткачук Наталія Василівна"),
                new PhoneNumber("+380952223344"),
                LocalDate.of(1985, 11, 2), kovalenko);
        Client savchenko = new Client(FullName.parse("Савченко Дмитро Андрійович"),
                new PhoneNumber("+380509876543"),
                LocalDate.of(2001, 7, 25), melnyk);
        Client lysenko = new Client(FullName.parse("Лисенко Оксана Миколаївна"),
                new PhoneNumber("+380687654321"),
                LocalDate.of(1978, 1, 9), bondar);
        company.addClient(shevchenko);
        company.addClient(tkachuk);
        company.addClient(savchenko);
        company.addClient(lysenko);

        company.addPolicy(new Policy(InsuranceType.AUTO,
                new InsurancePeriod(LocalDate.of(2026, 1, 15),
                        LocalDate.of(2027, 1, 14)),
                "ДТП, викрадення автомобіля", 9800, shevchenko));
        company.addPolicy(new Policy(InsuranceType.HEALTH,
                new InsurancePeriod(LocalDate.of(2026, 2, 1),
                        LocalDate.of(2027, 1, 31)),
                "Хвороба, госпіталізація", 7200, tkachuk));
        company.addPolicy(new Policy(InsuranceType.PROPERTY,
                new InsurancePeriod(LocalDate.of(2026, 3, 10),
                        LocalDate.of(2027, 3, 9)),
                "Пожежа, затоплення квартири", 5400, tkachuk));
        company.addPolicy(new Policy(InsuranceType.AUTO,
                new InsurancePeriod(LocalDate.of(2026, 4, 1),
                        LocalDate.of(2026, 9, 30)),
                "ДТП з вини третіх осіб", 4600, savchenko));
        company.addPolicy(new Policy(InsuranceType.LIFE,
                new InsurancePeriod(LocalDate.of(2025, 9, 1),
                        LocalDate.of(2035, 8, 31)),
                "Смерть або інвалідність застрахованої особи", 25000, lysenko));
        company.addPolicy(new Policy(InsuranceType.TRAVEL,
                new InsurancePeriod(LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 21)),
                "Медична допомога за кордоном", 1350, shevchenko));
    }
}
