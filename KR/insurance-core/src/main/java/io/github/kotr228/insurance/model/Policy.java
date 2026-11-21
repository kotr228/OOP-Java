package io.github.kotr228.insurance.model;

import io.github.kotr228.platform.core.model.Describable;
import io.github.kotr228.platform.core.model.Identifiable;
import io.github.kotr228.platform.core.model.InvalidDataException;
import io.github.kotr228.platform.core.model.Money;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Страховий поліс (договір страхування). */
public final class Policy implements Identifiable, Describable, Priceable {
    public static final BigDecimal MAX_COST = Money.of(1_000_000);
    public static final int MAX_EVENT_LENGTH = 200;

    private final int id;
    private final String policyNumber;
    private InsuranceType insuranceType;
    private InsurancePeriod period;     // композиція: дати початку і завершення
    private String insuredEvent;        // страховий випадок
    private BigDecimal cost;
    private Client client;              // асоціація: страхувальник

    /**
     * @param id унікальний id; для нового поліса —
     *           див. {@link io.github.kotr228.insurance.service.InsuranceCompany#createPolicy}
     */
    public Policy(int id, InsuranceType type, InsurancePeriod period,
                  String insuredEvent, BigDecimal cost, Client client) {
        if (id <= 0) {
            throw new InvalidDataException("Id має бути додатним числом");
        }
        setInsuranceType(type);
        setPeriod(period);
        setInsuredEvent(insuredEvent);
        setCost(cost);
        setClient(client);
        this.id = id;
        this.policyNumber = generateNumber(id, period);
    }

    /** Номер поліса формується з року початку дії та id: «ПЛ-2026-0007». */
    private static String generateNumber(int id, InsurancePeriod period) {
        return String.format("ПЛ-%d-%04d", period.getStartDate().getYear(), id);
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
        String trimmed = insuredEvent.strip();
        if (trimmed.length() > MAX_EVENT_LENGTH) {
            throw new InvalidDataException("Опис страхового випадку має бути"
                    + " не довшим за " + MAX_EVENT_LENGTH + " символів");
        }
        this.insuredEvent = trimmed;
    }

    @Override
    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        BigDecimal normalized = Money.normalize(cost);
        if (normalized.signum() <= 0 || normalized.compareTo(MAX_COST) > 0) {
            throw new InvalidDataException(
                    "Вартість має бути більшою за 0 і не перевищувати "
                    + Money.formatWithCurrency(MAX_COST));
        }
        this.cost = normalized;
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

    /** Стан поліса на вказану дату. */
    public PolicyStatus getStatusOn(LocalDate date) {
        if (period.isActiveOn(date)) {
            return PolicyStatus.ACTIVE;
        }
        return period.getStartDate().isAfter(date)
                ? PolicyStatus.PENDING : PolicyStatus.EXPIRED;
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
    public String toString() {
        return policyNumber;
    }
}
