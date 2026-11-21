package io.github.kotr228.platform.core.repository;

import io.github.kotr228.platform.core.model.Identifiable;
import io.github.kotr228.platform.core.model.InvalidDataException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistryTest {

    record Item(int id, String name) implements Identifiable {
        @Override
        public int getId() {
            return id;
        }
    }

    @Test
    void issuesIdsIndependentlyPerRegistry() {
        Registry<Item> first = new Registry<>();
        Registry<Item> second = new Registry<>();
        assertEquals(1, first.nextId());
        first.add(new Item(first.nextId(), "a"));
        first.add(new Item(first.nextId(), "b"));
        assertEquals(3, first.nextId());
        assertEquals(1, second.nextId(), "інший реєстр не залежить від першого");
    }

    @Test
    void idsOfRemovedItemsAreNotReused() {
        Registry<Item> registry = new Registry<>();
        registry.add(new Item(1, "a"));
        registry.add(new Item(2, "b"));
        assertTrue(registry.remove(2));
        assertEquals(3, registry.nextId());
    }

    @Test
    void continuesAfterLoadedIds() {
        Registry<Item> registry = new Registry<>();
        registry.add(new Item(41, "з файлу"));
        assertEquals(42, registry.nextId());
    }

    @Test
    void rejectsDuplicatesAndNull() {
        Registry<Item> registry = new Registry<>();
        registry.add(new Item(1, "a"));
        assertThrows(InvalidDataException.class, () -> registry.add(new Item(1, "b")));
        assertThrows(InvalidDataException.class, () -> registry.add(null));
    }

    @Test
    void findsAndKeepsInsertionOrder() {
        Registry<Item> registry = new Registry<>();
        registry.add(new Item(5, "x"));
        registry.add(new Item(2, "y"));
        assertEquals(List.of(5, 2), registry.getAll().stream().map(Item::id).toList());
        assertEquals("y", registry.findById(2).orElseThrow().name());
        assertTrue(registry.findById(7).isEmpty());
        assertEquals(1, registry.findAll(i -> i.name().equals("x")).size());
        assertFalse(registry.remove(7));
    }

    @Test
    void listIsReadOnly() {
        Registry<Item> registry = new Registry<>();
        assertThrows(UnsupportedOperationException.class,
                () -> registry.getAll().add(new Item(1, "a")));
    }
}
