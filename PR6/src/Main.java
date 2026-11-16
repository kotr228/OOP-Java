import java.util.List;

public class Main {
    static final int DELAY_MS = 300;

    // ---------- Підготовка даних ----------

    static Ranking<Integer> createScores() {
        Ranking<Integer> scores = new Ranking<>("Бали за тур");
        for (int value : new int[]{87, 45, 92, 45, 70, 92, 61}) {
            scores.add(value);   // автоупаковка int → Integer
        }
        return scores;
    }

    static Ranking<Player> createPlayers() {
        Ranking<Player> players = new Ranking<>("Таблиця гравців");
        players.add(new Player("Оксана", 1450));
        players.add(new Player("Андрій", 1320));
        players.add(new Player("Марко", 1580));
        players.add(new Player("Ірина", 1320));
        players.add(new Player("Тарас", 1505));
        return players;
    }

    // ---------- Режими запуску ----------

    static void runSequential() throws InterruptedException {
        System.out.println("\n=== Послідовний режим ===");
        DataProcessor<Integer> scores = new DataProcessor<>(createScores(), DELAY_MS);
        DataProcessor<Player> players = new DataProcessor<>(createPlayers(), DELAY_MS);
        Thread first = new Thread(scores, "Потік-Бали");
        Thread second = new Thread(players, "Потік-Гравці");

        long start = System.nanoTime();
        first.start();
        first.join();      // другий потік стартує лише після завершення першого
        second.start();
        second.join();
        long elapsed = (System.nanoTime() - start) / 1_000_000;

        System.out.println("Результати:");
        scores.printResult();
        players.printResult();
        System.out.println("Час: " + elapsed + " мс");
    }

    static void runParallel(int attempt) throws InterruptedException {
        System.out.println("\n=== Паралельний режим, запуск " + attempt + " ===");
        // Для кожного запуску — нові дані, нові обробники і нові об'єкти Thread
        DataProcessor<Integer> scores = new DataProcessor<>(createScores(), DELAY_MS);
        DataProcessor<Player> players = new DataProcessor<>(createPlayers(), DELAY_MS);
        Thread first = new Thread(scores, "Потік-Бали");
        Thread second = new Thread(players, "Потік-Гравці");

        long start = System.nanoTime();
        first.start();
        second.start();
        first.join();
        second.join();
        long elapsed = (System.nanoTime() - start) / 1_000_000;

        System.out.println("Результати:");
        scores.printResult();
        players.printResult();
        System.out.println("Час: " + elapsed + " мс");
    }

    // ---------- Перевірки граничних випадків ----------

    static void checkEdgeCases() throws InterruptedException {
        System.out.println("\n=== Перевірка: порожній набір і один елемент ===");
        Ranking<Integer> empty = new Ranking<>("Порожня таблиця");
        Ranking<Player> single = new Ranking<>("Один гравець");
        single.add(new Player("Олег", 1200));

        DataProcessor<Integer> emptyProc = new DataProcessor<>(empty, DELAY_MS);
        DataProcessor<Player> singleProc = new DataProcessor<>(single, DELAY_MS);
        Thread t1 = new Thread(emptyProc, "Потік-Порожній");
        Thread t2 = new Thread(singleProc, "Потік-Один");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        emptyProc.printResult();
        singleProc.printResult();

        System.out.println("\n=== Перевірка: заборона null ===");
        try {
            empty.add(null);
        } catch (IllegalArgumentException e) {
            System.out.println("Відхилено: " + e.getMessage());
        }

        System.out.println("\n=== Перевірка: переривання потоку ===");
        DataProcessor<Integer> interrupted = new DataProcessor<>(createScores(), DELAY_MS);
        Thread t3 = new Thread(interrupted, "Потік-Переривання");
        t3.start();
        Thread.sleep(700);    // даємо вивести кілька елементів
        t3.interrupt();
        t3.join();
        interrupted.printResult();
    }

    // ---------- Додаткове завдання: wildcard ----------

    static double sum(List<? extends Number> values) {
        double total = 0;
        for (Number value : values) {
            total += value.doubleValue();
        }
        return total;
    }

    public static void main(String[] args) throws InterruptedException {
        runSequential();
        runParallel(1);
        runParallel(2);
        checkEdgeCases();

        System.out.println("\n=== Додаткове завдання: sum(List<? extends Number>) ===");
        List<Integer> ints = List.of(87, 45, 92);
        List<Double> doubles = List.of(1.5, 2.25, 3.0);
        System.out.println("Сума Integer: " + sum(ints));
        System.out.println("Сума Double: " + sum(doubles));

        // Помилки, які виявляє компілятор (розкоментуйте, щоб показати):
        // Ranking<int> r1;                 // примітив не може бути аргументом типу
        // Ranking<Object> r2;              // Object не реалізує Comparable<Object>
        // createScores().add("текст");     // String не є Integer
        // Integer bad = createPlayers().min();  // min() повертає Player, а не Integer
    }
}