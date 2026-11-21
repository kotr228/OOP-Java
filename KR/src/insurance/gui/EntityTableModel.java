package insurance.gui;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Узагальнена модель таблиці: рядок — об'єкт T, стовпці задаються функціями. */
public class EntityTableModel<T> extends AbstractTableModel {
    private static final long serialVersionUID = 1L;

    /** Опис стовпця: заголовок, тип значень і спосіб отримати значення. */
    public record Column<T>(String title, Class<?> type,
                            Function<T, Object> value) {
    }

    private final transient List<Column<T>> columns;
    private final transient List<T> items = new ArrayList<>();

    public EntityTableModel(List<Column<T>> columns) {
        this.columns = List.copyOf(columns);
    }

    public void setItems(List<? extends T> newItems) {
        items.clear();
        items.addAll(newItems);
        fireTableDataChanged();
    }

    public T getItem(int row) {
        return items.get(row);
    }

    public int indexOf(T item) {
        return items.indexOf(item);
    }

    @Override
    public int getRowCount() {
        return items.size();
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public String getColumnName(int column) {
        return columns.get(column).title();
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return columns.get(column).type();
    }

    @Override
    public Object getValueAt(int row, int column) {
        return columns.get(column).value().apply(items.get(row));
    }
}
