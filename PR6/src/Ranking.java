import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Турнірна таблиця: узагальнений контейнер з пошуком мінімуму і максимуму. */
public class Ranking<T extends Comparable<T>> implements MinMax<T> {
    private final String title;
    private final List<T> items = new ArrayList<>();

    public Ranking(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void add(T item) {
        if (item == null) {
            throw new IllegalArgumentException("елемент таблиці не може бути null");
        }
        items.add(item);
    }

    public List<T> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public T min() {
        if (items.isEmpty()) {
            return null;
        }
        T result = items.get(0);
        for (T item : items) {
            if (item.compareTo(result) < 0) {   // строго менше — при рівності лишається перший
                result = item;
            }
        }
        return result;
    }

    @Override
    public T max() {
        if (items.isEmpty()) {
            return null;
        }
        T result = items.get(0);
        for (T item : items) {
            if (item.compareTo(result) > 0) {
                result = item;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "«" + title + "» (" + items.size() + " ел.): " + items;
    }
}