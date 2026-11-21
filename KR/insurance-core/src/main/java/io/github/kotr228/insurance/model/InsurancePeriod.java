package io.github.kotr228.insurance.model;

import io.github.kotr228.platform.core.model.InvalidDataException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/** Період дії страхового поліса (незмінний об'єкт-значення). */
public final class InsurancePeriod {
    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("dd.MM.uuuu");

    private final LocalDate startDate;
    private final LocalDate endDate;

    public InsurancePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new InvalidDataException(
                    "Дати початку та завершення мають бути задані");
        }
        if (!endDate.isAfter(startDate)) {
            throw new InvalidDataException(
                    "Дата завершення має бути пізнішою за дату початку");
        }
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    /** Тривалість періоду в днях. */
    public long getDurationDays() {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /** Чи діє поліс на вказану дату (обидві межі включно). */
    public boolean isActiveOn(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof InsurancePeriod other
                && startDate.equals(other.startDate)
                && endDate.equals(other.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, endDate);
    }

    @Override
    public String toString() {
        return startDate.format(DATE) + " – " + endDate.format(DATE);
    }
}
