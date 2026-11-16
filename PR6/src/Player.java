import java.util.Objects;

/** Гравець турніру. Порівнюється за кількістю балів. */
public class Player implements Comparable<Player> {
    private final String name;
    private final int points;

    public Player(String name, int points) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("ім'я гравця не може бути порожнім");
        }
        if (points < 0) {
            throw new IllegalArgumentException("бали не можуть бути від'ємними");
        }
        this.name = name.trim();
        this.points = points;
    }

    public String getName() {
        return name;
    }

    public int getPoints() {
        return points;
    }

    @Override
    public int compareTo(Player other) {
        return Integer.compare(points, other.points);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Player)) {
            return false;
        }
        Player other = (Player) o;
        return points == other.points && name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, points);
    }

    @Override
    public String toString() {
        return name + " (" + points + " балів)";
    }
}