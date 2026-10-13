enum DroneType {
    CAMERA,
    RACING,
    AGRICULTURAL
}

class Device {
    String model;
    String manufacturer;

    Device(String model, String manufacturer) {
        this.model = model;
        this.manufacturer = manufacturer;
    }

    void showInfo() {
        System.out.println("Пристрій: " + manufacturer + " " + model);
    }
}

interface Flyable {
    int MAX_ALTITUDE = 120;  // м — фактично public static final

    void fly(int altitude);

    void land();

    default void showFlightRules() {
        System.out.println("Правила: висота не більше " + MAX_ALTITUDE
                + " м, тримайте дрон у зоні прямої видимості.");
    }

    static boolean isAltitudeAllowed(int altitude) {
        return altitude > 0 && altitude <= MAX_ALTITUDE;
    }
}

interface PhotoCapable {
    void takePhoto();

    default String getPhotoFormat() {
        return "JPEG";
    }
}

class Drone extends Device implements Flyable, PhotoCapable {
    DroneType type;
    int cameraResolution;   // Мп
    int batteryLevel = 100; // %
    int currentAltitude = 0;
    int photoCount = 0;

    static final int MIN_BATTERY = 20;

    Drone(String model, String manufacturer, DroneType type, int cameraResolution) {
        super(model, manufacturer);
        this.type = type;
        this.cameraResolution = cameraResolution;
    }

    @Override
    void showInfo() {
        super.showInfo();
        System.out.println("  Тип: " + type + ", камера: " + cameraResolution
                + " Мп, заряд: " + batteryLevel + "%");
    }

    @Override
    public void fly(int altitude) {
        if (!Flyable.isAltitudeAllowed(altitude)) {
            System.out.println(model + ": висота " + altitude
                    + " м недопустима (дозволено 1–" + MAX_ALTITUDE + " м)");
            return;
        }
        if (batteryLevel < MIN_BATTERY) {
            System.out.println(model + ": низький заряд, зліт неможливий");
            return;
        }
        currentAltitude = altitude;
        batteryLevel -= altitude / 10;
        System.out.println(model + " піднявся на висоту " + altitude
                + " м. Заряд: " + batteryLevel + "%");
    }

    @Override
    public void land() {
        if (currentAltitude == 0) {
            System.out.println(model + " уже на землі");
        } else {
            currentAltitude = 0;
            System.out.println(model + " приземлився");
        }
    }

    @Override
    public void takePhoto() {
        photoCount++;
        System.out.println(model + ": фото №" + photoCount + " з висоти "
                + currentAltitude + " м (" + cameraResolution + " Мп, "
                + getPhotoFormat() + ")");
    }

    @Override
    public String getPhotoFormat() {
        // Перевизначення default-методу: зйомочні дрони пишуть RAW
        if (type == DroneType.CAMERA) {
            return "RAW";
        }
        return PhotoCapable.super.getPhotoFormat();
    }
}

class Smartphone extends Device implements PhotoCapable {
    int cameraResolution;

    Smartphone(String model, String manufacturer, int cameraResolution) {
        super(model, manufacturer);
        this.cameraResolution = cameraResolution;
    }

    @Override
    public void takePhoto() {
        System.out.println(model + ": фото на смартфон (" + cameraResolution
                + " Мп, " + getPhotoFormat() + ")");
    }
}

public class Main {
    public static void main(String[] args) {
        Drone mavic = new Drone("Mavic 3", "DJI", DroneType.CAMERA, 20);
        Drone racer = new Drone("Avata", "DJI", DroneType.RACING, 12);
        Smartphone phone = new Smartphone("Galaxy S24", "Samsung", 50);

        System.out.println("=== Пристрої ===");
        mavic.showInfo();
        racer.showInfo();
        phone.showInfo();

        System.out.println("\n=== Константа та static-метод інтерфейсу ===");
        System.out.println("Максимальна висота: " + Flyable.MAX_ALTITUDE + " м");
        System.out.println("100 м дозволено: " + Flyable.isAltitudeAllowed(100));
        System.out.println("150 м дозволено: " + Flyable.isAltitudeAllowed(150));

        System.out.println("\n=== Посилання інтерфейсного типу ===");
        Flyable flyer = mavic;  // тип змінної — Flyable, об'єкт — Drone
        flyer.showFlightRules(); // default-метод
        flyer.fly(80);
        racer.fly(50);

        System.out.println("\n=== Масив інтерфейсного типу PhotoCapable ===");
        PhotoCapable[] cameras = {mavic, racer, phone};
        for (PhotoCapable camera : cameras) {
            camera.takePhoto();
        }

        System.out.println("\n=== Завершення польотів ===");
        mavic.fly(150);   // перевищення висоти
        mavic.land();
        racer.land();
        racer.land();     // повторне приземлення
    }
}