package io.github.kotr228.platform.core.repository;

import io.github.kotr228.platform.core.model.Identifiable;
import io.github.kotr228.platform.core.model.InvalidDataException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Узагальнена колекція об'єктів з id (сховище в пам'яті).
 * ArrayList зберігає порядок додавання, HashMap дає швидкий пошук за id.
 * Реєстр сам видає id новим об'єктам ({@link #nextId()}), тому різні
 * реєстри (і різні підприємства в одній програмі) не впливають один на одного.
 */
public class Registry<T extends Identifiable> {
    private final List<T> items = new ArrayList<>();
    private final Map<Integer, T> byId = new HashMap<>();
    private int maxId;

    /** Id, який слід надати наступному новому об'єкту. */
    public int nextId() {
        return maxId + 1;
    }

    public void add(T item) {
        if (item == null) {
            throw new InvalidDataException("Не можна додати порожній об'єкт");
        }
        if (byId.containsKey(item.getId())) {
            throw new InvalidDataException(
                    "Об'єкт з id " + item.getId() + " уже існує");
        }
        items.add(item);
        byId.put(item.getId(), item);
        maxId = Math.max(maxId, item.getId());
    }

    /**
     * Видаляє об'єкт. Id видаленого об'єкта повторно не видається,
     * щоб посилання на нього в історії не вказували на інший об'єкт.
     */
    public boolean remove(int id) {
        T item = byId.remove(id);
        return item != null && items.remove(item);
    }

    public Optional<T> findById(int id) {
        return Optional.ofNullable(byId.get(id));
    }

    public List<T> findAll(Predicate<? super T> condition) {
        List<T> result = new ArrayList<>();
        for (T item : items) {
            if (condition.test(item)) {
                result.add(item);
            }
        }
        return result;
    }

    /** Список лише для читання: колекцію змінюють тільки через add/remove. */
    public List<T> getAll() {
        return Collections.unmodifiableList(items);
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        items.clear();
        byId.clear();
        maxId = 0;
    }
}
