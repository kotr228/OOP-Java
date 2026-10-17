package insurance.model;

/** Типи страхування, які пропонує компанія. */
public enum InsuranceType {
    LIFE("Страхування життя"),
    HEALTH("Медичне страхування"),
    AUTO("Автострахування"),
    PROPERTY("Страхування майна"),
    TRAVEL("Страхування подорожей");

    private final String title;

    InsuranceType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return title;
    }
}
