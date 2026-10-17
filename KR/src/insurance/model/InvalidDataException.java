package insurance.model;

/** Виняток, що генерується під час спроби записати в об'єкт некоректні дані. */
public class InvalidDataException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;


    public InvalidDataException(String message) {
        super(message);
    }
}
