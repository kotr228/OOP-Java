import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;

public class Main {

    public static String findCommonPairs(String first, String second) {
        Set<String> pairs = new LinkedHashSet<>();

        for (int i = 0; i < first.length() - 1; i++) {
            String pair = first.substring(i, i + 2);
            if (second.contains(pair)) {
                pairs.add(pair);
            }
        }

        StringBuilder result = new StringBuilder();
        for (String pair : pairs) {
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(pair);
        }
        return result.toString();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Введіть перший рядок: ");
        String first = scanner.nextLine();

        System.out.print("Введіть другий рядок: ");
        String second = scanner.nextLine();

        if (first.length() < 2 || second.length() < 2) {
            System.out.println("Обидва рядки мають містити щонайменше 2 символи.");
            return;
        }

        String third = findCommonPairs(first, second);

        if (third.isEmpty()) {
            System.out.println("Спільних двосимвольних послідовностей не знайдено.");
        } else {
            System.out.println("Третій рядок: " + third);
        }

        scanner.close();
    }
}