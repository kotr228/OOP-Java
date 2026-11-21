# Журнал змін

Формат — [Keep a Changelog](https://keepachangelog.com/uk/1.1.0/),
версії — [Semantic Versioning](https://semver.org/lang/uk/).

## [Unreleased]

### Додано
- Maven Enforcer: перевірка JDK 21+, Maven 3.9+ і узгодженості залежностей.
- JaCoCo: звіт покриття і мінімальний поріг для модулів ядра.
- CI на Linux і Windows; причина падіння виводиться в анотації запуску.
- Dependabot для залежностей Maven і GitHub Actions.
- `InsuranceCompany(String, Clock)` і `today()`; `Client.getAgeOn(LocalDate)`.

### Змінено
- Обмеження віку клієнта перевіряє `InsuranceCompany` (при оформленні та зміні
  дати народження), а не конструктор `Client`.
- `Client.getAge()` замінено на `getAgeOn(LocalDate)`.
- Самодостатній `insurance-app.jar` збирається окремим файлом поруч із модульним jar.
- Виведення журналів під час тестів — у `target/surefire-reports`.

### Виправлено
- Збережений клієнт, якому з часом виповнилося понад 100 років, більше не
  ламає завантаження даних.
- Повторна збірка без `clean` більше не пакує готовий jar сам у себе.

## [1.0.0-SNAPSHOT] — основа пакета

- Багатомодульний Maven-проєкт: `platform-core`, `platform-swing`,
  `insurance-core`, `insurance-desktop`.
- Гроші в `BigDecimal`, id від реєстру, сховище за інтерфейсом `DataStore`,
  атомарний запис CSV, журнал роботи, 57 тестів.
