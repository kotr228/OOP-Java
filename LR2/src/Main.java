import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

enum DeviceState {
    OFF("вимкнено"),
    ON("увімкнено"),
    FAULTY("несправний");

    private final String title;

    DeviceState(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}

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
            throw new IllegalArgumentException("Кількість має бути більшою за 0");
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

class MeasuringDevice {
    private String name;
    private double weight;
    private final List<PurchasedItem> purchasedItems = new ArrayList<>();
    private DeviceState state;

    public MeasuringDevice(String name, double weight, List<PurchasedItem> items) {
        setName(name);
        setWeight(weight);
        if (items != null) {
            purchasedItems.addAll(items);
        }
        state = DeviceState.OFF;
    }

    public MeasuringDevice(Scanner scanner) {
        System.out.println("\n=== Введення даних приладу ===");
        setName(readNonEmptyLine(scanner, "Найменування приладу: "));
        setWeight(readPositiveDouble(scanner, "Вага, кг: "));

        int count = readInt(scanner, "Кількість покупних виробів: ", 0);
        for (int i = 0; i < count; i++) {
            System.out.println("Покупний виріб №" + (i + 1));
            String itemName = readNonEmptyLine(scanner, "  Назва: ");
            int quantity = readInt(scanner, "  Кількість: ", 1);
            String supplier = readNonEmptyLine(scanner, "  Постачальник: ");
            purchasedItems.add(new PurchasedItem(itemName, quantity, supplier));
        }
        state = DeviceState.OFF;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Найменування не може бути порожнім");
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

    public PurchasedItem findPurchasedItem(String itemName) {
        for (PurchasedItem item : purchasedItems) {
            if (item.getName().equalsIgnoreCase(itemName)) {
                return item;
            }
        }
        return null;
    }

    public int getTotalPurchasedQuantity() {
        int total = 0;
        for (PurchasedItem item : purchasedItems) {
            total += item.getQuantity();
        }
        return total;
    }


    public DeviceState getState() {
        return state;
    }

    public boolean turnOn() {
        if (state == DeviceState.FAULTY) {
            return false;
        }
        state = DeviceState.ON;
        return true;
    }

    public void turnOff() {
        if (state == DeviceState.ON) {
            state = DeviceState.OFF;
        }
    }

    public void breakDown() {
        state = DeviceState.FAULTY;
    }

    public void repair() {
        if (state == DeviceState.FAULTY) {
            state = DeviceState.OFF;
        }
    }

    public boolean isOn() {
        return state == DeviceState.ON;
    }

    public boolean isServiceable() {
        return state != DeviceState.FAULTY;
    }


    private static String readNonEmptyLine(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("Значення не може бути порожнім.");
        }
    }

    private static double readPositiveDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = Double.parseDouble(scanner.nextLine().trim().replace(',', '.'));
                if (value > 0) {
                    return value;
                }
                System.out.println("Число має бути більшим за 0.");
            } catch (NumberFormatException e) {
                System.out.println("Введіть число.");
            }
        }
    }

    private static int readInt(Scanner scanner, String prompt, int min) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min) {
                    return value;
                }
                System.out.println("Число має бути не меншим за " + min + ".");
            } catch (NumberFormatException e) {
                System.out.println("Введіть ціле число.");
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Прилад: ").append(name).append('\n');
        sb.append("  Вага: ").append(weight).append(" кг\n");
        sb.append("  Стан: ").append(state.getTitle()).append('\n');
        sb.append("  Покупні вироби:");
        if (purchasedItems.isEmpty()) {
            sb.append(" немає");
        } else {
            for (PurchasedItem item : purchasedItems) {
                sb.append("\n    - ").append(item);
            }
        }
        return sb.toString();
    }
}

public class Main {

    private static void printState(MeasuringDevice device) {
        System.out.println(device.getName() + ": " + device.getState().getTitle());
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        List<PurchasedItem> voltmeterItems = new ArrayList<>();
        voltmeterItems.add(new PurchasedItem("Резистор 10 кОм", 12, "ТОВ «Радіодеталь»"));
        voltmeterItems.add(new PurchasedItem("РК-індикатор", 1, "ПП «Дисплей-Сервіс»"));
        MeasuringDevice voltmeter = new MeasuringDevice("Вольтметр В7-40", 2.5, voltmeterItems);

        List<PurchasedItem> thermoItems = new ArrayList<>();
        thermoItems.add(new PurchasedItem("Термопара типу K", 1, "ТОВ «Термоконтакт»"));
        MeasuringDevice thermometer = new MeasuringDevice("Термометр ТЦ-1", 0.3, thermoItems);

        MeasuringDevice userDevice = new MeasuringDevice(scanner);

        System.out.println("\n=== Початкові дані ===");
        System.out.println(voltmeter);
        System.out.println(thermometer);
        System.out.println(userDevice);

        System.out.println("\n=== Зміна властивостей ===");
        voltmeter.setName("Вольтметр В7-40М");
        voltmeter.setWeight(2.2);
        System.out.println("Нова назва: " + voltmeter.getName());
        System.out.println("Нова вага: " + voltmeter.getWeight() + " кг");

        try {
            thermometer.setWeight(-1);
        } catch (IllegalArgumentException e) {
            System.out.println("Помилка: " + e.getMessage());
        }

        System.out.println("\n=== Покупні вироби ===");
        thermometer.addPurchasedItem(new PurchasedItem("Батарея CR2032", 2, "ТОВ «Енергоресурс»"));
        System.out.println("Після додавання: " + thermometer.getPurchasedItems());

        PurchasedItem resistor = voltmeter.findPurchasedItem("Резистор 10 кОм");
        if (resistor != null) {
            resistor.setQuantity(15);
            resistor.setSupplier("ТОВ «Електрокомплект»");
            System.out.println("Змінено виріб: " + resistor);
        }

        boolean removed = voltmeter.removePurchasedItem("РК-індикатор");
        System.out.println("РК-індикатор видалено: " + removed);
        System.out.println("Загальна кількість покупних виробів у вольтметрі: "
                + voltmeter.getTotalPurchasedQuantity() + " шт.");

        System.out.println("\n=== Керування станом ===");
        voltmeter.turnOn();
        printState(voltmeter);
        System.out.println("Прилад увімкнено: " + voltmeter.isOn());

        voltmeter.breakDown();
        printState(voltmeter);
        System.out.println("Прилад справний: " + voltmeter.isServiceable());

        if (!voltmeter.turnOn()) {
            System.out.println("Неможливо увімкнути несправний прилад.");
        }

        voltmeter.repair();
        printState(voltmeter);
        voltmeter.turnOn();
        printState(voltmeter);
        voltmeter.turnOff();
        printState(voltmeter);

        userDevice.turnOn();
        printState(userDevice);

        System.out.println("\n=== Підсумкові дані ===");
        System.out.println(voltmeter);
        System.out.println(thermometer);
        System.out.println(userDevice);

        scanner.close();
    }
}