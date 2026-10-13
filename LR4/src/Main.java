/** Власний виняток: спроба виконати дію з несправним приладом. */
class DeviceFaultException extends Exception {
    public DeviceFaultException(String message) {
        super(message);
    }
}

/** Інтерфейс 1: будь-який прилад заводу. */
interface Device {
    // Константи (неявно public static final)
    String STATE_OFF = "вимкнено";
    String STATE_ON = "увімкнено";
    String STATE_FAULTY = "несправний";

    String getName();

    double getCost();

    String getState();

    void turnOn() throws DeviceFaultException;

    void turnOff();

    String getInfo();

    double calculateDeliveryCost();

    /** Default-метод: реалізація через інший метод інтерфейсу. */
    default boolean isOn() {
        return STATE_ON.equals(getState());
    }
}

/** Інтерфейс 2: прилад з джерелом живлення. Розширює Device. */
interface PoweredDevice extends Device {
    double MAX_VOLTAGE = 1000.0;       // В
    double STANDARD_FREQUENCY = 50.0;  // Гц

    double getVoltage();

    double getCurrent();

    double getFrequency();

    double getPower();

    default boolean isDirectCurrent() {
        return getFrequency() == 0;
    }
}

/** Абстрактний клас: вимірювальний прилад. */
abstract class MeasuringDevice implements Device {
    private static int deviceCount = 0;

    private String name;
    private double cost;
    private String state;

    protected MeasuringDevice(String name, double cost) {
        setName(name);
        setCost(cost);
        state = STATE_OFF;
        deviceCount++;
    }

    public static int getDeviceCount() {
        return deviceCount;
    }

    // getInfo() і calculateDeliveryCost() тут не оголошуються:
    // вони успадковані з інтерфейсу Device як абстрактні.

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Найменування не може бути порожнім");
        }
        this.name = name.trim();
    }

    @Override
    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        if (cost <= 0) {
            throw new IllegalArgumentException("Вартість має бути більшою за 0");
        }
        this.cost = cost;
    }

    @Override
    public String getState() {
        return state;
    }

    @Override
    public void turnOn() throws DeviceFaultException {
        if (state.equals(STATE_FAULTY)) {
            throw new DeviceFaultException(
                    "Прилад «" + name + "» несправний, увімкнення неможливе");
        }
        state = STATE_ON;
    }

    @Override
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
}

/** Клас1: прилад з джерелом живлення. */
class ElectricDevice extends MeasuringDevice implements PoweredDevice {
    private static final double DELIVERY_RATE = 0.05;

    private double voltage;
    private double current;
    private double frequency;

    public ElectricDevice(String name, double cost,
                          double voltage, double current, double frequency) {
        super(name, cost);
        setVoltage(voltage);
        setCurrent(current);
        setFrequency(frequency);
    }

    public static double getDeliveryRate() {
        return DELIVERY_RATE;
    }

    @Override
    public double getVoltage() {
        return voltage;
    }

    public void setVoltage(double voltage) {
        if (voltage <= 0 || voltage > MAX_VOLTAGE) {
            throw new IllegalArgumentException(
                    "Напруга має бути в межах (0; " + MAX_VOLTAGE + "] В");
        }
        this.voltage = voltage;
    }

    @Override
    public double getCurrent() {
        return current;
    }

    public void setCurrent(double current) {
        if (current <= 0) {
            throw new IllegalArgumentException("Струм має бути більшим за 0");
        }
        this.current = current;
    }

    @Override
    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        if (frequency < 0) {
            throw new IllegalArgumentException("Частота не може бути від'ємною");
        }
        this.frequency = frequency;
    }

    @Override
    public double getPower() {
        return voltage * current;
    }

    @Override
    public String getInfo() {
        String currentType = isDirectCurrent()
                ? "постійний струм"
                : String.format("змінний струм, %.0f Гц", frequency);
        return String.format(
                "Прилад: %s%n  Вартість: %.2f грн%n  Стан: %s%n"
                        + "  Живлення: %.1f В, %.3f А, %s (потужність %.3f Вт)",
                getName(), getCost(), getState(), voltage, current, currentType, getPower());
    }

    @Override
    public double calculateDeliveryCost() {
        return getCost() * DELIVERY_RATE;
    }
}

/** Клас2: прилад з джерелом живлення та габаритами. */
class SizedElectricDevice extends ElectricDevice {
    private static double ratePerCubicMeter = 5000.0;

    private double length;  // мм
    private double height;  // мм
    private double width;   // мм

    public SizedElectricDevice(String name, double cost,
                               double voltage, double current, double frequency,
                               double length, double height, double width) {
        super(name, cost, voltage, current, frequency);
        setLength(length);
        setHeight(height);
        setWidth(width);
    }

    public static double getRatePerCubicMeter() {
        return ratePerCubicMeter;
    }

    public static void setRatePerCubicMeter(double rate) {
        if (rate < 0) {
            throw new IllegalArgumentException("Тариф не може бути від'ємним");
        }
        ratePerCubicMeter = rate;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        checkSize(length);
        this.length = length;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        checkSize(height);
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        checkSize(width);
        this.width = width;
    }

    private static void checkSize(double value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Габаритний розмір має бути більшим за 0");
        }
    }

    public double getVolume() {
        return length * height * width / 1_000_000_000.0;
    }

    @Override
    public String getInfo() {
        return super.getInfo() + String.format(
                "%n  Габарити (Д×В×Ш): %.0f×%.0f×%.0f мм, об'єм %.4f м³",
                length, height, width, getVolume());
    }

    @Override
    public double calculateDeliveryCost() {
        return super.calculateDeliveryCost() + getVolume() * ratePerCubicMeter;
    }
}

public class Main {

    /** Працює з будь-яким приладом лише через інтерфейс Device. */
    private static void tryTurnOn(Device device) {
        try {
            device.turnOn();
            System.out.println("Прилад «" + device.getName() + "» увімкнено.");
        } catch (DeviceFaultException e) {
            System.out.println("Помилка: " + e.getMessage());
        } finally {
            System.out.println("  Поточний стан: " + device.getState()
                    + ", увімкнено: " + device.isOn());
        }
    }

    public static void main(String[] args) {
        ElectricDevice multimeter = new ElectricDevice(
                "Мультиметр DT-830", 850, 9, 0.002, 0);
        SizedElectricDevice oscilloscope = new SizedElectricDevice(
                "Осцилограф С1-117", 15000, 220, 0.5, PoweredDevice.STANDARD_FREQUENCY,
                400, 200, 300);
        SizedElectricDevice generator = new SizedElectricDevice(
                "Генератор Г4-102", 9200, 220, 0.3, PoweredDevice.STANDARD_FREQUENCY,
                350, 150, 280);

        System.out.println("Створено приладів: " + MeasuringDevice.getDeviceCount());
        System.out.println("Константи інтерфейсів: стани «" + Device.STATE_OFF + "», «"
                + Device.STATE_ON + "», «" + Device.STATE_FAULTY + "»; "
                + "макс. напруга " + PoweredDevice.MAX_VOLTAGE + " В");

        // 1. Робота через посилання на інтерфейс Device
        Device[] devices = {multimeter, oscilloscope, generator};

        System.out.println("\n=== Інформація про прилади (через Device) ===");
        for (Device device : devices) {
            System.out.println(device.getInfo());
            System.out.printf("  Доставка: %.2f грн%n", device.calculateDeliveryCost());
        }

        // 2. Робота через посилання на інтерфейс PoweredDevice
        System.out.println("\n=== Параметри живлення (через PoweredDevice) ===");
        for (Device device : devices) {
            if (device instanceof PoweredDevice) {
                PoweredDevice powered = (PoweredDevice) device;
                System.out.printf("%s: %.3f Вт, %s%n",
                        powered.getName(), powered.getPower(),
                        powered.isDirectCurrent() ? "постійний струм" : "змінний струм");
            }
        }

        // 3. Обробка винятків при зміні даних
        System.out.println("\n=== Перевірка некоректних даних ===");
        try {
            multimeter.setVoltage(1500);
        } catch (IllegalArgumentException e) {
            System.out.println("Помилка: " + e.getMessage());
        }
        try {
            generator.setWidth(0);
        } catch (IllegalArgumentException e) {
            System.out.println("Помилка: " + e.getMessage());
        }

        // 4. Керування станом і власний виняток
        System.out.println("\n=== Керування станом ===");
        tryTurnOn(oscilloscope);
        oscilloscope.breakDown();
        tryTurnOn(oscilloscope);          // кине DeviceFaultException
        oscilloscope.repair();
        tryTurnOn(oscilloscope);
        oscilloscope.turnOff();
        System.out.println("Після вимкнення: " + oscilloscope.getState());

        // 5. Статичні дані впливають на всі об'єкти Класу2
        System.out.println("\n=== Тариф за об'єм змінено на 8000 грн/м³ ===");
        SizedElectricDevice.setRatePerCubicMeter(8000);
        for (Device device : devices) {
            System.out.printf("%s — доставка: %.2f грн%n",
                    device.getName(), device.calculateDeliveryCost());
        }
    }
}