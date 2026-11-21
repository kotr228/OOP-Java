package io.github.kotr228.insurance.model;

import io.github.kotr228.platform.core.model.FullName;
import io.github.kotr228.platform.core.model.InvalidDataException;
import io.github.kotr228.platform.core.model.PhoneNumber;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Страховий агент, який укладає договори з клієнтами. */
public final class Agent extends Person {
    /** Комісія агента за замовчуванням — 10 % від вартості поліса. */
    public static final BigDecimal DEFAULT_COMMISSION_RATE = new BigDecimal("0.10");
    private static final BigDecimal MAX_COMMISSION_RATE = new BigDecimal("0.50");

    private int experienceYears;

    /**
     * @param id унікальний id; для нового агента —
     *           див. {@link io.github.kotr228.insurance.service.InsuranceCompany#createAgent}
     */
    public Agent(int id, FullName fullName, PhoneNumber phone,
                 int experienceYears) {
        super(id, fullName, phone);
        setExperienceYears(experienceYears);
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
    public static BigDecimal calculateCommission(BigDecimal policyCost,
                                                 BigDecimal rate) {
        if (rate.signum() < 0 || rate.compareTo(MAX_COMMISSION_RATE) > 0) {
            throw new InvalidDataException(
                    "Комісія агента має бути в межах від 0 до 50 %");
        }
        return policyCost.multiply(rate).setScale(2, RoundingMode.HALF_EVEN);
    }

    @Override
    public String getRole() {
        return "Агент";
    }

    @Override
    public String getInfo() {
        return super.getInfo() + "\n  Стаж: " + experienceYears + " р.";
    }
}
