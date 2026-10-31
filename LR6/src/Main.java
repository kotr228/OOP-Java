import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Власний виняток: некоректні дані товару. */
class ProductException extends Exception {
    public ProductException(String message) {
        super(message);
    }
}

/** Власний виняток: товар уже є в множині, але з іншим гарантійним строком. */
class DuplicateProductException extends ProductException {
    public DuplicateProductException(String message) {
        super(message);
    }
}

/** Товар. */
class Product {
    public static final int MIN_NAME_LENGTH = 3;
    public static final int MIN_MANUFACTURER_LENGTH = 5;
    public static final int MIN_WARRANTY_MONTHS = 1;

    private String name;
    private String manufacturer;
    private int quantity;
    private double wholesalePrice;
    private double retailPrice;
    private int warrantyMonths;

    public Product(String name, String manufacturer, int quantity,
                   double wholesalePrice, double retailPrice, int warrantyMonths)
            throws ProductException {
        setName(name);
        setManufacturer(manufacturer);
        setQuantity(quantity);
        setPrices(wholesalePrice, retailPrice);
        setWarrantyMonths(warrantyMonths);
    }

    /** Конструктор копіювання (дані вже перевірені). */
    public Product(Product other) {
        this.name = other.name;
        this.manufacturer = other.manufacturer;
        this.quantity = other.quantity;
        this.wholesalePrice = other.wholesalePrice;
        this.retailPrice = other.retailPrice;
        this.warrantyMonths = other.warrantyMonths;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws ProductException {
        if (name == null || name.trim().length() < MIN_NAME_LENGTH) {
            throw new ProductException("найменування товару «" + name
                    + "» містить менше " + MIN_NAME_LENGTH + " символів");
        }
        this.name = name.trim();
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) throws ProductException {
        if (manufacturer == null || manufacturer.trim().length() < MIN_MANUFACTURER_LENGTH) {
            throw new ProductException("найменування виробника «" + manufacturer
                    + "» містить менше " + MIN_MANUFACTURER_LENGTH + " символів");
        }
        this.manufacturer = manufacturer.trim();
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) throws ProductException {
        if (quantity < 0) {
            throw new ProductException("кількість товару не може бути від'ємною ("
                    + quantity + ")");
        }
        this.quantity = quantity;
    }

    public double getWholesalePrice() {
        return wholesalePrice;
    }

    public double getRetailPrice() {
        return retailPrice;
    }

    /** Встановлює обидві ціни з перевіркою їх співвідношення. */
    public void setPrices(double wholesalePrice, double retailPrice) throws ProductException {
        if (wholesalePrice <= 0) {
            throw new ProductException("оптова ціна має бути більшою за нуль ("
                    + wholesalePrice + ")");
        }
        if (retailPrice <= 0) {
            throw new ProductException("роздрібна ціна має бути більшою за нуль ("
                    + retailPrice + ")");
        }
        if (wholesalePrice > retailPrice) {
            throw new ProductException("оптова ціна (" + wholesalePrice
                    + ") вища за роздрібну (" + retailPrice + ")");
        }
        this.wholesalePrice = wholesalePrice;
        this.retailPrice = retailPrice;
    }

    public void setWholesalePrice(double wholesalePrice) throws ProductException {
        setPrices(wholesalePrice, retailPrice);
    }

    public void setRetailPrice(double retailPrice) throws ProductException {
        setPrices(wholesalePrice, retailPrice);
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    public void setWarrantyMonths(int warrantyMonths) throws ProductException {
        if (warrantyMonths < MIN_WARRANTY_MONTHS) {
            throw new ProductException("гарантійний строк менше "
                    + MIN_WARRANTY_MONTHS + " місяця (" + warrantyMonths + ")");
        }
        this.warrantyMonths = warrantyMonths;
    }

    /** Чи це той самий елемент множини (найменування + виробник). */
    public boolean sameKey(String name, String manufacturer) {
        return this.name.equalsIgnoreCase(name)
                && this.manufacturer.equalsIgnoreCase(manufacturer);
    }
}

/** Інтерфейс операцій над множиною товарів. */
interface ProductOperations {
    String getTitle();

    /** Метод 1: додавання товару. */
    void add(Product product) throws ProductException;

    /** Метод 2: виведення множини на екран. */
    void print();

    /** Метод 18: видалення товару за найменуванням і виробником. */
    boolean remove(String name, String manufacturer);

    /** Метод 3: об'єднання множин. */
    ProductOperations union(ProductOperations other) throws ProductException;

    Product find(String name, String manufacturer);

    List<Product> getProducts();
}

/** Клас «Множина товарів». */
class ProductSet implements ProductOperations {
    private final String title;
    private final List<Product> products = new ArrayList<>();

    public ProductSet(String title) {
        this.title = title;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public List<Product> getProducts() {
        return Collections.unmodifiableList(products);
    }

    @Override
    public Product find(String name, String manufacturer) {
        for (Product p : products) {
            if (p.sameKey(name, manufacturer)) {
                return p;
            }
        }
        return null;
    }

    @Override
    public void add(Product product) throws ProductException {
        Product existing = find(product.getName(), product.getManufacturer());
        if (existing == null) {
            products.add(product);
            return;
        }
        if (existing.getWarrantyMonths() != product.getWarrantyMonths()) {
            throw new DuplicateProductException("товар «" + product.getName()
                    + "» виробника «" + product.getManufacturer()
                    + "» вже є з гарантійним строком " + existing.getWarrantyMonths()
                    + " міс., спроба додати з новим строком "
                    + product.getWarrantyMonths() + " міс.");
        }
        // Той самий товар з тим самим строком — збільшуємо кількість
        existing.setQuantity(existing.getQuantity() + product.getQuantity());
    }

    @Override
    public boolean remove(String name, String manufacturer) {
        return products.removeIf(p -> p.sameKey(name, manufacturer));
    }

    @Override
    public ProductOperations union(ProductOperations other) throws ProductException {
        ProductSet result = new ProductSet(title + " ∪ " + other.getTitle());

        for (Product p : products) {
            result.products.add(new Product(p));
        }

        for (Product q : other.getProducts()) {
            Product existing = result.find(q.getName(), q.getManufacturer());
            if (existing == null) {
                result.products.add(new Product(q));
            } else {
                existing.setQuantity(existing.getQuantity() + q.getQuantity());
                double wholesale = Math.min(existing.getWholesalePrice(), q.getWholesalePrice());
                double retail = (existing.getRetailPrice() + q.getRetailPrice()) / 2;
                existing.setPrices(wholesale, retail);
            }
        }
        return result;
    }

    @Override
    public void print() {
        System.out.println("--- " + title + " (позицій: " + products.size() + ") ---");
        if (products.isEmpty()) {
            System.out.println("  множина порожня");
            return;
        }
        System.out.printf("%-3s %-12s %-10s %7s %10s %10s %9s%n",
                "№", "Товар", "Виробник", "К-сть", "Опт, грн", "Розд, грн", "Гар, міс");
        int n = 1;
        for (Product p : products) {
            System.out.printf("%-3d %-12s %-10s %7d %10.2f %10.2f %9d%n",
                    n++, p.getName(), p.getManufacturer(), p.getQuantity(),
                    p.getWholesalePrice(), p.getRetailPrice(), p.getWarrantyMonths());
        }
    }
}

public class Main {

    /** Створює товар і додає його в множину з обробкою винятків. */
    static void tryAdd(ProductOperations set, String name, String manufacturer,
                       int quantity, double wholesale, double retail, int warranty) {
        try {
            set.add(new Product(name, manufacturer, quantity, wholesale, retail, warranty));
            System.out.println("Додано в «" + set.getTitle() + "»: " + name
                    + " (" + manufacturer + "), " + quantity + " од.");
        } catch (DuplicateProductException e) {
            System.out.println("Помилка додавання: " + e.getMessage());
        } catch (ProductException e) {
            System.out.println("Некоректні дані товару: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Створення об'єктів через посилання на інтерфейс
        ProductOperations shop1 = new ProductSet("Магазин №1");
        ProductOperations shop2 = new ProductSet("Магазин №2");

        System.out.println("=== Метод 1: додавання товарів ===");
        tryAdd(shop1, "Ноутбук", "Lenovo", 10, 18000, 22000, 24);
        tryAdd(shop1, "Монітор", "Samsung", 5, 6000, 7500, 36);
        tryAdd(shop1, "Миша", "Logitech", 40, 400, 650, 12);
        tryAdd(shop1, "Миша", "Logitech", 5, 400, 650, 12);  // вже є — кількість додається
        tryAdd(shop2, "Ноутбук", "Lenovo", 4, 17500, 23000, 24);
        tryAdd(shop2, "Миша", "Logitech", 10, 380, 600, 12);
        tryAdd(shop2, "Клавіатура", "Logitech", 15, 900, 1200, 24);

        System.out.println("\n=== Обробка виняткових ситуацій при додаванні ===");
        tryAdd(shop1, "ПК", "Lenovo", 1, 15000, 19000, 12);           // №8
        tryAdd(shop1, "Принтер", "HP", 3, 4000, 5200, 12);            // №9
        tryAdd(shop1, "Сканер", "Canon", 2, 5000, 4500, 12);          // №5
        tryAdd(shop1, "Колонки", "Defender", -3, 500, 800, 6);        // №6
        tryAdd(shop1, "Навушники", "Sennheiser", 7, 0, 2500, 12);     // №7
        tryAdd(shop1, "Кабель HDMI", "Vention", 50, 80, 150, 0);      // №11
        tryAdd(shop1, "Монітор", "Samsung", 2, 6000, 7500, 24);       // №4

        System.out.println("\n=== Метод 2: перегляд даних ===");
        shop1.print();
        shop2.print();

        System.out.println("\n=== Зміна даних ===");
        Product monitor = shop1.find("монітор", "SAMSUNG");  // пошук без урахування регістру
        if (monitor != null) {
            try {
                monitor.setRetailPrice(7200);
                System.out.println("Нова роздрібна ціна монітора: " + monitor.getRetailPrice());
                monitor.setRetailPrice(5000);  // нижча за оптову 6000
                System.out.println("Цей рядок не виконається");
            } catch (ProductException e) {
                System.out.println("Помилка зміни ціни: " + e.getMessage());
            }
            try {
                monitor.setQuantity(-1);
            } catch (ProductException e) {
                System.out.println("Помилка зміни кількості: " + e.getMessage());
            }
        }
        Product tablet = shop1.find("Планшет", "Apple Inc");
        System.out.println("Пошук «Планшет (Apple Inc)»: "
                + (tablet == null ? "не знайдено" : "знайдено"));

        System.out.println("\n=== Метод 3: об'єднання множин ===");
        try {
            ProductOperations all = shop1.union(shop2);
            all.print();
        } catch (ProductException e) {
            System.out.println("Помилка об'єднання: " + e.getMessage());
        }

        System.out.println("\n=== Метод 18: видалення товару (найменування, виробник) ===");
        boolean removed = shop2.remove("Миша", "Logitech");
        System.out.println("Миша (Logitech) видалена з «" + shop2.getTitle() + "»: " + removed);
        removed = shop2.remove("Миша", "Logitech");
        System.out.println("Повторне видалення: " + removed + " (товару вже немає)");
        shop2.print();
    }
}