/** Абстрактний клас: вимірювальний прилад. */
abstract class MeasuringDevice {
    public static final String STATE_OFF = "вимкнено";
    public static final String STATE_ON = "увімкнено";
    public static final String STATE_FAULTY = "несправний";

    // Статичні дані: кількість створених приладів усіх типів
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

    // Статичний метод
    public static int getDeviceCount() {
        return deviceCount;
    }

    // ---------- Віртуальні (абстрактні) функції ----------

    /** Повертає текстовий опис приладу. */
    public abstract String getInfo();

    /** Обчислює вартість доставки приладу замовнику, грн. */
    public abstract double calculateDeliveryCost();

    // ---------- Отримання та зміна властивостей ----------

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Найменування не може бути порожнім");
        }
        this.name = name.trim();
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        if (cost <= 0) {
            throw new IllegalArgumentException("Вартість має бути більшою за 0");
        }
        this.cost = cost;
    }

    // ---------- Керування станом ----------

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

    public boolean isOn() {
        return state.equals(STATE_ON);
    }
}

/** Клас1: прилад з джерелом живлення. */
class ElectricDevice extends MeasuringDevice {
    // Статичні дані: частка вартості, яка йде на страхування при доставці
    private static final double DELIVERY_RATE = 0.05;

    private double voltage;    // В
    private double current;    // А
    private double frequency;  // Гц (0 — постійний струм)

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

    public double getVoltage() {
        return voltage;
    }

    public void setVoltage(double voltage) {
        if (voltage <= 0) {
            throw new IllegalArgumentException("Напруга має бути більшою за 0");
        }
        this.voltage = voltage;
    }

    public double getCurrent() {
        return current;
    }

    public void setCurrent(double current) {
        if (current <= 0) {
            throw new IllegalArgumentException("Струм має бути більшим за 0");
        }
        this.current = current;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        if (frequency < 0) {
            throw new IllegalArgumentException("Частота не може бути від'ємною");
        }
        this.frequency = frequency;
    }

    /** Споживана потужність, Вт. */
    public double getPower() {
        return voltage * current;
    }

    public boolean isDirectCurrent() {
        return frequency == 0;
    }

    // ---------- Реалізація віртуальних функцій ----------

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
    // Статичні дані: тариф перевезення за 1 м³ об'єму, грн
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

    /** Об'єм приладу, м³ (розміри задані в мм). */
    public double getVolume() {
        return length * height * width / 1_000_000_000.0;
    }

    // ---------- Перевизначення віртуальних функцій ----------

    @Override
    public String getInfo() {
        return super.getInfo() + String.format(
                "%n  Габарити (Д×В×Ш): %.0f×%.0f×%.0f мм, об'єм %.4f м³",
                length, height, width, getVolume());
    }

    @Override
    public double calculateDeliveryCost() {
        // Страхування з Класу1 + плата за об'єм
        return super.calculateDeliveryCost() + getVolume() * ratePerCubicMeter;
    }
}

public class Main {

    private static void printDeliveryCosts(MeasuringDevice[] devices) {
        for (MeasuringDevice device : devices) {
            System.out.printf("%s — доставка: %.2f грн%n",
                    device.getName(), device.calculateDeliveryCost());
        }
    }

    public static void main(String[] args) {
        // new MeasuringDevice(...) — помилка компіляції: клас абстрактний

        ElectricDevice multimeter = new ElectricDevice(
                "Мультиметр DT-830", 850, 9, 0.002, 0);
        SizedElectricDevice oscilloscope = new SizedElectricDevice(
                "Осцилограф С1-117", 15000, 220, 0.5, 50, 400, 200, 300);
        SizedElectricDevice generator = new SizedElectricDevice(
                "Генератор Г4-102", 9200, 220, 0.3, 50, 350, 150, 280);

        System.out.println("Створено приладів: " + MeasuringDevice.getDeviceCount());
        System.out.println("Ставка страхування: "
                + ElectricDevice.getDeliveryRate() * 100 + "%");
        System.out.println("Тариф за об'єм: "
                + SizedElectricDevice.getRatePerCubicMeter() + " грн/м³");

        // Масив посилань на абстрактний клас — поліморфізм
        MeasuringDevice[] devices = {multimeter, oscilloscope, generator};

        System.out.println("\n=== Інформація про прилади (виклик getInfo) ===");
        for (MeasuringDevice device : devices) {
            System.out.println(device.getInfo());   // метод обирається за типом об'єкта
        }

        System.out.println("\n=== Вартість доставки ===");
        printDeliveryCosts(devices);

        System.out.println("\n=== Зміна статичного тарифу на 8000 грн/м³ ===");
        SizedElectricDevice.setRatePerCubicMeter(8000);
        printDeliveryCosts(devices);

        System.out.println("\n=== Зміна властивостей ===");
        multimeter.setCost(920);
        oscilloscope.setVoltage(230);
        generator.setWidth(250);
        System.out.println(multimeter.getName() + ": нова вартість " + multimeter.getCost() + " грн");
        System.out.println(oscilloscope.getName() + ": нова напруга " + oscilloscope.getVoltage() + " В");
        System.out.printf("%s: новий об'єм %.4f м³%n", generator.getName(), generator.getVolume());

        try {
            generator.setHeight(-10);
        } catch (IllegalArgumentException e) {
            System.out.println("Помилка: " + e.getMessage());
        }

        System.out.println("\n=== Керування станом ===");
        oscilloscope.turnOn();
        System.out.println(oscilloscope.getName() + ": " + oscilloscope.getState());
        oscilloscope.breakDown();
        System.out.println(oscilloscope.getName() + ": " + oscilloscope.getState());
        if (!oscilloscope.turnOn()) {
            System.out.println("Неможливо увімкнути несправний прилад.");
        }
        oscilloscope.repair();
        oscilloscope.turnOn();
        System.out.println(oscilloscope.getName() + ": " + oscilloscope.getState()
                + ", увімкнено: " + oscilloscope.isOn());
        oscilloscope.turnOff();
        System.out.println(oscilloscope.getName() + ": " + oscilloscope.getState());

        System.out.println("\n=== Підсумкові дані ===");
        for (MeasuringDevice device : devices) {
            System.out.println(device.getInfo());
        }
        System.out.println("Всього приладів: " + MeasuringDevice.getDeviceCount());
    }
}