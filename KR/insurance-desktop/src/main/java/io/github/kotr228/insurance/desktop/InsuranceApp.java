package io.github.kotr228.insurance.desktop;

import io.github.kotr228.insurance.demo.DemoData;
import io.github.kotr228.insurance.service.InsuranceCompany;
import io.github.kotr228.insurance.storage.CsvInsuranceStore;
import io.github.kotr228.platform.core.storage.DataFileException;
import io.github.kotr228.platform.core.storage.DataStore;
import io.github.kotr228.platform.swing.SwingUi;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Точка входу настільної програми «Страхова компанія».
 *
 * <p>Каталог даних (перший знайдений):
 * <ol>
 *   <li>системна властивість {@code -Dinsurance.dataDir=...};</li>
 *   <li>змінна середовища {@code INSURANCE_DATA_DIR};</li>
 *   <li>{@code <домашній каталог>/InsuranceData}.</li>
 * </ol>
 * Якщо збережених даних ще немає, відкриваються демонстраційні дані.
 * Журнал роботи записується в підкаталог {@code logs} каталогу даних.
 */
public final class InsuranceApp {
    public static final String DATA_DIR_PROPERTY = "insurance.dataDir";
    public static final String DATA_DIR_ENV = "INSURANCE_DATA_DIR";

    private static final Logger LOG = Logger.getLogger(InsuranceApp.class.getName());

    private InsuranceApp() {
    }

    public static void main(String[] args) {
        Path dataDir = dataDirectory();
        setUpLogging(dataDir);
        SwingUi.installErrorHandler();
        LOG.info(() -> "Запуск програми, каталог даних: " + dataDir.toAbsolutePath());

        Function<Path, DataStore<InsuranceCompany>> storeFactory =
                dir -> new CsvInsuranceStore(dir, DemoData.COMPANY_NAME);
        SwingUtilities.invokeLater(() -> start(storeFactory.apply(dataDir),
                storeFactory));
    }

    private static void start(DataStore<InsuranceCompany> store,
                              Function<Path, DataStore<InsuranceCompany>> factory) {
        SwingUi.install();
        InsuranceCompany company;
        String message;
        if (store.exists()) {
            try {
                company = store.load();
                message = "Дані завантажено: " + store.location();
            } catch (IOException | DataFileException e) {
                LOG.log(Level.WARNING, "Не вдалося прочитати дані", e);
                JOptionPane.showMessageDialog(null, e.getMessage()
                                + "\nБуде відкрито демонстраційні дані.",
                        "Помилка читання даних", JOptionPane.ERROR_MESSAGE);
                company = DemoData.create();
                message = "Відкрито демонстраційні дані";
            }
        } else {
            company = DemoData.create();
            message = "Збережених даних немає — відкрито демонстраційні дані";
        }
        MainFrame frame = new MainFrame(company, store, factory);
        frame.setVisible(true);
        frame.showStatus(message);
    }

    static Path dataDirectory() {
        String fromProperty = System.getProperty(DATA_DIR_PROPERTY);
        if (fromProperty != null && !fromProperty.isBlank()) {
            return Path.of(fromProperty);
        }
        String fromEnv = System.getenv(DATA_DIR_ENV);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return Path.of(fromEnv);
        }
        return Path.of(System.getProperty("user.home"), "InsuranceData");
    }

    /** Журнал у файлах logs/insurance-0.log … (до 5 файлів по 1 МБ). */
    private static void setUpLogging(Path dataDir) {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tF %1$tT %4$-7s %3$s — %5$s%6$s%n");
        try {
            Path logs = dataDir.resolve("logs");
            Files.createDirectories(logs);
            FileHandler file = new FileHandler(
                    logs.resolve("insurance-%g.log").toString(), 1_000_000, 5, true);
            file.setEncoding(StandardCharsets.UTF_8.name());
            file.setFormatter(new SimpleFormatter());
            Logger.getLogger("io.github.kotr228").addHandler(file);
        } catch (IOException | SecurityException e) {
            LOG.log(Level.WARNING, "Журнал у файл недоступний", e);
        }
    }
}
