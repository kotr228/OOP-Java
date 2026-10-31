import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

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

/** Вимірювальний прилад (з ЛР2) + equals/hashCode для роботи з колекцією. */
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
        purchasedItems.add(item);
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

    /** Прилади однакові, якщо збігаються найменування (без урахування регістру) і вага. */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MeasuringDevice)) {
            return false;
        }
        MeasuringDevice other = (MeasuringDevice) o;
        return name.equalsIgnoreCase(other.name)
                && Double.compare(weight, other.weight) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase(), weight);
    }
}

/** Колекція приладів на основі LinkedList. */
class DeviceList {
    private final LinkedList<MeasuringDevice> devices = new LinkedList<>();

    /** Допоміжне додавання в кінець (для заповнення списку). */
    public void add(MeasuringDevice device) {
        devices.add(device);
    }

    /** Функція 3: додати елемент у позицію індексу. */
    public void insertAt(int index, MeasuringDevice device) {
        if (index < 0 || index > devices.size()) {
            throw new IndexOutOfBoundsException(
                    "індекс " + index + " поза межами 0–" + devices.size());
        }
        devices.add(index, device);
    }

    /** Функція 7: видалити останній елемент. */
    public MeasuringDevice removeLast() {
        return devices.removeLast();  // NoSuchElementException, якщо список порожній
    }

    /** Функція 18: видалити певний елемент (перше входження). */
    public boolean remove(MeasuringDevice device) {
        return devices.remove(device);
    }

    /** Функція 10: кількість входжень елемента у список. */
    public int countOccurrences(MeasuringDevice device) {
        return Collections.frequency(devices, device);
    }

    /** Функція 16: новий список приладів із заданим станом. */
    public DeviceList filterByState(String state) {
        DeviceList result = new DeviceList();
        for (MeasuringDevice device : devices) {
            if (device.getState().equals(state)) {
                result.add(device);
            }
        }
        return result;
    }

    /** Функція 14: сортування за зростанням ваги. */
    public void sortByWeightAscending() {
        devices.sort(Comparator.comparingDouble(MeasuringDevice::getWeight));
    }

    public int size() {
        return devices.size();
    }

    public void print(String title) {
        System.out.println(title + " (" + devices.size() + " шт.):");
        if (devices.isEmpty()) {
            System.out.println("  список порожній");
            return;
        }
        int index = 0;
        for (MeasuringDevice d : devices) {
            System.out.printf("  [%d] %s — %.1f кг, стан: %s, покупних виробів: %d%n",
                    index++, d.getName(), d.getWeight(), d.getState(),
                    d.getPurchasedItems().size());
        }
    }
}

public class Main {
    public static void main(String[] args) {
        // Створення приладів
        List<PurchasedItem> voltItems = new ArrayList<>();
        voltItems.add(new PurchasedItem("Резистор 10 кОм", 12, "ТОВ «Радіодеталь»"));
        voltItems.add(new PurchasedItem("РК-індикатор", 1, "ПП «Дисплей-Сервіс»"));
        MeasuringDevice voltmeter = new MeasuringDevice("Вольтметр В7-40", 2.5, voltItems);

        MeasuringDevice thermometer = new MeasuringDevice("Термометр ТЦ-1", 0.3,
                List.of(new PurchasedItem("Термопара типу K", 1, "ТОВ «Термоконтакт»")));
        MeasuringDevice oscilloscope = new MeasuringDevice("Осцилограф С1-117", 9.5,
                List.of(new PurchasedItem("Електронно-променева трубка", 1, "ТОВ «Електрон»")));
        MeasuringDevice ammeter = new MeasuringDevice("Амперметр Е42", 0.8,
                List.of(new PurchasedItem("Шунт 75 мВ", 1, "ТОВ «Електроприлад»")));

        DeviceList list = new DeviceList();
        list.add(voltmeter);
        list.add(thermometer);
        list.add(oscilloscope);
        list.print("=== Початковий список");

        // Функція 3
        System.out.println("\n=== Функція 3: додати елемент у позицію індексу ===");
        list.insertAt(1, ammeter);
        System.out.println(ammeter.getName() + " вставлено в позицію 1.");
        list.print("Список");
        try {
            list.insertAt(10, ammeter);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Помилка: " + e.getMessage());
        }

        // Функція 10
        System.out.println("\n=== Функція 10: кількість входжень елемента ===");
        list.add(thermometer);  // той самий прилад ще раз
        list.print("Список після повторного додавання термометра");
        System.out.println(thermometer.getName() + " входить у список "
                + list.countOccurrences(thermometer) + " раз(и)");
        MeasuringDevice sameAsThermometer = new MeasuringDevice("термометр тц-1", 0.3, null);
        System.out.println("Новий об'єкт з тими ж даними входить у список "
                + list.countOccurrences(sameAsThermometer) + " раз(и) (порівняння через equals)");

        // Функція 16
        System.out.println("\n=== Функція 16: новий список приладів у стані «увімкнено» ===");
        voltmeter.turnOn();
        ammeter.turnOn();
        oscilloscope.breakDown();
        DeviceList switchedOn = list.filterByState(MeasuringDevice.STATE_ON);
        switchedOn.print("Увімкнені прилади");

        // Функція 14
        System.out.println("\n=== Функція 14: сортування за зростанням ваги ===");
        list.sortByWeightAscending();
        list.print("Відсортований список");

        // Функція 18
        System.out.println("\n=== Функція 18: видалити певний елемент ===");
        boolean removed = list.remove(thermometer);
        System.out.println(thermometer.getName() + " видалено: " + removed);
        System.out.println("Тепер входить у список "
                + list.countOccurrences(thermometer) + " раз(и)");
        list.print("Список");

        // Функція 7
        System.out.println("\n=== Функція 7: видалити останній елемент ===");
        MeasuringDevice last = list.removeLast();
        System.out.println("Видалено останній: " + last.getName());
        list.print("Список");

        DeviceList emptyList = new DeviceList();
        try {
            emptyList.removeLast();
        } catch (NoSuchElementException e) {
            System.out.println("Помилка: неможливо видалити елемент з порожнього списку");
        }
    }
}