package insurance.model;

/** Об'єкт, що має грошову вартість. */
public interface Priceable {

    /** Валюта, у якій зберігаються всі суми в програмі. */
    String CURRENCY = "грн";

    double getCost();

    default String getFormattedCost() {
        return String.format("%.2f %s", getCost(), CURRENCY);
    }
}
