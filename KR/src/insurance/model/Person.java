package insurance.model;

/** Абстрактний базовий клас для людей, з якими працює страхова компанія. */
public abstract class Person implements Identifiable, Describable, CsvSerializable,
        Comparable<Person> {
    private final int id;
    private FullName fullName;      // композиція
    private PhoneNumber phone;      // композиція

    protected Person(int id, FullName fullName, PhoneNumber phone) {
        if (id <= 0) {
            throw new InvalidDataException("Id має бути додатним числом");
        }
        this.id = id;
        setFullName(fullName);
        setPhone(phone);
    }

    /** Назва ролі людини в системі («Агент», «Клієнт»). */
    public abstract String getRole();

    @Override
    public int getId() {
        return id;
    }

    public FullName getFullName() {
        return fullName;
    }

    public final void setFullName(FullName fullName) {
        if (fullName == null) {
            throw new InvalidDataException("ПІБ має бути задане");
        }
        this.fullName = fullName;
    }

    public PhoneNumber getPhone() {
        return phone;
    }

    public final void setPhone(PhoneNumber phone) {
        if (phone == null) {
            throw new InvalidDataException("Телефон має бути заданий");
        }
        this.phone = phone;
    }

    @Override
    public String getInfo() {
        return getRole() + " №" + id + ": " + fullName
                + "\n  Телефон: " + phone.getFormatted();
    }

    /** Люди впорядковуються за ПІБ. */
    @Override
    public int compareTo(Person other) {
        return fullName.compareTo(other.fullName);
    }

    @Override
    public String toString() {
        return fullName.getShortForm();
    }
}
