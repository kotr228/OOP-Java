package io.github.kotr228.insurance.model;

import io.github.kotr228.platform.core.model.FullName;
import io.github.kotr228.platform.core.model.InvalidDataException;
import io.github.kotr228.platform.core.model.PhoneNumber;

import java.time.LocalDate;
import java.time.Period;

/** Клієнт страхової компанії, якого обслуговує певний агент. */
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
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < MIN_AGE || age > MAX_AGE) {
            throw new InvalidDataException("Вік клієнта має бути від "
                    + MIN_AGE + " до " + MAX_AGE + " років");
        }
        this.birthDate = birthDate;
    }

    public int getAge() {
        return Period.between(birthDate, LocalDate.now()).getYears();
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
        return super.getInfo() + "\n  Вік: " + getAge() + " р., агент: " + agent;
    }
}
