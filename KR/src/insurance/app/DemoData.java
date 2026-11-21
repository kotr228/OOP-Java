package insurance.app;

import insurance.model.Agent;
import insurance.model.Client;
import insurance.model.FullName;
import insurance.model.InsurancePeriod;
import insurance.model.InsuranceType;
import insurance.model.PhoneNumber;
import insurance.model.Policy;
import insurance.service.InsuranceCompany;

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
