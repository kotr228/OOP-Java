package io.github.kotr228.insurance.service;

import io.github.kotr228.insurance.demo.DemoData;
import io.github.kotr228.insurance.model.Agent;
import io.github.kotr228.insurance.model.Client;
import io.github.kotr228.insurance.model.InsurancePeriod;
import io.github.kotr228.insurance.model.InsuranceType;
import io.github.kotr228.insurance.model.Policy;
import io.github.kotr228.insurance.model.PolicyStatus;
import io.github.kotr228.platform.core.model.FullName;
import io.github.kotr228.platform.core.model.InvalidDataException;
import io.github.kotr228.platform.core.model.Money;
import io.github.kotr228.platform.core.model.PhoneNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InsuranceCompanyTest {
    private InsuranceCompany company;

    @BeforeEach
    void setUp() {
        company = DemoData.create();
    }

    private Agent agent(String lastName) {
        return company.getAgents().stream()
                .filter(a -> a.getFullName().getLastName().equals(lastName))
                .findFirst().orElseThrow();
    }

    private Client client(String lastName) {
        return company.getClients().stream()
                .filter(c -> c.getFullName().getLastName().equals(lastName))
                .findFirst().orElseThrow();
    }

    private Policy policy(int index) {
        return company.getPolicies().get(index);
    }

    @Nested
    @DisplayName("Запити 1–6 (варіант 26)")
    class Queries {

        @Test
        void onDemoData() {
            List<Client> clients = company.getClientList();
            assertAll(
                    () -> assertEquals(4, clients.size()),
                    () -> assertEquals("Лисенко", clients.get(0).getFullName().getLastName()),
                    () -> assertEquals("Шевченко", clients.get(3).getFullName().getLastName()),
                    () -> assertEquals(6, company.getPolicyCount()),
                    () -> assertEquals(Money.of(25000),
                            company.getMostExpensivePolicy().orElseThrow().getCost()),
                    () -> assertEquals(new BigDecimal("8891.67"), company.getAverageCost()),
                    () -> assertEquals(agent("Коваленко"),
                            company.getAgentWithMostClients().orElseThrow()),
                    () -> assertEquals(InsuranceType.AUTO,
                            company.getMostPopularType().orElseThrow()));
        }

        @Test
        void onEmptyCompanyHaveNoResult() {
            InsuranceCompany empty = new InsuranceCompany("Порожня");
            assertAll(
                    () -> assertTrue(empty.getClientList().isEmpty()),
                    () -> assertEquals(0, empty.getPolicyCount()),
                    () -> assertTrue(empty.getMostExpensivePolicy().isEmpty()),
                    () -> assertEquals(Money.ZERO, empty.getAverageCost()),
                    () -> assertTrue(empty.getAgentWithMostClients().isEmpty()),
                    () -> assertTrue(empty.getMostPopularType().isEmpty()));
        }

        @Test
        void tieForTopAgentIsResolvedBySmallerId() {
            InsuranceCompany c = new InsuranceCompany("Рівність");
            Agent first = c.createAgent(FullName.parse("Перший Агент"),
                    new PhoneNumber("+380500000001"), 1);
            c.createAgent(FullName.parse("Другий Агент"),
                    new PhoneNumber("+380500000002"), 1);
            assertEquals(first, c.getAgentWithMostClients().orElseThrow());
        }
    }

    @Nested
    @DisplayName("Перевірка значень")
    class Validation {

        @Test
        void invalidValuesAreRejected() {
            Agent kovalenko = agent("Коваленко");
            Policy p = policy(0);
            assertAll(
                    () -> assertThrows(InvalidDataException.class,
                            () -> new InsurancePeriod(LocalDate.of(2026, 5, 1),
                                    LocalDate.of(2026, 1, 1))),
                    () -> assertThrows(InvalidDataException.class,
                            () -> company.createClient(FullName.parse("Мала Олена"),
                                    new PhoneNumber("+380500000001"),
                                    LocalDate.now().minusYears(17), kovalenko.getId())),
                    () -> assertThrows(InvalidDataException.class,
                            () -> p.setCost(Money.ZERO)),
                    () -> assertThrows(InvalidDataException.class,
                            () -> p.setCost(Money.of(1_000_001))),
                    () -> assertThrows(InvalidDataException.class,
                            () -> p.setCost(new BigDecimal("100.001"))),
                    () -> assertThrows(InvalidDataException.class,
                            () -> p.setInsuredEvent("x".repeat(201))),
                    () -> assertThrows(InvalidDataException.class,
                            () -> Agent.calculateCommission(Money.of(1000),
                                    new BigDecimal("0.9"))));
        }

        @Test
        void commissionIsCalculatedInKopecks() {
            assertEquals(new BigDecimal("980.00"),
                    Agent.calculateCommission(Money.of(9800),
                            Agent.DEFAULT_COMMISSION_RATE));
        }

        @Test
        void policyStatusDependsOnDate() {
            Policy travel = policy(5);       // 01.07.2026 – 21.07.2026
            assertAll(
                    () -> assertEquals(PolicyStatus.PENDING,
                            travel.getStatusOn(LocalDate.of(2026, 6, 30))),
                    () -> assertEquals(PolicyStatus.ACTIVE,
                            travel.getStatusOn(LocalDate.of(2026, 7, 21))),
                    () -> assertEquals(PolicyStatus.EXPIRED,
                            travel.getStatusOn(LocalDate.of(2026, 7, 22))));
        }
    }

    @Nested
    @DisplayName("Створення і редагування")
    class Editing {

        @Test
        void editsAgentClientAndPolicy() {
            Agent melnyk = agent("Мельник");
            company.updateAgent(melnyk.getId(), melnyk.getFullName(),
                    new PhoneNumber("+380674445566"), 4);
            assertEquals("+380674445566", melnyk.getPhone().getNumber());
            assertEquals(4, melnyk.getExperienceYears());

            Client tkachuk = client("Ткачук");
            company.updateClient(tkachuk.getId(), tkachuk.getFullName(),
                    tkachuk.getPhone(), tkachuk.getBirthDate(), melnyk.getId());
            assertSame(melnyk, tkachuk.getAgent());
            assertEquals(melnyk, company.getAgentWithMostClients().orElseThrow());

            Policy p = policy(3);
            String number = p.getPolicyNumber();
            company.updatePolicy(p.getId(), InsuranceType.AUTO, p.getPeriod(),
                    p.getInsuredEvent(), Money.of(5200), p.getClient().getId());
            assertEquals(Money.of(5200), p.getCost());
            assertEquals(number, p.getPolicyNumber(), "номер поліса не змінюється");
            assertEquals(new BigDecimal("8991.67"), company.getAverageCost());
        }

        @Test
        void failedEditLeavesPolicyUnchanged() {
            Policy p = policy(3);
            assertThrows(InvalidDataException.class, () -> company.updatePolicy(
                    p.getId(), InsuranceType.LIFE, p.getPeriod(), "", Money.of(3000),
                    p.getClient().getId()));
            assertEquals(Money.of(4600), p.getCost());
            assertEquals(InsuranceType.AUTO, p.getInsuranceType());
            assertEquals("ДТП з вини третіх осіб", p.getInsuredEvent());
        }

        @Test
        void newObjectsGetNextIds() {
            Agent agent = company.createAgent(FullName.parse("Нова Агентка"),
                    new PhoneNumber("+380990000010"), 2);
            assertEquals(4, agent.getId());
            Policy policy = company.createPolicy(InsuranceType.TRAVEL,
                    policy(0).getPeriod(), "Втрата багажу", Money.of(800),
                    client("Ткачук").getId());
            assertEquals(7, policy.getId());
            assertEquals("ПЛ-2026-0007", policy.getPolicyNumber());
        }

        @Test
        void companiesDoNotShareIdCounters() {
            InsuranceCompany other = new InsuranceCompany("Інша");
            Agent first = other.createAgent(FullName.parse("Перший Агент"),
                    new PhoneNumber("+380500000001"), 1);
            assertEquals(1, first.getId(), "id не залежать від інших компаній");
        }
    }

    @Nested
    @DisplayName("Цілісність даних")
    class Integrity {

        @Test
        void referencesAndPhonesAreChecked() {
            Client savchenko = client("Савченко");
            assertAll(
                    () -> assertThrows(InvalidDataException.class,
                            () -> company.updateClient(savchenko.getId(),
                                    savchenko.getFullName(), savchenko.getPhone(),
                                    savchenko.getBirthDate(), -1)),
                    () -> assertThrows(InvalidDataException.class,
                            () -> company.updatePolicy(-1, InsuranceType.AUTO,
                                    policy(0).getPeriod(), "ДТП", Money.of(3000),
                                    savchenko.getId())),
                    () -> assertThrows(InvalidDataException.class,
                            () -> company.createAgent(FullName.parse("Новий Агент"),
                                    new PhoneNumber("+38 068 765 43 21"), 1)),
                    () -> assertThrows(InvalidDataException.class,
                            () -> company.createClient(FullName.parse("Новий Клієнт"),
                                    new PhoneNumber("+380990000001"),
                                    LocalDate.of(1990, 1, 1), 99)));
        }

        @Test
        void foreignAgentObjectIsRejected() {
            InsuranceCompany other = new InsuranceCompany("Інша");
            Agent foreign = other.createAgent(FullName.parse("Сторонній Агент"),
                    new PhoneNumber("+380990000000"), 1);  // той самий id 1, що й у Коваленко
            assertThrows(InvalidDataException.class, () -> company.addClient(
                    new Client(company.getClients().size() + 1,
                            FullName.parse("Новий Клієнт"),
                            new PhoneNumber("+380990000001"),
                            LocalDate.of(1990, 1, 1), foreign)));
        }

        @Test
        void removalFollowsIntegrityRules() {
            int kovalenko = agent("Коваленко").getId();
            assertThrows(InvalidDataException.class, () -> company.removeAgent(kovalenko));
            assertTrue(company.removeClient(client("Ткачук").getId()));
            assertEquals(4, company.getPolicyCount(), "разом із клієнтом видалено 2 поліси");
            int first = policy(0).getId();
            assertTrue(company.removePolicy(first));
            assertFalse(company.removePolicy(first));
            assertEquals(3, company.getPolicyCount());
        }

        @Test
        void listsCannotBeModifiedDirectly() {
            assertThrows(UnsupportedOperationException.class,
                    () -> company.getPolicies().clear());
        }
    }
}
