package insurance.model;

/** Об'єкт, який уміє подати інформацію про себе у вигляді тексту. */
public interface Describable {

    /** Повна інформація про об'єкт (для перегляду в програмі). */
    String getInfo();

    /** Коротка інформація для списків: за замовчуванням — 1-й рядок getInfo(). */
    default String getShortInfo() {
        String info = getInfo();
        int end = info.indexOf('\n');
        return end < 0 ? info : info.substring(0, end);
    }
}
