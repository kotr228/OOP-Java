package io.github.kotr228.insurance.model;

import io.github.kotr228.platform.core.model.FullName;
import io.github.kotr228.platform.core.model.InvalidDataException;
import io.github.kotr228.platform.core.model.PhoneNumber;

import java.time.LocalDate;
import java.time.Period;

/**
 * Клієнт страхової компанії, якого обслуговує певний агент.
 * <p>
 * Вік залежить від поточної дати, тому обмеження віку перевіряє
 * {@link io.github.kotr228.insurance.service.InsuranceCompany} під час
 * оформлення чи зміни дати народження, а не сам клієнт: інакше збережений
 * колись клієнт міг би «стати недійсним» і зламати завантаження даних.
 */
public final class Client extends Person {
    /** Договір страхування укладається лише з повнолітніми. */
    public static final int MIN_AGE = 18;
    public static final int MAX_AGE = 100;

    private LocalDate birthDate;
    private Agent agent;          // асоціація: агент, що обслуговує клієнта

    /**
     * @param id унікальний id; для нового клієнта —
     *           див. {@link io.github.kotr228.insurance.service.InsuranceCompany#createClient}
     */
    public Client(int id, FullName fullName, PhoneNumber phone,
                  LocalDate birthDate, Agent agent) {
        super(id, fullName, phone);
        setBirthDate(birthDate);
        setAgent(agent);
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        if (birthDate == null) {
            throw new InvalidDataException("Дата народження має бути задана");
        }
        this.birthDate = birthDate;
    }

    /** Кількість повних років на вказану дату. */
    public int getAgeOn(LocalDate date) {
        return Period.between(birthDate, date).getYears();
    }

    /** Чи може особа з такою датою народження укласти договір на вказану дату. */
    public static boolean isEligibleAge(LocalDate birthDate, LocalDate date) {
        int age = Period.between(birthDate, date).getYears();
        return !birthDate.isAfter(date) && age >= MIN_AGE && age <= MAX_AGE;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        if (agent == null) {
            throw new InvalidDataException("Клієнта має обслуговувати агент");
        }
        this.agent = agent;
    }

    @Override
    public String getRole() {
        return "Клієнт";
    }

    @Override
    public String getInfo() {
        return super.getInfo() + "\n  Дата народження: " + birthDate
                + ", агент: " + agent;
    }
}
