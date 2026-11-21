package io.github.kotr228.insurance.model;

import io.github.kotr228.platform.core.model.Money;

import java.math.BigDecimal;

/** Об'єкт, що має грошову вартість. */
public interface Priceable {

    BigDecimal getCost();

    default String getFormattedCost() {
        return Money.formatWithCurrency(getCost());
    }
}
