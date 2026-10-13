enum Size {
    SMALL,
    MEDIUM,
    LARGE
}

class Animal {
    String name;
    int age;        // років
    double weight;  // кг

    Animal(String name, int age, double weight) {
        this.name = name;
        this.age = age;
        this.weight = weight;
    }

    void showInfo() {
        System.out.println(name + ", " + age + " р., " + weight + " кг");
    }
}

class Dog extends Animal {
    String breed;
    Size size;

    static final double GRAMS_PER_KG = 25;  // базова норма корму, г на 1 кг ваги

    Dog(String name, int age, double weight, String breed, Size size) {
        super(name, age, weight);
        this.breed = breed;
        this.size = size;
    }

    void showDogInfo() {
        showInfo();  // успадкований метод
        System.out.println("  Порода: " + breed + ", розмір: " + sizeName(size));
    }

    static String sizeName(Size size) {
        switch (size) {
            case SMALL:
                return "малий";
            case MEDIUM:
                return "середній";
            case LARGE:
                return "великий";
            default:
                return "невідомий";
        }
    }

    // Версія 1: порція лише за вагою
    double feed(double weight) {
        return weight * GRAMS_PER_KG;
    }

    // Версія 2: за вагою та віком
    double feed(double weight, int age) {
        double ageFactor;
        if (age <= 1) {
            ageFactor = 1.5;   // цуценя росте — потрібно більше
        } else if (age >= 8) {
            ageFactor = 0.8;   // літній пес — менше
        } else {
            ageFactor = 1.0;
        }
        return feed(weight) * ageFactor;
    }

    // Версія 3: за вагою, віком і коефіцієнтом активності
    double feed(double weight, int age, double activity) {
        return feed(weight, age) * activity;
    }
}

public class Main {
    public static void main(String[] args) {
        Dog[] dogs = {
                new Dog("Рекс", 5, 32.0, "Німецька вівчарка", Size.LARGE),
                new Dog("Бусинка", 1, 3.5, "Чихуахуа", Size.SMALL),
                new Dog("Чарлі", 9, 14.0, "Бігль", Size.MEDIUM),
                new Dog("Бім", 3, 18.0, "Бордер-коллі", Size.MEDIUM)
        };
        double[] activity = {1.3, 1.0, 0.9, 1.4};

        System.out.println("=== Собаки та добова порція корму, г ===");
        for (int i = 0; i < dogs.length; i++) {
            Dog dog = dogs[i];
            dog.showDogInfo();
            System.out.printf("  За вагою: %.1f%n", dog.feed(dog.weight));
            System.out.printf("  За вагою й віком: %.1f%n", dog.feed(dog.weight, dog.age));
            System.out.printf("  За вагою, віком і активністю (%.1f): %.1f%n",
                    activity[i], dog.feed(dog.weight, dog.age, activity[i]));
        }

        Size target = Size.MEDIUM;
        System.out.println("\n=== Собаки розміру «" + Dog.sizeName(target) + "» ===");
        for (Dog dog : dogs) {
            if (dog.size == target) {
                System.out.println(dog.name + " (" + dog.breed + ")");
            }
        }
    }
}