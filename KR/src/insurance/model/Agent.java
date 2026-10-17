package insurance.model;

/** Страховий агент, який укладає договори з клієнтами. */
public final class Agent extends Person {
    // Статичні дані: спільні для всіх агентів
    private static int nextId = 1;
    private static double commissionRate = 0.10;   // частка від вартості поліса

    private int experienceYears;

    public Agent(FullName fullName, PhoneNumber phone, int experienceYears) {
        this(nextId, fullName, phone, experienceYears);
    }

    /** Конструктор з явним id — для відновлення об'єктів з файлу. */
    public Agent(int id, FullName fullName, PhoneNumber phone,
                 int experienceYears) {
        super(id, fullName, phone);
        setExperienceYears(experienceYears);
        nextId = Math.max(nextId, id + 1);
    }

    public static double getCommissionRate() {
        return commissionRate;
    }

    public static void setCommissionRate(double rate) {
        if (rate < 0 || rate > 0.5) {
            throw new InvalidDataException(
                    "Комісія агента має бути в межах від 0 до 50 %");
        }
        commissionRate = rate;
    }

    public int getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(int experienceYears) {
        if (experienceYears < 0 || experienceYears > 60) {
            throw new InvalidDataException("Стаж має бути від 0 до 60 років");
        }
        this.experienceYears = experienceYears;
    }

    /** Комісійна винагорода агента за поліс указаної вартості. */
    public double calculateCommission(double policyCost) {
        return policyCost * commissionRate;
    }

    @Override
    public String getRole() {
        return "Агент";
    }

    @Override
    public String getInfo() {
        return super.getInfo() + "\n  Стаж: " + experienceYears + " р.";
    }

    @Override
    public String toCsv() {
        return String.join(SEPARATOR, String.valueOf(getId()),
                getFullName().toString(), getPhone().getNumber(),
                String.valueOf(experienceYears));
    }
}
