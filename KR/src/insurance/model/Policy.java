package insurance.model;

import java.time.LocalDate;

/** Страховий поліс (договір страхування). */
public final class Policy
        implements Identifiable, Describable, Priceable, CsvSerializable {
    private static int nextId = 1;
    public static final double MAX_COST = 1_000_000;

    private final int id;
    private final String policyNumber;
    private InsuranceType insuranceType;
    private InsurancePeriod period;     // композиція: дати початку і завершення
    private String insuredEvent;        // страховий випадок
    private double cost;
    private Client client;              // асоціація: страхувальник

    public Policy(InsuranceType type, InsurancePeriod period, String insuredEvent,
                  double cost, Client client) {
        this(nextId, type, period, insuredEvent, cost, client);
    }

    public Policy(int id, InsuranceType type, InsurancePeriod period,
                  String insuredEvent, double cost, Client client) {
        if (id <= 0) {
            throw new InvalidDataException("Id має бути додатним числом");
        }
        this.id = id;
        this.policyNumber = generateNumber(id, period);
        setInsuranceType(type);
        setPeriod(period);
        setInsuredEvent(insuredEvent);
        setCost(cost);
        setClient(client);
        nextId = Math.max(nextId, id + 1);
    }

    /** Номер поліса формується з року початку дії та id: «ПЛ-2026-0007». */
    private static String generateNumber(int id, InsurancePeriod period) {
        int year = period == null
                ? LocalDate.now().getYear()
                : period.getStartDate().getYear();
        return String.format("ПЛ-%d-%04d", year, id);
    }

    @Override
    public int getId() {
        return id;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public InsuranceType getInsuranceType() {
        return insuranceType;
    }

    public void setInsuranceType(InsuranceType insuranceType) {
        if (insuranceType == null) {
            throw new InvalidDataException("Тип страхування має бути заданий");
        }
        this.insuranceType = insuranceType;
    }

    public InsurancePeriod getPeriod() {
        return period;
    }

    public void setPeriod(InsurancePeriod period) {
        if (period == null) {
            throw new InvalidDataException("Період дії поліса має бути заданий");
        }
        this.period = period;
    }

    public String getInsuredEvent() {
        return insuredEvent;
    }

    public void setInsuredEvent(String insuredEvent) {
        if (insuredEvent == null || insuredEvent.isBlank()) {
            throw new InvalidDataException("Страховий випадок має бути описаний");
        }
        if (insuredEvent.contains(SEPARATOR)) {
            throw new InvalidDataException(
                    "Опис не може містити символ «" + SEPARATOR + "»");
        }
        this.insuredEvent = insuredEvent.trim();
    }

    @Override
    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        if (cost <= 0 || cost > MAX_COST) {
            throw new InvalidDataException(
                    "Вартість має бути більшою за 0 і не перевищувати "
                    + String.format("%.0f", MAX_COST) + " " + CURRENCY);
        }
        this.cost = cost;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        if (client == null) {
            throw new InvalidDataException("Поліс має бути оформлений на клієнта");
        }
        this.client = client;
    }

    public boolean isActiveOn(LocalDate date) {
        return period.isActiveOn(date);
    }

    @Override
    public String getInfo() {
        return "Поліс " + policyNumber + " (" + insuranceType + ")"
                + "\n  Клієнт: " + client + ", агент: " + client.getAgent()
                + "\n  Період дії: " + period
                + " (" + period.getDurationDays() + " дн.)"
                + "\n  Страховий випадок: " + insuredEvent
                + "\n  Вартість: " + getFormattedCost();
    }

    @Override
    public String toCsv() {
        return String.join(SEPARATOR, String.valueOf(id), insuranceType.name(),
                period.getStartDate().toString(), period.getEndDate().toString(),
                insuredEvent, String.valueOf(cost),
                String.valueOf(client.getId()));
    }

    @Override
    public String toString() {
        return policyNumber;
    }
}
