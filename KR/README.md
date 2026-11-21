# Enterprise Suite — пакет програм для підприємств

Набір настільних програм на Java зі спільною основою. Перша програма пакета —
**«Страхова компанія»**: облік агентів, клієнтів і страхових полісів
(виросла з курсової роботи з ООП, варіант 26).

## Швидкий старт

Потрібен лише **JDK 21+**. Maven встановлювати не треба: Maven Wrapper
(`mvnw`) завантажить потрібну версію сам.

```bash
# збірка + усі тести + готовий jar
./mvnw verify                       # Windows: mvnw.cmd verify

# запуск програми
java -jar insurance-desktop/target/insurance-app.jar

# консольна демонстрація запитів (без вікон)
./mvnw -q -pl insurance-core -am install -DskipTests
./mvnw -q -pl insurance-core exec:java
```

**IntelliJ IDEA:** `File → Open` → оберіть файл `KR/pom.xml` → *Open as Project*.
Головний клас: `io.github.kotr228.insurance.desktop.InsuranceApp`.

### Де зберігаються дані

| Пріоритет | Джерело                                   | Приклад                                         |
|-----------|-------------------------------------------|-------------------------------------------------|
| 1         | системна властивість `insurance.dataDir`  | `java -Dinsurance.dataDir=D:\data -jar ...`     |
| 2         | змінна середовища `INSURANCE_DATA_DIR`    | `set INSURANCE_DATA_DIR=D:\data`                |
| 3         | за замовчуванням                          | `%USERPROFILE%\InsuranceData`                   |

У каталозі даних — `agents.csv`, `clients.csv`, `policies.csv` (UTF-8, роздільник `;`,
відкриваються в Excel) і журнал роботи `logs/insurance-0.log`.
Якщо даних ще немає, програма відкриває демонстраційні.

## Архітектура

```
KR/
├── pom.xml                 батьківський POM: Java 21, версії залежностей і плагінів
├── platform-core/          ┐ СПІЛЬНА ОСНОВА — перевикористовується всіма програмами
├── platform-swing/         ┘
├── insurance-core/         ┐ ПРОГРАМА «Страхова компанія»
└── insurance-desktop/      ┘
```

```
insurance-desktop ──► insurance-core ──► platform-core
        │                                     ▲
        └──────────► platform-swing ──────────┘
```

Залежності спрямовані лише «вниз»: ядро не знає про інтерфейс, платформа не
знає про страхування. Тому ядро можна тестувати без вікон, а інтерфейс —
замінити (наприклад, на веб) без зміни бізнес-логіки.

| Модуль | Що містить | Чого НЕ містить |
|---|---|---|
| **platform-core** | `Identifiable`, `InvalidDataException`, `FullName`, `PhoneNumber`, `Money` (суми в `BigDecimal`), `Registry` (сховище в пам'яті з видачею id), `DataStore` (інтерфейс сховища), `Csv` (запис/розбір з екрануванням) | залежностей, крім JDK |
| **platform-swing** | `EntityTablePanel` (таблиця з пошуком, сортуванням, кнопками й гарячими клавішами), `EntityDialog` (форма введення), `EntityTableModel`, `Formats`, `SwingUi` (тема, шрифт, обробник помилок) | предметної логіки |
| **insurance-core** | модель (`Agent`, `Client`, `Policy`, …), `InsuranceCompany` (бізнес-правила й запити), `CsvInsuranceStore`, демо-дані | Swing |
| **insurance-desktop** | головне вікно, діалоги, вкладка запитів, точка входу | бізнес-правил |

### Ключові рішення

- **Гроші — `BigDecimal`** з двома знаками після коми (`Money`). `double` дає похибки
  (`0.1 + 0.2 ≠ 0.3`), неприпустимі у фінансах. Суми з частками копійки відхиляються.
- **Id видає реєстр**, а не статичний лічильник у класі: дві компанії в одній
  програмі (або два тести) не впливають одна на одну. Id видалених записів
  повторно не використовуються.
- **Зміни лише через `InsuranceCompany`**: `create*`, `update*`, `remove*`.
  Списки назовні — лише для читання. Редагування атомарне: усі нові значення
  перевіряються разом, і об'єкт змінюється або повністю, або ніяк.
- **Модель не знає про файли**: перетворення в CSV і назад — у `CsvInsuranceStore`.
  Інтерфейс працює з `DataStore<InsuranceCompany>`, тож перехід на базу даних —
  це новий клас `JdbcInsuranceStore implements DataStore<…>` і один рядок у
  `InsuranceApp`.
- **Запис файлів атомарний** (через тимчасовий файл): збій посеред запису не
  псує збережені дані. Пошкоджений файл повідомляє назву файлу й номер рядка.
- **Непередбачені помилки** записуються в журнал і показуються користувачу,
  а не «вбивають» програму мовчки.

## Як додати до пакета нову програму

Наприклад, «Склад»:

1. Створіть модулі `warehouse-core` (залежить від `platform-core`) і
   `warehouse-desktop` (від `warehouse-core` і `platform-swing`); скопіюйте
   POM-файли з `insurance-*` і додайте модулі в `<modules>` кореневого `pom.xml`.
2. Сутності реалізують `Identifiable`; перевірки кидають `InvalidDataException`;
   суми — `BigDecimal` через `Money`.
3. Сервіс (аналог `InsuranceCompany`) зберігає дані в `Registry` і видає id через
   `registry.nextId()`.
4. Сховище реалізує `DataStore<…>`; для CSV використовуйте `Csv.join` / `Csv.split`.
5. Вкладка-список у GUI — один рядок:
   `new EntityTablePanel<>(model, "Додати", this::add, this::edit, this::delete)`;
   форма — підклас `EntityDialog`.
6. Якщо щось потрібне двом програмам — переносьте у `platform-*`, а не копіюйте.

## Якість коду

- компіляція з `-Xlint:all -Werror` — жодного попередження;
- тести JUnit у кожному модулі (`./mvnw test`), зокрема всі 9 сценаріїв
  перевірки з пояснювальної записки курсової;
- CI (GitHub Actions, `.github/workflows/kr-build.yml`) збирає й тестує проєкт
  на кожен push і зберігає готовий `insurance-app.jar`;
- однакове форматування — `.editorconfig`.

## Подальші кроки

- [ ] `JdbcInsuranceStore` (H2 вбудована / PostgreSQL) — реалізація `DataStore`
- [ ] інжекція `Clock` у перевірки віку й статусу поліса (детерміновані тести дат)
- [ ] звіти (експорт у PDF / Excel)
- [ ] користувачі й ролі (агент бачить лише своїх клієнтів)
- [ ] інсталятор Windows через `jpackage`
