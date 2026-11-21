package io.github.kotr228.platform.swing;

import javax.swing.table.DefaultTableCellRenderer;
import java.io.Serial;

/** Клітинка таблиці, що показує значення через {@link Formats#display}. */
public class FormattedCellRenderer extends DefaultTableCellRenderer {
    @Serial
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("this-escape")      // стандартне налаштування Swing-компонента
    public FormattedCellRenderer(int horizontalAlignment) {
        setHorizontalAlignment(horizontalAlignment);
    }

    @Override
    protected void setValue(Object value) {
        setText(Formats.display(value));
    }
}
