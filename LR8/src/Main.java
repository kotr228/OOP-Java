import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Покупний виріб (з ЛР2). */
class PurchasedItem {
    private String name;
    private int quantity;
    private String supplier;

    public PurchasedItem(String name, int quantity, String supplier) {
        setName(name);
        setQuantity(quantity);
        setSupplier(supplier);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Назва виробу не може бути порожньою");
        }
        this.name = name.trim();
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Кількість виробів має бути більшою за 0");
        }
        this.quantity = quantity;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        if (supplier == null || supplier.isBlank()) {
            throw new IllegalArgumentException("Назва постачальника не може бути порожньою");
        }
        this.supplier = supplier.trim();
    }

    @Override
    public String toString() {
        return name + " — " + quantity + " шт., постачальник: " + supplier;
    }
}

/** Вимірювальний прилад (з ЛР2). */
class MeasuringDevice {
    public static final String STATE_OFF = "вимкнено";
    public static final String STATE_ON = "увімкнено";
    public static final String STATE_FAULTY = "несправний";

    private String name;
    private double weight;
    private final List<PurchasedItem> purchasedItems = new ArrayList<>();
    private String state;

    public MeasuringDevice(String name, double weight, List<PurchasedItem> items) {
        setName(name);
        setWeight(weight);
        if (items != null) {
            purchasedItems.addAll(items);
        }
        state = STATE_OFF;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Найменування приладу не може бути порожнім");
        }
        this.name = name.trim();
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        if (weight <= 0) {
            throw new IllegalArgumentException("Вага має бути більшою за 0");
        }
        this.weight = weight;
    }

    public List<PurchasedItem> getPurchasedItems() {
        return Collections.unmodifiableList(purchasedItems);
    }

    public void addPurchasedItem(PurchasedItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Виріб не може бути null");
        }
        purchasedItems.add(item);
    }

    public boolean removePurchasedItem(String itemName) {
        return purchasedItems.removeIf(item -> item.getName().equalsIgnoreCase(itemName));
    }

    public int getTotalPurchasedQuantity() {
        int total = 0;
        for (PurchasedItem item : purchasedItems) {
            total += item.getQuantity();
        }
        return total;
    }

    public String getState() {
        return state;
    }

    public boolean turnOn() {
        if (state.equals(STATE_FAULTY)) {
            return false;
        }
        state = STATE_ON;
        return true;
    }

    public void turnOff() {
        if (state.equals(STATE_ON)) {
            state = STATE_OFF;
        }
    }

    public void breakDown() {
        state = STATE_FAULTY;
    }

    public void repair() {
        if (state.equals(STATE_FAULTY)) {
            state = STATE_OFF;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Прилад: ").append(name).append('\n');
        sb.append("Вага: ").append(weight).append(" кг\n");
        sb.append("Стан: ").append(state).append('\n');
        sb.append("Покупні вироби (всього ").append(getTotalPurchasedQuantity()).append(" шт.):");
        if (purchasedItems.isEmpty()) {
            sb.append(" немає");
        } else {
            for (PurchasedItem item : purchasedItems) {
                sb.append("\n  - ").append(item);
            }
        }
        return sb.toString();
    }
}

/** Головне вікно програми. */
class DeviceManagerFrame extends JFrame {
    private final DefaultListModel<MeasuringDevice> listModel = new DefaultListModel<>();
    private final JList<MeasuringDevice> deviceList = new JList<>(listModel);

    private final JTextField nameField = new JTextField(18);
    private final JTextField weightField = new JTextField(6);
    private final JLabel stateLabel = new JLabel("—");

    private final DefaultTableModel itemsModel = new DefaultTableModel(
            new String[]{"Назва виробу", "Кількість", "Постачальник"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable itemsTable = new JTable(itemsModel);
    private final JTextField itemNameField = new JTextField(14);
    private final JTextField itemQtyField = new JTextField(4);
    private final JTextField itemSupplierField = new JTextField(14);

    private final JLabel statusLabel = new JLabel(" ");

    DeviceManagerFrame() {
        super("ЛР8. Облік вимірювальних приладів");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        // Список приладів ліворуч
        deviceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        deviceList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                MeasuringDevice d = (MeasuringDevice) value;
                setText(d.getName() + "  [" + d.getState() + "]");
                return this;
            }
        });
        deviceList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelected();
            }
        });
        JScrollPane listScroll = new JScrollPane(deviceList);
        listScroll.setPreferredSize(new Dimension(280, 0));
        listScroll.setBorder(BorderFactory.createTitledBorder("Прилади"));
        add(listScroll, BorderLayout.WEST);

        // Права частина: дані, стан, покупні вироби
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.add(fixHeight(createDevicePanel()));
        right.add(fixHeight(createStatePanel()));
        right.add(createItemsPanel());
        add(right, BorderLayout.CENTER);

        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 6, 8));
        add(statusLabel, BorderLayout.SOUTH);

        addSampleData();
        setSize(980, 600);
        setLocationRelativeTo(null);
    }

    // ---------- Побудова інтерфейсу ----------

    private static JPanel fixHeight(JPanel panel) {
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
        return panel;
    }

    private static JButton button(String text, ActionListener listener) {
        JButton b = new JButton(text);
        b.addActionListener(listener);
        return b;
    }

    private JPanel createDevicePanel() {
        JPanel fields = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fields.add(new JLabel("Найменування:"));
        fields.add(nameField);
        fields.add(new JLabel("Вага, кг:"));
        fields.add(weightField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(button("Додати", e -> addDevice()));
        buttons.add(button("Зберегти зміни", e -> updateDevice()));
        buttons.add(button("Видалити", e -> deleteDevice()));
        buttons.add(button("Детально", e -> showDetails()));
        buttons.add(button("Очистити поля", e -> clearFields()));

        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.setBorder(BorderFactory.createTitledBorder("Дані приладу"));
        panel.add(fields);
        panel.add(buttons);
        return panel;
    }

    private JPanel createStatePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Стан приладу"));
        panel.add(new JLabel("Поточний стан:"));
        stateLabel.setFont(stateLabel.getFont().deriveFont(Font.BOLD, 14f));
        stateLabel.setPreferredSize(new Dimension(110, 20));
        panel.add(stateLabel);
        panel.add(button("Увімкнути", e -> turnOn()));
        panel.add(button("Вимкнути", e -> changeState(MeasuringDevice::turnOff, "вимкнено")));
        panel.add(button("Несправність", e -> changeState(MeasuringDevice::breakDown,
                "зафіксовано несправність")));
        panel.add(button("Відремонтувати", e -> changeState(MeasuringDevice::repair,
                "відремонтовано")));
        return panel;
    }

    private JPanel createItemsPanel() {
        itemsTable.setFillsViewportHeight(true);
        itemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel inputs = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputs.add(new JLabel("Назва:"));
        inputs.add(itemNameField);
        inputs.add(new JLabel("К-сть:"));
        inputs.add(itemQtyField);
        inputs.add(new JLabel("Постачальник:"));
        inputs.add(itemSupplierField);
        inputs.add(button("Додати виріб", e -> addItem()));
        inputs.add(button("Видалити виріб", e -> removeItem()));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Покупні вироби"));
        panel.add(new JScrollPane(itemsTable), BorderLayout.CENTER);
        panel.add(inputs, BorderLayout.SOUTH);
        return panel;
    }

    private void addSampleData() {
        List<PurchasedItem> voltItems = new ArrayList<>();
        voltItems.add(new PurchasedItem("Резистор 10 кОм", 12, "ТОВ «Радіодеталь»"));
        voltItems.add(new PurchasedItem("РК-індикатор", 1, "ПП «Дисплей-Сервіс»"));
        listModel.addElement(new MeasuringDevice("Вольтметр В7-40", 2.5, voltItems));

        List<PurchasedItem> thermoItems = new ArrayList<>();
        thermoItems.add(new PurchasedItem("Термопара типу K", 1, "ТОВ «Термоконтакт»"));
        listModel.addElement(new MeasuringDevice("Термометр ТЦ-1", 0.3, thermoItems));

        deviceList.setSelectedIndex(0);
        status("Завантажено " + listModel.size() + " прилади.");
    }

    // ---------- Допоміжні методи ----------

    private MeasuringDevice getSelected() {
        MeasuringDevice d = deviceList.getSelectedValue();
        if (d == null) {
            showError("Спочатку оберіть прилад у списку.");
        }
        return d;
    }

    private double parseWeight() {
        return Double.parseDouble(weightField.getText().trim().replace(',', '.'));
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Помилка", JOptionPane.ERROR_MESSAGE);
        status("Помилка: " + message);
    }

    private void status(String message) {
        statusLabel.setText(message);
    }

    private void refresh() {
        deviceList.repaint();
        showSelected();
    }

    /** Показує дані вибраного приладу у формі, мітці стану й таблиці. */
    private void showSelected() {
        MeasuringDevice d = deviceList.getSelectedValue();
        itemsModel.setRowCount(0);
        if (d == null) {
            stateLabel.setText("—");
            stateLabel.setForeground(Color.BLACK);
            return;
        }
        nameField.setText(d.getName());
        weightField.setText(String.valueOf(d.getWeight()));

        stateLabel.setText(d.getState());
        if (d.getState().equals(MeasuringDevice.STATE_ON)) {
            stateLabel.setForeground(new Color(0, 140, 0));
        } else if (d.getState().equals(MeasuringDevice.STATE_FAULTY)) {
            stateLabel.setForeground(Color.RED);
        } else {
            stateLabel.setForeground(Color.GRAY);
        }

        for (PurchasedItem item : d.getPurchasedItems()) {
            itemsModel.addRow(new Object[]{item.getName(), item.getQuantity(), item.getSupplier()});
        }
    }

    // ---------- Робота з приладами ----------

    private void addDevice() {
        try {
            MeasuringDevice d = new MeasuringDevice(nameField.getText(), parseWeight(), null);
            listModel.addElement(d);
            deviceList.setSelectedValue(d, true);
            status("Додано прилад «" + d.getName() + "».");
        } catch (NumberFormatException ex) {
            showError("Вага має бути числом.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void updateDevice() {
        MeasuringDevice d = getSelected();
        if (d == null) {
            return;
        }
        try {
            String name = nameField.getText();
            double weight = parseWeight();
            // Перевірка на тимчасовому об'єкті, щоб не змінити прилад частково
            new MeasuringDevice(name, weight, null);
            d.setName(name);
            d.setWeight(weight);
            refresh();
            status("Зміни приладу «" + d.getName() + "» збережено.");
        } catch (NumberFormatException ex) {
            showError("Вага має бути числом.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void deleteDevice() {
        MeasuringDevice d = getSelected();
        if (d == null) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Видалити прилад «" + d.getName() + "»?", "Підтвердження",
                JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            listModel.removeElement(d);
            clearFields();
            status("Прилад «" + d.getName() + "» видалено.");
        }
    }

    private void showDetails() {
        MeasuringDevice d = getSelected();
        if (d != null) {
            JOptionPane.showMessageDialog(this, d.toString(), "Інформація про прилад",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void clearFields() {
        nameField.setText("");
        weightField.setText("");
        deviceList.clearSelection();
    }

    // ---------- Керування станом ----------

    private void turnOn() {
        MeasuringDevice d = getSelected();
        if (d == null) {
            return;
        }
        if (d.turnOn()) {
            status("Прилад «" + d.getName() + "» увімкнено.");
        } else {
            showError("Несправний прилад не можна увімкнути. Спочатку відремонтуйте його.");
        }
        refresh();
    }

    private void changeState(java.util.function.Consumer<MeasuringDevice> action, String message) {
        MeasuringDevice d = getSelected();
        if (d == null) {
            return;
        }
        action.accept(d);
        refresh();
        status("Прилад «" + d.getName() + "»: " + message + ".");
    }

    // ---------- Покупні вироби ----------

    private void addItem() {
        MeasuringDevice d = getSelected();
        if (d == null) {
            return;
        }
        try {
            int quantity = Integer.parseInt(itemQtyField.getText().trim());
            PurchasedItem item = new PurchasedItem(itemNameField.getText(), quantity,
                    itemSupplierField.getText());
            d.addPurchasedItem(item);
            itemNameField.setText("");
            itemQtyField.setText("");
            itemSupplierField.setText("");
            refresh();
            status("До приладу «" + d.getName() + "» додано виріб «" + item.getName() + "».");
        } catch (NumberFormatException ex) {
            showError("Кількість має бути цілим числом.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void removeItem() {
        MeasuringDevice d = getSelected();
        if (d == null) {
            return;
        }
        int row = itemsTable.getSelectedRow();
        if (row == -1) {
            showError("Оберіть виріб у таблиці.");
            return;
        }
        String itemName = (String) itemsModel.getValueAt(row, 0);
        d.removePurchasedItem(itemName);
        refresh();
        status("Виріб «" + itemName + "» видалено.");
    }
}

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DeviceManagerFrame().setVisible(true));
    }
}