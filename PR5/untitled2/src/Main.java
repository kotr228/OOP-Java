import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

/** Власний перевірюваний виняток: розмірності матриць не дозволяють множення. */
class MatrixDimensionException extends Exception {
    public MatrixDimensionException(int colsA, int rowsB) {
        super("кількість стовпців A (" + colsA + ") не дорівнює кількості рядків B ("
                + rowsB + "), множення неможливе");
    }
}

public class Main {
    static final int MAX_SIZE = 10;

    /** Пояснює, чому токен не вдалося прочитати як int. */
    static String describeBadToken(String token) {
        if (token.matches("[-+]?\\d+")) {
            return "число " + token + " виходить за межі типу int";
        } else if (token.matches("[-+]?\\d*[.,]\\d+")) {
            return "невідповідність типу даних — дробове число " + token
                    + " замість цілого";
        } else {
            return "рядок «" + token + "» замість числа";
        }
    }

    /** Зчитує ціле число; при некоректному введенні просить повторити. */
    static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return scanner.nextInt();
            } catch (InputMismatchException e) {
                String token = scanner.next();
                System.out.println("  Помилка: " + describeBadToken(token)
                        + ". Повторіть введення.");
            }
            // NoSuchElementException тут не перехоплюється — передається в main
        }
    }

    /** Перевіряє розмір матриці; генерує виняток вручну. */
    static void validateSize(int size) {
        if (size < 1 || size > MAX_SIZE) {
            throw new IllegalArgumentException(
                    "розмір має бути в межах від 1 до " + MAX_SIZE);
        }
    }

    /** Зчитує розмір матриці з перевіркою допустимого діапазону. */
    static int readSize(Scanner scanner, String prompt) {
        while (true) {
            int size = readInt(scanner, prompt);
            try {
                validateSize(size);
                return size;
            } catch (IllegalArgumentException e) {
                System.out.println("  Помилка: " + e.getMessage() + ". Повторіть введення.");
            }
        }
    }

    /** Перевіряє, чи можна перемножити матриці. */
    static void checkDimensions(int colsA, int rowsB) throws MatrixDimensionException {
        if (colsA != rowsB) {
            throw new MatrixDimensionException(colsA, rowsB);
        }
    }

    /** Зчитує елементи матриці з клавіатури. */
    static int[][] readMatrix(Scanner scanner, String name, int rows, int cols) {
        int[][] matrix = new int[rows][cols];
        System.out.println("Введіть елементи матриці " + name + " (цілі числа):");
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = readInt(scanner,
                        name + "[" + (i + 1) + "][" + (j + 1) + "] = ");
            }
        }
        return matrix;
    }

    /**
     * Перемножує матриці.
     * @throws MatrixDimensionException якщо розмірності не узгоджені
     * @throws ArithmeticException якщо результат виходить за межі int
     */
    static int[][] multiply(int[][] a, int[][] b) throws MatrixDimensionException {
        checkDimensions(a[0].length, b.length);

        int rows = a.length;
        int cols = b[0].length;
        int inner = b.length;
        int[][] c = new int[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int sum = 0;
                for (int k = 0; k < inner; k++) {
                    sum = Math.addExact(sum, Math.multiplyExact(a[i][k], b[k][j]));
                }
                c[i][j] = sum;
            }
        }
        return c;
    }

    /** Виводить матрицю на екран. */
    static void printMatrix(String title, int[][] matrix) {
        System.out.println(title + " (" + matrix.length + "×" + matrix[0].length + "):");
        for (int[] row : matrix) {
            for (int value : row) {
                System.out.printf("%8d", value);
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("=== Множення матриць A × B ===");
            int rowsA = readSize(scanner, "Кількість рядків матриці A: ");
            int colsA = readSize(scanner, "Кількість стовпців матриці A: ");
            int rowsB = readSize(scanner, "Кількість рядків матриці B: ");
            int colsB = readSize(scanner, "Кількість стовпців матриці B: ");

            // Перевіряємо одразу, щоб не змушувати вводити елементи даремно
            checkDimensions(colsA, rowsB);

            int[][] a = readMatrix(scanner, "A", rowsA, colsA);
            int[][] b = readMatrix(scanner, "B", rowsB, colsB);

            int[][] c = multiply(a, b);

            System.out.println();
            printMatrix("Матриця A", a);
            printMatrix("Матриця B", b);
            printMatrix("Результат C = A × B", c);

        } catch (MatrixDimensionException e) {
            System.out.println("Невідповідність розмірності: " + e.getMessage());
        } catch (ArithmeticException e) {
            System.out.println("Помилка: переповнення типу int під час обчислення ("
                    + e.getMessage() + ")");
        } catch (NoSuchElementException e) {
            System.out.println("Помилка: введення даних перервано");
        } catch (Exception e) {
            System.out.println("Непередбачена помилка: " + e);
        } finally {
            scanner.close();
            System.out.println("Роботу програми завершено.");
        }
    }
}