enum Membership {
    BASIC,
    STANDARD,
    PREMIUM
}

class Member {
    String name;                           // поле об'єкта
    int visits;                            // поле об'єкта
    Membership membership;                 // поле об'єкта типу enum

    static int count = 0;                  // статичне поле: спільне для всіх
    static final int ACTIVE_LIMIT = 8;     // константа: відвідувань на місяць

    Member(String name, int visits, Membership membership) {
        this.name = name;
        this.visits = visits;
        this.membership = membership;
        count++;
    }

    void show() {
        System.out.println(name + " — відвідувань: " + visits
                + ", абонемент: " + membershipName(membership));
    }

    static boolean isActive(int visits) {
        return visits >= ACTIVE_LIMIT;
    }

    static String membershipName(Membership membership) {
        switch (membership) {
            case BASIC:
                return "Базовий";
            case STANDARD:
                return "Стандартний";
            case PREMIUM:
                return "Преміум";
            default:
                return "Невідомий";
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Member[] members = {
                new Member("Олена Коваль", 12, Membership.PREMIUM),
                new Member("Андрій Шевченко", 5, Membership.BASIC),
                new Member("Ірина Бондар", 9, Membership.STANDARD),
                new Member("Максим Ковальчук", 3, Membership.STANDARD)
        };

        System.out.println("Кількість створених відвідувачів: " + Member.count);

        System.out.println("\n=== Усі відвідувачі ===");
        for (Member member : members) {
            member.show();
        }

        System.out.println("\n=== Активні відвідувачі (від "
                + Member.ACTIVE_LIMIT + " відвідувань) ===");
        int activeCount = 0;
        for (Member member : members) {
            if (Member.isActive(member.visits)) {
                System.out.println(member.name + " — " + member.visits);
                activeCount++;
            }
        }
        System.out.println("Кількість активних: " + activeCount);

        System.out.println("\n=== Відвідувачі з прізвищем, що містить «Коваль» ===");
        for (Member member : members) {
            if (member.name.contains("Коваль")) {
                System.out.println(member.name.toUpperCase());
            }
        }

        System.out.println("\n=== Кількість відвідувачів за типом абонемента ===");
        for (Membership type : Membership.values()) {
            int n = 0;
            for (Member member : members) {
                if (member.membership == type) {
                    n++;
                }
            }
            System.out.println(Member.membershipName(type) + " (" + type.name() + "): " + n);
        }
    }
}