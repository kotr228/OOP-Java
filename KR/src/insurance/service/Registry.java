package insurance.service;

import insurance.model.Identifiable;
import insurance.model.InvalidDataException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Узагальнена колекція об'єктів з id.
 * ArrayList зберігає порядок додавання, HashMap дає швидкий пошук за id.
 */
public class Registry<T extends Identifiable> {
    private final List<T> items = new ArrayList<>();
    private final Map<Integer, T> byId = new HashMap<>();

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
    }

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

    public void clear() {
        items.clear();
        byId.clear();
    }
}
