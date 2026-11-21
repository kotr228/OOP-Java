package io.github.kotr228.insurance.demo;

import io.github.kotr228.insurance.model.Agent;
import io.github.kotr228.insurance.model.Client;
import io.github.kotr228.insurance.model.InsurancePeriod;
import io.github.kotr228.insurance.model.InsuranceType;
import io.github.kotr228.insurance.service.InsuranceCompany;
import io.github.kotr228.platform.core.model.FullName;
import io.github.kotr228.platform.core.model.Money;
import io.github.kotr228.platform.core.model.PhoneNumber;

import java.time.LocalDate;

/** Початкові (демонстраційні) дані: 3 агенти, 4 клієнти, 6 полісів. */
public final class DemoData {
    public static final String COMPANY_NAME = "Надійний захист";

    private DemoData() {
    }

    /** Нова компанія з демонстраційними даними. */
    public static InsuranceCompany create() {
        InsuranceCompany company = new InsuranceCompany(COMPANY_NAME);
        fill(company);
        return company;
    }

    public static void fill(InsuranceCompany company) {
        Agent kovalenko = company.createAgent(
                FullName.parse("Коваленко Олена Петрівна"),
                new PhoneNumber("+380501234567"), 8);
        Agent melnyk = company.createAgent(
                FullName.parse("Мельник Андрій Іванович"),
                new PhoneNumber("+380673456789"), 3);
        Agent bondar = company.createAgent(
                FullName.parse("Бондар Ірина Сергіївна"),
                new PhoneNumber("+380931112233"), 12);

        Client shevchenko = company.createClient(
                FullName.parse("Шевченко Максим Олегович"),
                new PhoneNumber("+380661234500"),
                LocalDate.of(1990, 3, 14), kovalenko.getId());
        Client tkachuk = company.createClient(
                FullName.parse("Ткачук Наталія Василівна"),
                new PhoneNumber("+380952223344"),
                LocalDate.of(1985, 11, 2), kovalenko.getId());
        Client savchenko = company.createClient(
                FullName.parse("Савченко Дмитро Андрійович"),
                new PhoneNumber("+380509876543"),
                LocalDate.of(2001, 7, 25), melnyk.getId());
        Client lysenko = company.createClient(
                FullName.parse("Лисенко Оксана Миколаївна"),
                new PhoneNumber("+380687654321"),
                LocalDate.of(1978, 1, 9), bondar.getId());

        company.createPolicy(InsuranceType.AUTO,
                period(2026, 1, 15, 2027, 1, 14),
                "ДТП, викрадення автомобіля", Money.of(9800), shevchenko.getId());
        company.createPolicy(InsuranceType.HEALTH,
                period(2026, 2, 1, 2027, 1, 31),
                "Хвороба, госпіталізація", Money.of(7200), tkachuk.getId());
        company.createPolicy(InsuranceType.PROPERTY,
                period(2026, 3, 10, 2027, 3, 9),
                "Пожежа, затоплення квартири", Money.of(5400), tkachuk.getId());
        company.createPolicy(InsuranceType.AUTO,
                period(2026, 4, 1, 2026, 9, 30),
                "ДТП з вини третіх осіб", Money.of(4600), savchenko.getId());
        company.createPolicy(InsuranceType.LIFE,
                period(2025, 9, 1, 2035, 8, 31),
                "Смерть або інвалідність застрахованої особи", Money.of(25000),
                lysenko.getId());
        company.createPolicy(InsuranceType.TRAVEL,
                period(2026, 7, 1, 2026, 7, 21),
                "Медична допомога за кордоном", Money.of(1350), shevchenko.getId());
    }

    private static InsurancePeriod period(int y1, int m1, int d1,
                                          int y2, int m2, int d2) {
        return new InsurancePeriod(LocalDate.of(y1, m1, d1), LocalDate.of(y2, m2, d2));
    }
}
