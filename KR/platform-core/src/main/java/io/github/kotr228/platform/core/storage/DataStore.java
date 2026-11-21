package io.github.kotr228.platform.core.storage;

import java.io.IOException;

/**
 * Постійне сховище даних програми (файли, база даних тощо).
 * Інтерфейс програми працює лише з цим інтерфейсом, тому спосіб
 * зберігання можна замінити, не змінюючи інтерфейс і бізнес-логіку.
 *
 * @param <T> корінь даних, що зберігається цілком (наприклад, компанія)
 */
public interface DataStore<T> {

    /** Чи є в сховищі збережені дані. */
    boolean exists();

    /**
     * Читає дані. Якщо дані пошкоджені, генерується DataFileException,
     * і жоден частково прочитаний об'єкт не повертається.
     */
    T load() throws IOException, DataFileException;

    /** Зберігає дані цілком (старі дані замінюються). */
    void save(T data) throws IOException;

    /** Опис розташування для повідомлень користувачу. */
    String location();
}
