package insurance.app;

import insurance.gui.MainFrame;
import insurance.service.InsuranceCompany;
import insurance.storage.CsvStorage;
import insurance.storage.DataFileException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Точка входу програми «Страхова компанія».
 * Дані читаються з каталогу data; якщо його ще немає — завантажуються
 * демонстраційні дані.
 */
public class Main {
    public static final Path DATA_DIR = Path.of("data");

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame.applyUiFont();
            InsuranceCompany company;
            String message;
            if (CsvStorage.hasData(DATA_DIR)) {
                try {
                    company = CsvStorage.load(DATA_DIR, DemoData.COMPANY_NAME);
                    message = "Дані завантажено з каталогу "
                            + DATA_DIR.toAbsolutePath();
                } catch (IOException | DataFileException e) {
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
            MainFrame frame = new MainFrame(company, DATA_DIR);
            frame.setVisible(true);
            frame.showStatus(message);
        });
    }
}
