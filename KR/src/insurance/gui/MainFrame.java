package insurance.gui;

import insurance.gui.EntityTableModel.Column;
import insurance.model.Agent;
import insurance.model.Client;
import insurance.model.InvalidDataException;
import insurance.model.Policy;
import insurance.service.InsuranceCompany;
import insurance.storage.CsvStorage;
import insurance.storage.DataFileException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/** Головне вікно програми: вкладки «Агенти», «Клієнти», «Поліси», «Запити». */
public final class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private transient InsuranceCompany company;
    private transient Path dataDir;
    private boolean modified;

    private final EntityTableModel<Agent> agentModel =
            new EntityTableModel<>(List.of(
                new Column<>("Id", Integer.class, Agent::getId),
                new Column<>("ПІБ", String.class,
                        a -> a.getFullName().toString()),
                new Column<>("Телефон", String.class,
                        a -> a.getPhone().getFormatted()),
                new Column<>("Стаж, р.", Integer.class,
                        Agent::getExperienceYears),
                new Column<>("Клієнтів", Integer.class,
                        a -> company.countClientsByAgent().get(a))));
    private final EntityTableModel<Client> clientModel =
            new EntityTableModel<>(List.of(
                new Column<>("Id", Integer.class, Client::getId),
                new Column<>("ПІБ", String.class,
                        c -> c.getFullName().toString()),
                new Column<>("Телефон", String.class,
                        c -> c.getPhone().getFormatted()),
                new Column<>("Дата народження", String.class,
                        c -> Formats.date(c.getBirthDate())),
                new Column<>("Вік", Integer.class, Client::getAge),
                new Column<>("Агент", String.class,
                        c -> c.getAgent().toString())));
    private final EntityTableModel<Policy> policyModel =
            new EntityTableModel<>(List.of(
                new Column<>("Номер", String.class,
                        Policy::getPolicyNumber),
                new Column<>("Тип", String.class,
                        p -> p.getInsuranceType().toString()),
                new Column<>("Клієнт", String.class,
                        p -> p.getClient().toString()),
                new Column<>("Початок", String.class,
                        p -> Formats.date(p.getPeriod().getStartDate())),
                new Column<>("Завершення", String.class,
                        p -> Formats.date(p.getPeriod().getEndDate())),
                new Column<>("Страховий випадок", String.class,
                        Policy::getInsuredEvent),
                new Column<>("Вартість, грн", Double.class,
                        Policy::getCost)));

    private final JTable agentTable =
            createTable(agentModel, 50, 330, 200, 90, 90);
    private final JTable clientTable =
            createTable(clientModel, 50, 290, 190, 150, 60, 170);
    private final JTable policyTable =
            createTable(policyModel, 130, 190, 140, 110, 110, 230, 120);
    private final QueryPanel queryPanel = new QueryPanel(() -> company);
    private final JTabbedPane tabs = new JTabbedPane();
    private final JLabel statusLabel = new JLabel(" ");
    private final JLabel countsLabel = new JLabel(" ");

    public MainFrame(InsuranceCompany company, Path dataDir) {
        this.company = company;
        this.dataDir = dataDir;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });

        setJMenuBar(createMenu());
        tabs.addTab("Агенти", createTab(agentTable, this::editAgent,
                button("Додати", e -> addAgent()),
                button("Редагувати", e -> editAgent()),
                button("Видалити", e -> deleteAgent())));
        tabs.addTab("Клієнти", createTab(clientTable, this::editClient,
                button("Додати", e -> addClient()),
                button("Редагувати", e -> editClient()),
                button("Видалити", e -> deleteClient())));
        tabs.addTab("Поліси", createTab(policyTable, this::editPolicy,
                button("Оформити поліс", e -> addPolicy()),
                button("Редагувати", e -> editPolicy()),
                button("Видалити", e -> deletePolicy())));
        tabs.addTab("Запити", queryPanel);
        tabs.addChangeListener(e -> queryPanel.refresh());

        JPanel status = new JPanel(new BorderLayout());
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        status.add(statusLabel, BorderLayout.CENTER);
        status.add(countsLabel, BorderLayout.EAST);

        add(tabs, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        setSize(1060, 600);
        setLocationRelativeTo(null);
        refreshAll();
    }

    /** Збільшений шрифт і українські підписи стандартних кнопок Swing. */
    public static void applyUiFont() {
        for (Object key : UIManager.getDefaults().keySet().toArray()) {
            if (UIManager.get(key) instanceof FontUIResource f) {
                UIManager.put(key,
                        new FontUIResource(f.getFamily(), f.getStyle(), 15));
            }
        }
        UIManager.put("OptionPane.yesButtonText", "Так");
        UIManager.put("OptionPane.noButtonText", "Ні");
        UIManager.put("OptionPane.cancelButtonText", "Скасувати");
        UIManager.put("FileChooser.openButtonText", "Обрати");
        UIManager.put("FileChooser.saveButtonText", "Обрати");
        UIManager.put("FileChooser.cancelButtonText", "Скасувати");
    }

    // ---------- Побудова інтерфейсу ----------

    private JMenuBar createMenu() {
        JMenu file = new JMenu("Файл");
        file.add(menuItem("Завантажити дані з файлу...", this::loadData));
        file.add(menuItem("Зберегти дані у файл", this::saveData));
        file.add(menuItem("Зберегти дані як...", this::saveDataAs));
        file.addSeparator();
        file.add(menuItem("Вихід", this::exit));
        JMenu help = new JMenu("Довідка");
        help.add(menuItem("Про програму", this::about));
        JMenuBar bar = new JMenuBar();
        bar.add(file);
        bar.add(help);
        return bar;
    }

    private static JMenuItem menuItem(String text, Runnable action) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(e -> action.run());
        return item;
    }

    private static JButton button(String text, ActionListener action) {
        JButton b = new JButton(text);
        b.addActionListener(action);
        return b;
    }

    /** Таблиця з сортуванням за стовпцями; widths — бажані ширини стовпців. */
    private static JTable createTable(EntityTableModel<?> model, int... widths) {
        JTable table = new JTable(model);
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);           // сортування за стовпцем
        table.setRowHeight(table.getRowHeight() + 6);
        table.setFillsViewportHeight(true);
        return table;
    }

    /** Вкладка: таблиця і панель кнопок; подвійне клацання — редагування. */
    private JPanel createTab(JTable table, Runnable onDoubleClick,
                             JButton... buttons) {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    onDoubleClick.run();
                }
            }
        });
        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (JButton b : buttons) {
            buttonBar.add(b);
        }
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buttonBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // ---------- Оновлення відображення ----------

    private void refreshAll() {
        agentModel.setItems(company.getAgents().getAll());
        clientModel.setItems(company.getClients().getAll());
        policyModel.setItems(company.getPolicies().getAll());
        queryPanel.refresh();
        countsLabel.setText("Агентів: " + company.getAgents().size()
                + "   Клієнтів: " + company.getClients().size()
                + "   Полісів: " + company.getPolicies().size());
        setTitle("Страхова компанія «" + company.getName() + "»"
                + (modified ? " — є незбережені зміни" : ""));
    }

    private void changed(String message) {
        modified = true;
        refreshAll();
        showStatus(message);
    }

    public void showStatus(String message) {
        statusLabel.setText(message);
    }

    public JTabbedPane getTabs() {
        return tabs;
    }

    public QueryPanel getQueryPanel() {
        return queryPanel;
    }

    /** Об'єкт, вибраний у таблиці (з урахуванням сортування), або null. */
    private <T> T selected(JTable table, EntityTableModel<T> model) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Оберіть рядок у таблиці.",
                    "Немає вибраного запису", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return model.getItem(table.convertRowIndexToModel(row));
    }

    private <T> void select(JTable table, EntityTableModel<T> model, T item) {
        int row = model.indexOf(item);
        if (row >= 0) {
            int view = table.convertRowIndexToView(row);
            table.setRowSelectionInterval(view, view);
            table.scrollRectToVisible(table.getCellRect(view, 0, true));
        }
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title,
                JOptionPane.ERROR_MESSAGE);
        showStatus("Помилка: " + message);
    }

    // ---------- Агенти ----------

    private void addAgent() {
        AgentDialog dialog = new AgentDialog(this, company, null);
        if (dialog.showDialog()) {
            changed("Додано агента " + dialog.getResult().getFullName());
            select(agentTable, agentModel, dialog.getResult());
        }
    }

    private void editAgent() {
        Agent agent = selected(agentTable, agentModel);
        if (agent != null && new AgentDialog(this, company, agent).showDialog()) {
            changed("Змінено дані агента " + agent.getFullName());
            select(agentTable, agentModel, agent);
        }
    }

    private void deleteAgent() {
        Agent agent = selected(agentTable, agentModel);
        if (agent == null
                || !confirm("Видалити агента " + agent.getFullName() + "?")) {
            return;
        }
        try {
            company.removeAgent(agent.getId());
            changed("Видалено агента " + agent.getFullName());
        } catch (InvalidDataException e) {
            showError("Видалення неможливе", e.getMessage());
        }
    }

    // ---------- Клієнти ----------

    private void addClient() {
        if (company.getAgents().size() == 0) {
            showError("Немає агентів", "Спершу додайте хоча б одного агента.");
            return;
        }
        ClientDialog dialog = new ClientDialog(this, company, null);
        if (dialog.showDialog()) {
            changed("Додано клієнта " + dialog.getResult().getFullName());
            select(clientTable, clientModel, dialog.getResult());
        }
    }

    private void editClient() {
        Client client = selected(clientTable, clientModel);
        if (client != null
                && new ClientDialog(this, company, client).showDialog()) {
            changed("Змінено дані клієнта " + client.getFullName());
            select(clientTable, clientModel, client);
        }
    }

    private void deleteClient() {
        Client client = selected(clientTable, clientModel);
        if (client == null) {
            return;
        }
        int policies = company.getPoliciesOf(client).size();
        if (confirm("Видалити клієнта " + client.getFullName() + "?\n"
                + "Разом з ним буде видалено полісів: " + policies)) {
            company.removeClient(client.getId());
            changed("Видалено клієнта " + client.getFullName()
                    + " і полісів: " + policies);
        }
    }

    // ---------- Поліси ----------

    private void addPolicy() {
        if (company.getClients().size() == 0) {
            showError("Немає клієнтів", "Спершу додайте хоча б одного клієнта.");
            return;
        }
        PolicyDialog dialog = new PolicyDialog(this, company, null);
        if (dialog.showDialog()) {
            changed("Оформлено поліс " + dialog.getResult().getPolicyNumber());
            select(policyTable, policyModel, dialog.getResult());
        }
    }

    private void editPolicy() {
        Policy policy = selected(policyTable, policyModel);
        if (policy != null
                && new PolicyDialog(this, company, policy).showDialog()) {
            changed("Змінено поліс " + policy.getPolicyNumber());
            select(policyTable, policyModel, policy);
        }
    }

    private void deletePolicy() {
        Policy policy = selected(policyTable, policyModel);
        if (policy != null
                && confirm("Видалити поліс " + policy.getPolicyNumber() + "?")) {
            company.removePolicy(policy.getId());
            changed("Видалено поліс " + policy.getPolicyNumber());
        }
    }

    private boolean confirm(String question) {
        return JOptionPane.showConfirmDialog(this, question, "Підтвердження",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    // ---------- Файли ----------

    private boolean saveData() {
        try {
            CsvStorage.save(company, dataDir);
            modified = false;
            refreshAll();
            showStatus("Дані збережено в каталог " + dataDir.toAbsolutePath());
            return true;
        } catch (IOException e) {
            showError("Помилка збереження", "Не вдалося записати файли: "
                    + e.getMessage());
            return false;
        }
    }

    private void saveDataAs() {
        Path dir = chooseDirectory("Каталог для збереження даних");
        if (dir != null) {
            dataDir = dir;
            saveData();
        }
    }

    private void loadData() {
        if (modified && !confirm("Є незбережені зміни. Завантажити дані з файлу"
                + " і втратити їх?")) {
            return;
        }
        Path dir = chooseDirectory("Каталог з файлами даних");
        if (dir == null) {
            return;
        }
        if (!CsvStorage.hasData(dir)) {
            showError("Немає даних", "У каталозі немає файлів "
                    + CsvStorage.AGENTS_FILE + ", " + CsvStorage.CLIENTS_FILE
                    + ", " + CsvStorage.POLICIES_FILE);
            return;
        }
        try {
            company = CsvStorage.load(dir, company.getName());
            dataDir = dir;
            modified = false;
            refreshAll();
            showStatus("Дані завантажено з каталогу " + dir.toAbsolutePath());
        } catch (IOException | DataFileException e) {
            showError("Помилка читання даних", e.getMessage());
        }
    }

    private Path chooseDirectory(String title) {
        JFileChooser chooser = new JFileChooser(dataDir.toAbsolutePath().toFile());
        chooser.setDialogTitle(title);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().toPath();
        }
        return null;
    }

    private void exit() {
        if (modified) {
            int answer = JOptionPane.showConfirmDialog(this,
                    "Зберегти зміни перед виходом?", "Вихід",
                    JOptionPane.YES_NO_CANCEL_OPTION);
            if (answer == JOptionPane.CANCEL_OPTION
                    || answer == JOptionPane.CLOSED_OPTION
                    || (answer == JOptionPane.YES_OPTION && !saveData())) {
                return;
            }
        }
        dispose();
        System.exit(0);
    }

    private void about() {
        JOptionPane.showMessageDialog(this,
                "Програма «Страхова компанія»\n"
                        + "Курсова робота з дисципліни «ООП на Java», варіант 26\n"
                        + "Виконав: Троцький К. А., гр. АС-253",
                "Про програму", JOptionPane.INFORMATION_MESSAGE);
    }
}
