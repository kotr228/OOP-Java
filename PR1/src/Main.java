public class Main {
    public static void main(String[] args) {
        String[] movies = {
                "1+1",
                "",
                "Термінатор",
                "Зоряні війни",
                "END",
                "Гра пресотолів"
        };

        int countLong = 0;

        for (int i = 0; i < movies.length; i++) {
            String movie = movies[i];

            if (movie.isEmpty()) {
                continue;
            }

            if (movie.equals("END")) {
                System.out.println("Перегляд масиву завершено.");
                break;
            }

            String upperMovie = movie.toUpperCase();
            System.out.println("Елемент " + i + ": " + upperMovie);

            if (movie.length() > 10) {
                countLong++;
            }
        }

        System.out.println();
        System.out.println("Кількість назв довжиною більше 10 символів: " + countLong);
    }
}