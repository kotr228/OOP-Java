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

/** Консольна демонстрація роботи класів, запитів і редагування. */
public class ConsoleDemo {

    public static void main(String[] args) {
        InsuranceCompany company = DemoData.create();

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

        System.out.println("\n=== Редагування через InsuranceCompany ===");
        demoEditing(company);

        System.out.println("\n=== Перевірка некоректних даних ===");
        tryCreate(() -> new PhoneNumber("050-123"));
        tryCreate(() -> new InsurancePeriod(LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 1, 1)));
        tryCreate(() -> company.getPolicies().getAll().get(0).setCost(-500));
        tryCreate(() -> Agent.setCommissionRate(0.9));
    }

    private static void demoEditing(InsuranceCompany company) {
        Agent melnyk = company.getAgents().findById(2).orElseThrow();
        company.updateAgent(2, melnyk.getFullName(),
                new PhoneNumber("+380674445566"), 4);
        System.out.println("   Агент " + melnyk + ": новий телефон "
                + melnyk.getPhone().getFormatted() + ", стаж "
                + melnyk.getExperienceYears() + " р.");

        Client tkachuk = company.getClients().findById(2).orElseThrow();
        company.updateClient(2, tkachuk.getFullName(), tkachuk.getPhone(),
                tkachuk.getBirthDate(), 2);
        System.out.println("   Клієнта " + tkachuk + " передано агенту "
                + tkachuk.getAgent());

        Policy policy = company.getPolicies().findById(4).orElseThrow();
        company.updatePolicy(4, policy.getInsuranceType(), policy.getPeriod(),
                policy.getInsuredEvent(), 5200, policy.getClient().getId());
        System.out.println("   Поліс " + policy + ": нова вартість "
                + policy.getFormattedCost());

        System.out.println("   Кількість клієнтів у агентів після змін:");
        Map<Agent, Integer> counts = company.countClientsByAgent();
        for (Map.Entry<Agent, Integer> e : counts.entrySet()) {
            System.out.println("      " + e.getKey() + " — " + e.getValue());
        }
        System.out.printf("   Середня вартість після змін: %.2f грн%n",
                company.getAverageCost());

        tryCreate(() -> company.updateClient(3, FullName.parse("Савченко Дмитро"),
                new PhoneNumber("+380509876543"), LocalDate.of(2001, 7, 25), 99));
        tryCreate(() -> company.updatePolicy(42, InsuranceType.AUTO,
                policy.getPeriod(), "ДТП", 3000, 3));
        tryCreate(() -> company.updateAgent(1, FullName.parse("Коваленко Олена"),
                new PhoneNumber("+380687654321"), 8));
        tryCreate(() -> company.updatePolicy(4, InsuranceType.AUTO,
                policy.getPeriod(), "", 3000, 3));
        System.out.println("   Поліс " + policy + " не змінився: "
                + policy.getFormattedCost()
                + ", «" + policy.getInsuredEvent() + "»");
    }

    private static void tryCreate(Runnable action) {
        try {
            action.run();
            System.out.println("   Дані прийнято");
        } catch (InvalidDataException e) {
            System.out.println("   Помилка: " + e.getMessage());
        }
    }
}
