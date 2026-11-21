package io.github.kotr228.insurance.model;

/** Стан поліса відносно певної дати. */
public enum PolicyStatus {
    ACTIVE("діє"),
    PENDING("ще не діє"),
    EXPIRED("завершено");

    private final String title;

    PolicyStatus(String title) {
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
