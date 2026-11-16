/**
 * Узагальнений обробник турнірної таблиці.
 * Виводить елементи із затримкою та знаходить мінімум і максимум.
 */
public class DataProcessor<T extends Comparable<T>> implements Runnable {
    private final Ranking<T> ranking;
    private final int delayMs;

    // Результати записує робочий потік, читає main після join()
    private T min;
    private T max;
    private boolean completed;

    public DataProcessor(Ranking<T> ranking, int delayMs) {
        this.ranking = ranking;
        this.delayMs = delayMs;
    }

    @Override
    public void run() {
        String thread = Thread.currentThread().getName();
        System.out.println(thread + ": початок обробки «" + ranking.getTitle()
                + "» (" + ranking.size() + " ел.)");

        if (ranking.isEmpty()) {
            System.out.println(thread + ": набір порожній — результат не визначений");
            completed = true;
            return;
        }

        try {
            int index = 0;
            for (T item : ranking.getItems()) {
                System.out.println(thread + ": [" + index++ + "] " + item);
                Thread.sleep(delayMs);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();   // відновлюємо прапорець переривання
            System.out.println(thread + ": обробку перервано");
            return;
        }

        min = ranking.min();
        max = ranking.max();
        completed = true;
        System.out.println(thread + ": обробку завершено");
    }

    public void printResult() {
        String title = ranking.getTitle();
        if (!completed) {
            System.out.println("  " + title + ": обробку перервано, результат відсутній");
        } else if (min == null) {
            System.out.println("  " + title + ": результат не визначений (порожній набір)");
        } else {
            System.out.println("  " + title + ": мінімум = " + min + ", максимум = " + max);
        }
    }
}