package insurance.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Період дії страхового поліса (допоміжний клас для композиції). */
public class InsurancePeriod {
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

    /** Чи діє поліс на вказану дату. */
    public boolean isActiveOn(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    @Override
    public String toString() {
        return startDate + " – " + endDate;
    }
}
