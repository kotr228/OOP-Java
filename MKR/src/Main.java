import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// ===================== 2. Enum =====================
// Перелік типів відпочинку; кожна константа має текстовий опис
enum TravelType {
    BEACH("пляжний відпочинок"),
    SKI("гірськолижний"),
    EXCURSION("екскурсійний"),
    HOT("гарячі тури"),
    ECO("еко-туризм"),
    OTHER("інший");

    private final String description;

    TravelType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

// ===================== 1. Базовий клас Tour =====================
// Тур: назва, тривалість (днів), країна, тип відпочинку, ціна та знижка
class Tour {
    private String name;
    private int size;
    private String country;
    private TravelType type;
    private double price;
    private double discount;

    // ===== Конструктори =====
    public Tour() {
        this("Без назви", 1, "Невідомо", TravelType.OTHER, 0.0);
    }

    public Tour(String name, int size, String country, TravelType type, double price) {
        setName(name);
        setSize(size);
        setCountry(country);
        setType(type);
        setPrice(price);
    }

    public Tour(String name, int size, String country, TravelType type,
                double price, double discount) {
        this(name, size, country, type, price);
        setPrice(price, discount);
    }

    // ===== 4. Перевантаження методів =====
    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Ціна не може бути від'ємною");
        }
        this.price = price;
        this.discount = 0;
    }

    public void setPrice(double price, double discount) {
        if (discount < 0 || discount > 100) {
            throw new IllegalArgumentException("Знижка має бути від 0 до 100%");
        }
        setPrice(price);
        this.discount = discount;
    }

    // Підсумкова ціна з урахуванням знижки, округлена до копійок
    public double getPrice() {
        return Math.round(price * (100 - discount)) / 100.0;
    }

    public int getDuration() {
        return size;
    }

    public double getBasePrice() { return price; }
    public double getDiscount()  { return discount; }
    public String getName()      { return name; }
    public String getCountry()   { return country; }
    public TravelType getType()  { return type; }

    // ===== Сетери з перевіркою коректності даних =====
    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Назва не може бути порожньою");
        }
        this.name = name;
    }

    public void setSize(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Тривалість має бути більше 0");
        }
        this.size = size;
    }

    public void setCountry(String country) {
        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("Країна не може бути порожньою");
        }
        this.country = country;
    }

    public void setType(TravelType type) {
        this.type = (type == null) ? TravelType.OTHER : type;
    }

    // Рядок з ціною (і знижкою, якщо вона є) з заданим відступом
    public String getPriceInfo(String indent) {
        if (discount > 0) {
            return indent + "Ціна: " + price + " грн (знижка " + discount + "%)\n"
                    + indent + "Підсумкова ціна: " + getPrice() + " грн";
        }
        return indent + "Ціна: " + price + " грн";
    }

    public String getInfo() {
        return "Назва: " + name + "\n"
                + "Тип: " + type + " (" + type.getDescription() + ")\n"
                + "Країна: " + country + "\n"
                + "Тривалість: " + size + " дн.\n"
                + getPriceInfo("");
    }

    @Override
    public String toString() {
        return name + " (" + size + " дн.)";
    }
}

// ===================== 3. Успадкування =====================
// Базовий клас для людей: ім'я, телефон, email
class Person {
    private String name;
    private String phone;
    private String email;

    public Person(String name, String phone, String email) {
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public String getName()  { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }

    public void setName(String name)   { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }

    public void displayInfo() {
        System.out.println("Ім'я: " + name);
        System.out.println("Телефон: " + phone);
        System.out.println("Email: " + email);
    }
}

// Клієнт агентства — нащадок Person зі списком заброньованих турів
class Client extends Person {
    private List<Tour> bookedTours = new ArrayList<>();

    public Client(String name, String phone, String email) {
        super(name, phone, email);
    }

    // Повертає false, якщо тур уже заброньовано цим клієнтом
    public boolean bookTour(Tour tour) {
        if (bookedTours.contains(tour)) {
            return false;
        }
        bookedTours.add(tour);
        return true;
    }

    public void cancelTour(Tour tour) {
        bookedTours.remove(tour);
    }

    public List<Tour> getBookedTours() {
        return bookedTours;
    }

    // Загальна вартість усіх бронювань клієнта
    public double getTotalCost() {
        double sum = 0;
        for (Tour t : bookedTours) {
            sum += t.getPrice();
        }
        return sum;
    }

    // Перевизначення: додатково виводить заброньовані тури
    @Override
    public void displayInfo() {
        System.out.println("Клієнт: " + getName());
        System.out.println("Телефон: " + getPhone());
        System.out.println("Email: " + getEmail());
        System.out.println("Заброньовані тури:");
        if (bookedTours.isEmpty()) {
            System.out.println("  (немає бронювань)");
            return;
        }
        for (int i = 0; i < bookedTours.size(); i++) {
            Tour t = bookedTours.get(i);
            System.out.println("  " + (i + 1) + ". " + t);
            System.out.println(t.getPriceInfo("     "));
        }
    }
}

// ===================== 5. Демонстрація роботи (меню) =====================
public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final List<Tour> tours = new ArrayList<>();
    private static final List<Client> clients = new ArrayList<>();

    // Головний цикл: виводимо меню і виконуємо обрану дію
    public static void main(String[] args) {
        loadDemoData();

        while (true) {
            printMenu();
            int choice = readInt("Ваш вибір: ", 0, 13);
            System.out.println();
            try {
                switch (choice) {
                    case 1 -> showTours();
                    case 2 -> addTour();
                    case 3 -> editTour();
                    case 4 -> changePrice();
                    case 5 -> deleteTour();
                    case 6 -> findToursByType();
                    case 7 -> showClients();
                    case 8 -> addClient();
                    case 9 -> editClient();
                    case 10 -> deleteClient();
                    case 11 -> bookTour();
                    case 12 -> cancelBooking();
                    case 13 -> showStatistics();
                    case 0 -> {
                        System.out.println("До побачення!");
                        return;
                    }
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Помилка: " + e.getMessage());
            }
            System.out.println();
        }
    }

    private static void printMenu() {
        System.out.println("========== ТУРИСТИЧНЕ АГЕНТСТВО ==========");
        System.out.println("--- Тури ---");
        System.out.println(" 1. Переглянути всі тури");
        System.out.println(" 2. Додати тур");
        System.out.println(" 3. Редагувати тур");
        System.out.println(" 4. Змінити ціну / застосувати знижку");
        System.out.println(" 5. Видалити тур");
        System.out.println(" 6. Пошук турів за типом відпочинку");
        System.out.println("--- Клієнти ---");
        System.out.println(" 7. Переглянути всіх клієнтів");
        System.out.println(" 8. Додати клієнта");
        System.out.println(" 9. Редагувати клієнта");
        System.out.println("10. Видалити клієнта");
        System.out.println("--- Бронювання ---");
        System.out.println("11. Забронювати тур");
        System.out.println("12. Скасувати бронювання");
        System.out.println("13. Статистика");
        System.out.println(" 0. Вихід");
        System.out.println("==========================================");
    }

    // ---------- Тури ----------
    private static void showTours() {
        System.out.println("--- Інформація про тури ---");
        if (tours.isEmpty()) {
            System.out.println("Список турів порожній.");
            return;
        }
        for (int i = 0; i < tours.size(); i++) {
            System.out.println("[" + (i + 1) + "]");
            System.out.println(tours.get(i).getInfo());
            System.out.println();
        }
    }

    private static void addTour() {
        System.out.println("--- Додавання туру ---");
        String name = readNonEmpty("Назва: ");
        int size = readInt("Тривалість (днів): ", 1, 365);
        String country = readNonEmpty("Країна: ");
        TravelType type = readType();
        double price = readDouble("Ціна (грн): ", 0, 10_000_000);
        double discount = readDouble("Знижка (%) (0 - без знижки): ", 0, 100);

        Tour tour = (discount > 0)
                ? new Tour(name, size, country, type, price, discount)
                : new Tour(name, size, country, type, price);
        tours.add(tour);
        System.out.println("Тур додано: " + tour);
    }

    private static void editTour() {
        Tour tour = chooseTour();
        if (tour == null) return;

        while (true) {
            System.out.println("\nПоточні дані:\n" + tour.getInfo());
            System.out.println("\nЩо змінити?");
            System.out.println("1. Назву");
            System.out.println("2. Тривалість");
            System.out.println("3. Країну");
            System.out.println("4. Тип відпочинку");
            System.out.println("0. Завершити редагування");
            int c = readInt("Ваш вибір: ", 0, 4);
            switch (c) {
                case 1 -> tour.setName(readNonEmpty("Нова назва: "));
                case 2 -> tour.setSize(readInt("Нова тривалість (днів): ", 1, 365));
                case 3 -> tour.setCountry(readNonEmpty("Нова країна: "));
                case 4 -> tour.setType(readType());
                case 0 -> {
                    System.out.println("Зміни збережено.");
                    return;
                }
            }
        }
    }

    private static void changePrice() {
        Tour tour = chooseTour();
        if (tour == null) return;

        System.out.println(tour.getPriceInfo(""));
        System.out.println("1. Встановити ціну без знижки  -> setPrice(price)");
        System.out.println("2. Встановити ціну зі знижкою  -> setPrice(price, discount)");
        int c = readInt("Ваш вибір: ", 1, 2);
        double price = readDouble("Ціна (грн): ", 0, 10_000_000);
        if (c == 1) {
            tour.setPrice(price);
        } else {
            double discount = readDouble("Знижка (%): ", 0, 100);
            tour.setPrice(price, discount);
        }
        System.out.println("Ціну оновлено:\n" + tour.getPriceInfo(""));
    }

    private static void deleteTour() {
        Tour tour = chooseTour();
        if (tour == null) return;

        tours.remove(tour);
        // видаляємо тур і з бронювань усіх клієнтів
        for (Client c : clients) {
            c.cancelTour(tour);
        }
        System.out.println("Тур \"" + tour.getName() + "\" видалено.");
    }

    private static void findToursByType() {
        TravelType type = readType();
        System.out.println("--- Тури типу " + type + " (" + type.getDescription() + ") ---");
        boolean found = false;
        for (Tour t : tours) {
            if (t.getType() == type) {
                System.out.println(t.getInfo());
                System.out.println();
                found = true;
            }
        }
        if (!found) {
            System.out.println("Турів такого типу не знайдено.");
        }
    }

    // ---------- Клієнти ----------
    private static void showClients() {
        System.out.println("--- Інформація про клієнтів ---");
        if (clients.isEmpty()) {
            System.out.println("Список клієнтів порожній.");
            return;
        }
        for (Client c : clients) {
            c.displayInfo();
            System.out.println();
        }
    }

    private static void addClient() {
        System.out.println("--- Додавання клієнта ---");
        String name = readNonEmpty("ПІБ: ");
        String phone = readNonEmpty("Телефон: ");
        String email = readNonEmpty("Email: ");
        clients.add(new Client(name, phone, email));
        System.out.println("Клієнта додано: " + name);
    }

    private static void editClient() {
        Client client = chooseClient();
        if (client == null) return;

        while (true) {
            System.out.println();
            client.displayInfo();
            System.out.println("\nЩо змінити?");
            System.out.println("1. ПІБ");
            System.out.println("2. Телефон");
            System.out.println("3. Email");
            System.out.println("0. Завершити редагування");
            int c = readInt("Ваш вибір: ", 0, 3);
            switch (c) {
                case 1 -> client.setName(readNonEmpty("Нове ПІБ: "));
                case 2 -> client.setPhone(readNonEmpty("Новий телефон: "));
                case 3 -> client.setEmail(readNonEmpty("Новий email: "));
                case 0 -> {
                    System.out.println("Зміни збережено.");
                    return;
                }
            }
        }
    }

    private static void deleteClient() {
        Client client = chooseClient();
        if (client == null) return;
        clients.remove(client);
        System.out.println("Клієнта \"" + client.getName() + "\" видалено.");
    }

    // ---------- Бронювання ----------
    private static void bookTour() {
        Client client = chooseClient();
        if (client == null) return;
        Tour tour = chooseTour();
        if (tour == null) return;

        if (client.bookTour(tour)) {
            System.out.println("Тур \"" + tour.getName() + "\" заброньовано для "
                    + client.getName() + ".");
        } else {
            System.out.println("Цей тур уже заброньовано цим клієнтом.");
        }
    }

    private static void cancelBooking() {
        Client client = chooseClient();
        if (client == null) return;

        List<Tour> booked = client.getBookedTours();
        if (booked.isEmpty()) {
            System.out.println("У клієнта немає бронювань.");
            return;
        }
        for (int i = 0; i < booked.size(); i++) {
            System.out.println((i + 1) + ". " + booked.get(i));
        }
        int n = readInt("Номер бронювання для скасування: ", 1, booked.size());
        Tour tour = booked.get(n - 1);
        client.cancelTour(tour);
        System.out.println("Бронювання \"" + tour.getName() + "\" скасовано.");
    }

    private static void showStatistics() {
        int totalBookings = 0;
        double totalSum = 0;
        for (Client c : clients) {
            totalBookings += c.getBookedTours().size();
            totalSum += c.getTotalCost();
        }
        System.out.println("--- Статистика ---");
        System.out.println("Турів в агентстві: " + tours.size());
        System.out.println("Клієнтів: " + clients.size());
        System.out.println("Загальна кількість заброньованих турів: " + totalBookings);
        System.out.println("Загальна сума бронювань: " + Math.round(totalSum * 100) / 100.0 + " грн");
    }

    // ---------- Вибір зі списків ----------
    private static Tour chooseTour() {
        if (tours.isEmpty()) {
            System.out.println("Список турів порожній.");
            return null;
        }
        System.out.println("Оберіть тур:");
        for (int i = 0; i < tours.size(); i++) {
            System.out.println((i + 1) + ". " + tours.get(i));
        }
        int n = readInt("Номер туру: ", 1, tours.size());
        return tours.get(n - 1);
    }

    private static Client chooseClient() {
        if (clients.isEmpty()) {
            System.out.println("Список клієнтів порожній.");
            return null;
        }
        System.out.println("Оберіть клієнта:");
        for (int i = 0; i < clients.size(); i++) {
            System.out.println((i + 1) + ". " + clients.get(i).getName());
        }
        int n = readInt("Номер клієнта: ", 1, clients.size());
        return clients.get(n - 1);
    }

    private static TravelType readType() {
        TravelType[] types = TravelType.values();
        System.out.println("Тип відпочинку:");
        for (int i = 0; i < types.length; i++) {
            System.out.println((i + 1) + ". " + types[i] + " - " + types[i].getDescription());
        }
        int n = readInt("Номер типу: ", 1, types.length);
        return types[n - 1];
    }

    // ---------- Безпечне зчитування ----------
    private static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) {
            }
            System.out.println("Введіть ціле число від " + min + " до " + max + ".");
        }
    }

    private static double readDouble(String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim().replace(',', '.');
            try {
                double value = Double.parseDouble(line);
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) {
            }
            System.out.println("Введіть число від " + min + " до " + max + ".");
        }
    }

    private static String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            if (!line.isEmpty()) return line;
            System.out.println("Поле не може бути порожнім.");
        }
    }

    // ---------- Початкові дані ----------
    private static void loadDemoData() {
        Tour turkey = new Tour("Відпочинок у Туреччині", 7, "Туреччина",
                TravelType.BEACH, 850.0);
        Tour bukovel = new Tour("Гірськолижний тур у Буковель", 5, "Україна",
                TravelType.SKI, 620.0, 15.0);
        Tour prague = new Tour("Екскурсійний тур \"Прага – Відень\"", 6, "Чехія",
                TravelType.EXCURSION, 780.0, 10.0);
        tours.add(turkey);
        tours.add(bukovel);
        tours.add(prague);

        Client olena = new Client("Олена Коваленко", "+380501234567", "olena@mail.com");
        Client dmytro = new Client("Дмитро Сидоренко", "+380937654321", "dmytro@mail.com");
        olena.bookTour(turkey);
        olena.bookTour(prague);
        dmytro.bookTour(bukovel);
        clients.add(olena);
        clients.add(dmytro);
    }
}