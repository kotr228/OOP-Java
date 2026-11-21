package io.github.kotr228.platform.swing;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableStringConverter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Типова вкладка «список записів»: кнопки «Додати / Редагувати / Видалити»,
 * поле пошуку і таблиця з сортуванням за стовпцями.
 * <ul>
 *   <li>подвійне клацання або Enter — редагування, Insert — додавання,
 *       Delete — видалення, Esc у полі пошуку — очищення пошуку;</li>
 *   <li>«Редагувати» і «Видалити» доступні лише тоді, коли вибрано рядок;</li>
 *   <li>дати, суми і цілі числа показуються у звичному форматі.</li>
 * </ul>
 *
 * @param <T> тип записів таблиці
 */
public class EntityTablePanel<T> extends JPanel {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final Color STRIPE = new Color(243, 246, 251);

    private final EntityTableModel<T> model;
    private final JTable table;
    private final transient TableRowSorter<TableModel> sorter;
    private final JTextField searchField = new JTextField(18);
    private final JLabel foundLabel = new JLabel(" ");
    private final JButton editButton;
    private final JButton deleteButton;

    /**
     * @param model   модель таблиці
     * @param addText напис на кнопці додавання («Додати», «Оформити поліс»…)
     * @param add     дія додавання
     * @param edit    дія редагування вибраного запису
     * @param delete  дія видалення вибраного запису
     * @param widths  бажані ширини стовпців
     */
    @SuppressWarnings("this-escape")      // слухачі реєструються в конструкторі
    public EntityTablePanel(EntityTableModel<T> model, String addText,
                            Runnable add, Consumer<T> edit, Consumer<T> delete,
                            int... widths) {
        super(new BorderLayout());
        this.model = model;
        this.table = createTable(model, widths);
        this.sorter = new TableRowSorter<>(model);
        sorter.setStringConverter(new TableStringConverter() {
            @Override
            public String toString(TableModel m, int row, int column) {
                return Formats.display(m.getValueAt(row, column));
            }
        });
        table.setRowSorter(sorter);

        Runnable editSelected = () -> getSelected().ifPresent(edit);
        Runnable deleteSelected = () -> getSelected().ifPresent(delete);
        JButton addButton = button(addText, "Додати новий запис (Insert)", add);
        editButton = button("Редагувати",
                "Змінити вибраний запис (Enter або подвійне клацання)",
                editSelected);
        deleteButton = button("Видалити", "Видалити вибраний запис (Delete)",
                deleteSelected);

        table.getSelectionModel().addListSelectionListener(e -> updateButtons());
        model.addTableModelListener(e -> {
            applyFilter();
            SwingUtilities.invokeLater(this::updateButtons);
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.rowAtPoint(e.getPoint()) >= 0) {
                    editSelected.run();
                }
            }
        });
        bindKey(table, KeyEvent.VK_ENTER, "edit", editSelected);
        bindKey(table, KeyEvent.VK_DELETE, "delete", deleteSelected);
        bindKey(table, KeyEvent.VK_INSERT, "add", add);

        searchField.setToolTipText("Показати лише рядки, що містять введений текст");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });
        bindKey(searchField, KeyEvent.VK_ESCAPE, "clear", this::clearSearch);
        foundLabel.setForeground(Color.GRAY);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        buttonBar.add(addButton);
        buttonBar.add(editButton);
        buttonBar.add(deleteButton);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchBar.add(foundLabel);
        searchBar.add(new JLabel("Пошук:"));
        searchBar.add(searchField);

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(8, 2, 8, 2));
        top.add(buttonBar, BorderLayout.WEST);
        top.add(searchBar, BorderLayout.EAST);

        setBorder(BorderFactory.createEmptyBorder(0, 6, 6, 6));
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        updateButtons();
    }

    // ---------- Публічні операції ----------

    public JTable getTable() {
        return table;
    }

    /** Задає власний спосіб показу стовпця (наприклад, кольоровий статус). */
    public void setColumnRenderer(int column, TableCellRenderer renderer) {
        table.getColumnModel().getColumn(column).setCellRenderer(renderer);
    }

    /** Запис, вибраний у таблиці (з урахуванням сортування і пошуку). */
    public Optional<T> getSelected() {
        int row = table.getSelectedRow();
        return row < 0
                ? Optional.empty()
                : Optional.of(model.getItem(table.convertRowIndexToModel(row)));
    }

    /** Виділяє запис і прокручує до нього; якщо його приховав пошук — очищує пошук. */
    public void select(T item) {
        int row = model.indexOf(item);
        if (row < 0) {
            return;
        }
        int view = table.convertRowIndexToView(row);
        if (view < 0) {
            clearSearch();
            view = table.convertRowIndexToView(row);
        }
        table.setRowSelectionInterval(view, view);
        table.scrollRectToVisible(table.getCellRect(view, 0, true));
        table.requestFocusInWindow();
    }

    public void clearSearch() {
        searchField.setText("");
    }

    // ---------- Побудова ----------

    private static JTable createTable(EntityTableModel<?> model, int... widths) {
        JTable table = new JTable(model) {
            @Serial
            private static final long serialVersionUID = 1L;

            /** «Смугасті» рядки: легше читати довгі таблиці. */
            @Override
            public Component prepareRenderer(TableCellRenderer renderer,
                                             int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? getBackground() : STRIPE);
                }
                return c;
            }
        };
        for (int i = 0; i < widths.length && i < model.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.setDefaultRenderer(LocalDate.class,
                new FormattedCellRenderer(SwingConstants.CENTER));
        table.setDefaultRenderer(Integer.class,
                new FormattedCellRenderer(SwingConstants.CENTER));
        table.setDefaultRenderer(BigDecimal.class,
                new FormattedCellRenderer(SwingConstants.RIGHT));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int fontHeight = table.getFontMetrics(table.getFont()).getHeight();
        table.setRowHeight(fontHeight + 10);
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
        table.setGridColor(UIManager.getColor("Table.gridColor"));
        table.getTableHeader().setFont(
                table.getTableHeader().getFont().deriveFont(Font.BOLD));
        return table;
    }

    private static JButton button(String text, String tip, Runnable action) {
        JButton b = new JButton(text);
        b.setToolTipText(tip);
        b.addActionListener(e -> action.run());
        return b;
    }

    static void bindKey(JComponent component, int key, String name,
                        Runnable action) {
        component.getInputMap(JComponent.WHEN_FOCUSED)
                .put(KeyStroke.getKeyStroke(key, 0), name);
        component.getActionMap().put(name, new AbstractAction() {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    private void updateButtons() {
        boolean any = table.getSelectedRow() >= 0;
        editButton.setEnabled(any);
        deleteButton.setEnabled(any);
    }

    /** Залишає лише рядки, у яких хоч одна клітинка містить текст пошуку. */
    private void applyFilter() {
        String needle = searchField.getText().strip().toLowerCase(Locale.ROOT);
        if (needle.isEmpty()) {
            sorter.setRowFilter(null);
            foundLabel.setText(" ");
            return;
        }
        sorter.setRowFilter(new RowFilter<TableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> e) {
                for (int i = 0; i < e.getValueCount(); i++) {
                    if (e.getStringValue(i).toLowerCase(Locale.ROOT)
                            .contains(needle)) {
                        return true;
                    }
                }
                return false;
            }
        });
        foundLabel.setText("Знайдено: " + table.getRowCount() + " з "
                + model.getRowCount());
    }
}
