import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        final int MAX_SIZE = 10;
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("=== Множення матриць A × B ===");
            System.out.print("Кількість рядків матриці A: ");
            int rowsA = scanner.nextInt();
            System.out.print("Кількість стовпців матриці A: ");
            int colsA = scanner.nextInt();
            System.out.print("Кількість рядків матриці B: ");
            int rowsB = scanner.nextInt();
            System.out.print("Кількість стовпців матриці B: ");
            int colsB = scanner.nextInt();

            // Перевірка розмірів — виняток генерується вручну
            if (rowsA < 1 || colsA < 1 || rowsB < 1 || colsB < 1
                    || rowsA > MAX_SIZE || colsA > MAX_SIZE
                    || rowsB > MAX_SIZE || colsB > MAX_SIZE) {
                throw new IllegalArgumentException(
                        "розміри матриць мають бути в межах від 1 до " + MAX_SIZE);
            }
            if (colsA != rowsB) {
                throw new IllegalArgumentException(
                        "невідповідність розмірності: кількість стовпців A (" + colsA
                                + ") не дорівнює кількості рядків B (" + rowsB + ")");
            }

            int[][] a = new int[rowsA][colsA];
            System.out.println("Введіть елементи матриці A (цілі числа):");
            for (int i = 0; i < rowsA; i++) {
                for (int j = 0; j < colsA; j++) {
                    System.out.print("A[" + (i + 1) + "][" + (j + 1) + "] = ");
                    a[i][j] = scanner.nextInt();
                }
            }

            int[][] b = new int[rowsB][colsB];
            System.out.println("Введіть елементи матриці B (цілі числа):");
            for (int i = 0; i < rowsB; i++) {
                for (int j = 0; j < colsB; j++) {
                    System.out.print("B[" + (i + 1) + "][" + (j + 1) + "] = ");
                    b[i][j] = scanner.nextInt();
                }
            }

            // Множення з контролем переповнення
            int[][] c = new int[rowsA][colsB];
            for (int i = 0; i < rowsA; i++) {
                for (int j = 0; j < colsB; j++) {
                    int sum = 0;
                    for (int k = 0; k < colsA; k++) {
                        sum = Math.addExact(sum, Math.multiplyExact(a[i][k], b[k][j]));
                    }
                    c[i][j] = sum;
                }
            }

            System.out.println("\nРезультат C = A × B (" + rowsA + "×" + colsB + "):");
            for (int[] row : c) {
                for (int value : row) {
                    System.out.printf("%8d", value);
                }
                System.out.println();
            }

        } catch (InputMismatchException e) {
            // Некоректний токен ще лишається у Scanner — зчитуємо його для аналізу
            String token = scanner.next();
            if (token.matches("[-+]?\\d+")) {
                System.out.println("Помилка: число " + token
                        + " виходить за межі типу int (від " + Integer.MIN_VALUE
                        + " до " + Integer.MAX_VALUE + ")");
            } else if (token.matches("[-+]?\\d*[.,]\\d+")) {
                System.out.println("Помилка: невідповідність типу даних — введено дробове число "
                        + token + ", а очікувалось ціле");
            } else {
                System.out.println("Помилка: введено рядок «" + token + "» замість числа");
            }
        } catch (NoSuchElementException e) {
            System.out.println("Помилка: введення даних перервано");
        } catch (ArithmeticException e) {
            System.out.println("Помилка: переповнення типу int під час обчислення ("
                    + e.getMessage() + ")");
        } catch (IllegalArgumentException e) {
            System.out.println("Помилка: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Непередбачена помилка: " + e);
        } finally {
            scanner.close();
            System.out.println("Роботу програми завершено.");
        }
    }
}