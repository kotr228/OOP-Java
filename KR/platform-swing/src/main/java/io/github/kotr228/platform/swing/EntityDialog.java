package io.github.kotr228.platform.swing;

import io.github.kotr228.platform.core.model.InvalidDataException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

/**
 * Базове модальне вікно введення даних: поля в два стовпці (підпис — поле)
 * та кнопки «Зберегти» і «Скасувати». Похідний клас реалізує save().
 */
public abstract class EntityDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final JPanel form = new JPanel(new GridBagLayout());
    private int rows;
    private boolean saved;

    @SuppressWarnings("this-escape")      // кнопки створюються в конструкторі
    protected EntityDialog(Window owner, String title) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));

        JButton saveButton = new JButton("Зберегти");
        JButton cancelButton = new JButton("Скасувати");
        saveButton.addActionListener(e -> onSave());
        cancelButton.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(saveButton);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(saveButton);
        buttons.add(cancelButton);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    /** Додає до форми рядок «підпис — поле введення». */
    protected void addField(String label, JComponent field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = rows++;
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        form.add(new JLabel(label + ":"), c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        form.add(field, c);
    }

    /** Показує вікно; повертає true, якщо дані збережено. */
    public boolean showDialog() {
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(getOwner());
        setVisible(true);          // модальне вікно: виконання чекає тут
        return saved;
    }

    /**
     * Перевіряє введені значення та зберігає їх у страховій компанії.
     * Некоректні дані — виняток InvalidDataException з поясненням.
     */
    protected abstract void save();

    private void onSave() {
        try {
            save();
            saved = true;
            dispose();
        } catch (InvalidDataException e) {
            // вікно лишається відкритим, щоб користувач виправив дані
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Некоректні дані", JOptionPane.WARNING_MESSAGE);
        }
    }
}
