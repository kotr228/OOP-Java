package insurance.model;

import java.time.LocalDate;
import java.time.Period;

/** Клієнт страхової компанії, якого обслуговує певний агент. */
public final class Client extends Person {
    private static int nextId = 1;
    // договір страхування укладається лише з повнолітніми
    public static final int MIN_AGE = 18;

    private LocalDate birthDate;
    private Agent agent;          // асоціація: агент, що обслуговує клієнта

    public Client(FullName fullName, PhoneNumber phone, LocalDate birthDate,
                  Agent agent) {
        this(nextId, fullName, phone, birthDate, agent);
    }

    public Client(int id, FullName fullName, PhoneNumber phone,
                  LocalDate birthDate, Agent agent) {
        super(id, fullName, phone);
        setBirthDate(birthDate);
        setAgent(agent);
        nextId = Math.max(nextId, id + 1);
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        if (birthDate == null) {
            throw new InvalidDataException("Дата народження має бути задана");
        }
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < MIN_AGE || age > 100) {
            throw new InvalidDataException(
                    "Вік клієнта має бути від " + MIN_AGE + " до 100 років");
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

    @Override
    public String toCsv() {
        return String.join(SEPARATOR, String.valueOf(getId()),
                getFullName().toString(), getPhone().getNumber(),
                birthDate.toString(), String.valueOf(agent.getId()));
    }
}
