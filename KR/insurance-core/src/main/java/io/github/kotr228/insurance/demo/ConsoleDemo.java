package io.github.kotr228.insurance.demo;

import io.github.kotr228.insurance.model.Agent;
import io.github.kotr228.insurance.model.Client;
import io.github.kotr228.insurance.model.InsurancePeriod;
import io.github.kotr228.insurance.model.InsuranceType;
import io.github.kotr228.insurance.model.Policy;
import io.github.kotr228.insurance.service.InsuranceCompany;
import io.github.kotr228.platform.core.model.FullName;
import io.github.kotr228.platform.core.model.InvalidDataException;
import io.github.kotr228.platform.core.model.Money;
import io.github.kotr228.platform.core.model.PhoneNumber;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;

/** Консольна демонстрація роботи класів, запитів і редагування. */
public final class ConsoleDemo {
    private static final PrintStream OUT =
            new PrintStream(System.out, true, StandardCharsets.UTF_8);

    private ConsoleDemo() {
    }

    public static void main(String[] args) {
        InsuranceCompany company = DemoData.create();

        OUT.println("=== Страхова компанія «" + company.getName() + "» ===");
        OUT.println("Агентів: " + company.getAgentCount()
                + ", клієнтів: " + company.getClientCount()
                + ", полісів: " + company.getPolicyCount());

        OUT.println("\n1. Список клієнтів:");
        for (Client client : company.getClientList()) {
            OUT.println("   " + client.getFullName() + ", "
                    + client.getPhone().getFormatted()
                    + ", агент " + client.getAgent());
        }

        OUT.println("\n2. Кількість полісів: " + company.getPolicyCount());

        OUT.println("\n3. Найдорожчий поліс:");
        company.getMostExpensivePolicy()
                .ifPresent(p -> OUT.println("   " + p.getInfo()));

        OUT.println("\n4. Середня вартість страхування: "
                + Money.formatWithCurrency(company.getAverageCost()));

        OUT.println("\n5. Кількість клієнтів у агентів:");
        printCounts(company.countClientsByAgent());
        company.getAgentWithMostClients().ifPresent(a ->
                OUT.println("   Агент із найбільшою кількістю клієнтів: "
                        + a.getFullName()));

        OUT.println("\n6. Кількість полісів за типами страхування:");
        printCounts(company.countPoliciesByType());
        company.getMostPopularType().ifPresent(t ->
                OUT.println("   Тип з найбільшим попитом: " + t));

        OUT.println("\n=== Редагування через InsuranceCompany ===");
        demoEditing(company);

        OUT.println("\n=== Перевірка некоректних даних ===");
        tryAction(() -> new PhoneNumber("050-123"));
        tryAction(() -> new InsurancePeriod(LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 1, 1)));
        tryAction(() -> company.getPolicies().get(0).setCost(Money.of(-500)));
        tryAction(() -> company.getPolicies().get(0)
                .setCost(new java.math.BigDecimal("100.005")));
        tryAction(() -> Agent.calculateCommission(Money.of(1000),
                new java.math.BigDecimal("0.9")));
    }

    private static void printCounts(Map<?, Integer> counts) {
        for (Map.Entry<?, Integer> e : counts.entrySet()) {
            OUT.println("   " + e.getKey() + " — " + e.getValue());
        }
    }

    private static void demoEditing(InsuranceCompany company) {
        Agent melnyk = company.findAgent(2).orElseThrow();
        company.updateAgent(2, melnyk.getFullName(),
                new PhoneNumber("+380674445566"), 4);
        OUT.println("   Агент " + melnyk + ": новий телефон "
                + melnyk.getPhone().getFormatted() + ", стаж "
                + melnyk.getExperienceYears() + " р.");

        Client tkachuk = company.findClient(2).orElseThrow();
        company.updateClient(2, tkachuk.getFullName(), tkachuk.getPhone(),
                tkachuk.getBirthDate(), 2);
        OUT.println("   Клієнта " + tkachuk + " передано агенту "
                + tkachuk.getAgent());

        Policy policy = company.findPolicy(4).orElseThrow();
        company.updatePolicy(4, policy.getInsuranceType(), policy.getPeriod(),
                policy.getInsuredEvent(), Money.of(5200), policy.getClient().getId());
        OUT.println("   Поліс " + policy + ": нова вартість "
                + policy.getFormattedCost());

        OUT.println("   Кількість клієнтів у агентів після змін:");
        printCounts(company.countClientsByAgent());
        OUT.println("   Середня вартість після змін: "
                + Money.formatWithCurrency(company.getAverageCost()));

        tryAction(() -> company.updateClient(3, FullName.parse("Савченко Дмитро"),
                new PhoneNumber("+380509876543"), LocalDate.of(2001, 7, 25), 99));
        tryAction(() -> company.updatePolicy(42, InsuranceType.AUTO,
                policy.getPeriod(), "ДТП", Money.of(3000), 3));
        tryAction(() -> company.updateAgent(1, FullName.parse("Коваленко Олена"),
                new PhoneNumber("+380687654321"), 8));
        tryAction(() -> company.updatePolicy(4, InsuranceType.AUTO,
                policy.getPeriod(), "", Money.of(3000), 3));
        OUT.println("   Поліс " + policy + " не змінився: "
                + policy.getFormattedCost()
                + ", «" + policy.getInsuredEvent() + "»");
    }

    private static void tryAction(Runnable action) {
        try {
            action.run();
            OUT.println("   Дані прийнято");
        } catch (InvalidDataException e) {
            OUT.println("   Помилка: " + e.getMessage());
        }
    }
}
