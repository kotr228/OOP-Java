package io.github.kotr228.platform.swing;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Спільні налаштування інтерфейсу для всіх настільних програм пакета. */
public final class SwingUi {
    private static final Logger LOG = Logger.getLogger(SwingUi.class.getName());
    private static final int FONT_SIZE = 15;

    private SwingUi() {
    }

    /**
     * Вигляд вікон операційної системи, збільшений шрифт
     * і українські підписи стандартних кнопок Swing.
     * Викликається один раз до створення першого вікна.
     */
    public static void install() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException e) {
            LOG.log(Level.WARNING, "Системний вигляд недоступний", e);
        }
        for (Object key : UIManager.getDefaults().keySet().toArray()) {
            if (UIManager.get(key) instanceof FontUIResource f) {
                UIManager.put(key,
                        new FontUIResource(f.getFamily(), f.getStyle(), FONT_SIZE));
            }
        }
        UIManager.put("OptionPane.yesButtonText", "Так");
        UIManager.put("OptionPane.noButtonText", "Ні");
        UIManager.put("OptionPane.okButtonText", "Гаразд");
        UIManager.put("OptionPane.cancelButtonText", "Скасувати");
        UIManager.put("FileChooser.openButtonText", "Обрати");
        UIManager.put("FileChooser.saveButtonText", "Обрати");
        UIManager.put("FileChooser.cancelButtonText", "Скасувати");
        UIManager.put("FileChooser.lookInLabelText", "Каталог:");
        UIManager.put("FileChooser.fileNameLabelText", "Ім'я:");
        UIManager.put("FileChooser.filesOfTypeLabelText", "Тип:");
    }

    /**
     * Непередбачена помилка не повинна мовчки «вбивати» програму:
     * вона записується в журнал, а користувач бачить зрозуміле повідомлення.
     */
    public static void installErrorHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            LOG.log(Level.SEVERE, "Непередбачена помилка в потоці "
                    + thread.getName(), error);
            Runnable show = () -> JOptionPane.showMessageDialog(null,
                    "Сталася непередбачена помилка:\n" + error
                            + "\n\nПодробиці записано в журнал програми.",
                    "Помилка програми", JOptionPane.ERROR_MESSAGE);
            if (SwingUtilities.isEventDispatchThread()) {
                show.run();
            } else {
                SwingUtilities.invokeLater(show);
            }
        });
    }
}
