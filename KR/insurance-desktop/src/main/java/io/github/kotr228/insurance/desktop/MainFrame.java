package io.github.kotr228.insurance.desktop;

import io.github.kotr228.insurance.model.Agent;
import io.github.kotr228.insurance.model.Client;
import io.github.kotr228.insurance.model.Policy;
import io.github.kotr228.insurance.model.PolicyStatus;
import io.github.kotr228.insurance.service.InsuranceCompany;
import io.github.kotr228.platform.core.model.InvalidDataException;
import io.github.kotr228.platform.core.storage.DataFileException;
import io.github.kotr228.platform.core.storage.DataStore;
import io.github.kotr228.platform.swing.EntityTableModel;
import io.github.kotr228.platform.swing.EntityTableModel.Column;
import io.github.kotr228.platform.swing.EntityTablePanel;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.Serial;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Головне вікно програми: вкладки «Агенти», «Клієнти», «Поліси», «Запити». */
public final class MainFrame extends JFrame {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(MainFrame.class.getName());

    private transient InsuranceCompany company;
    private transient DataStore<InsuranceCompany> store;
    private final transient Function<Path, DataStore<InsuranceCompany>> storeFactory;
    /** Кількість клієнтів агентів; перераховується лише після змін даних. */
    private transient Map<Agent, Integer> clientCounts = Map.of();
    private transient Path lastDirectory;
    private boolean modified;

    private final EntityTableModel<Agent> agentModel = new EntityTableModel<>(List.of(
            new Column<>("Id", Integer.class, Agent::getId),
            new Column<>("ПІБ", String.class, a -> a.getFullName().toString()),
            new Column<>("Телефон", String.class, a -> a.getPhone().getFormatted()),
            new Column<>("Стаж, р.", Integer.class, Agent::getExperienceYears),
            new Column<>("Клієнтів", Integer.class,
                    a -> clientCounts.getOrDefault(a, 0))));
    private final EntityTableModel<Client> clientModel = new EntityTableModel<>(List.of(
            new Column<>("Id", Integer.class, Client::getId),
            new Column<>("ПІБ", String.class, c -> c.getFullName().toString()),
            new Column<>("Телефон", String.class, c -> c.getPhone().getFormatted()),
            new Column<>("Дата народження", LocalDate.class, Client::getBirthDate),
            new Column<>("Вік", Integer.class, Client::getAge),
            new Column<>("Агент", String.class, c -> c.getAgent().toString())));
    private final EntityTableModel<Policy> policyModel = new EntityTableModel<>(List.of(
            new Column<>("Номер", String.class, Policy::getPolicyNumber),
            new Column<>("Тип", String.class, p -> p.getInsuranceType().toString()),
            new Column<>("Клієнт", String.class, p -> p.getClient().toString()),
            new Column<>("Початок", LocalDate.class, p -> p.getPeriod().getStartDate()),
            new Column<>("Завершення", LocalDate.class, p -> p.getPeriod().getEndDate()),
            new Column<>("Страховий випадок", String.class, Policy::getInsuredEvent),
            new Column<>("Вартість, грн", BigDecimal.class, Policy::getCost),
            new Column<>("Статус", PolicyStatus.class,
                    p -> p.getStatusOn(LocalDate.now()))));

    private final EntityTablePanel<Agent> agentPanel;
    private final EntityTablePanel<Client> clientPanel;
    private final EntityTablePanel<Policy> policyPanel;
    private final QueryPanel queryPanel = new QueryPanel(() -> company);
    private final JTabbedPane tabs = new JTabbedPane();
    private final JLabel statusLabel = new JLabel(" ");
    private final JLabel countsLabel = new JLabel(" ");

    @SuppressWarnings("this-escape")      // вікно будується в конструкторі
    public MainFrame(InsuranceCompany company, DataStore<InsuranceCompany> store,
                     Function<Path, DataStore<InsuranceCompany>> storeFactory) {
        this.company = company;
        this.store = store;
        this.storeFactory = storeFactory;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });

        agentPanel = new EntityTablePanel<>(agentModel, "Додати",
                this::addAgent, this::editAgent, this::deleteAgent,
                50, 330, 200, 90, 90);
        clientPanel = new EntityTablePanel<>(clientModel, "Додати",
                this::addClient, this::editClient, this::deleteClient,
                50, 290, 190, 150, 60, 170);
        policyPanel = new EntityTablePanel<>(policyModel, "Оформити поліс",
                this::addPolicy, this::editPolicy, this::deletePolicy,
                120, 170, 140, 100, 110, 210, 120, 100);
        policyPanel.setColumnRenderer(7, new StatusRenderer());

        setJMenuBar(createMenu());
        tabs.addTab("Агенти", agentPanel);
        tabs.addTab("Клієнти", clientPanel);
        tabs.addTab("Поліси", policyPanel);
        tabs.addTab("Запити", queryPanel);
        tabs.setToolTipTextAt(0, "Страхові агенти компанії");
        tabs.setToolTipTextAt(1, "Клієнти та їхні агенти");
        tabs.setToolTipTextAt(2, "Страхові поліси");
        tabs.setToolTipTextAt(3, "Шість запитів за варіантом 26");
        tabs.addChangeListener(e -> queryPanel.refresh());

        JPanel status = new JPanel(new BorderLayout());
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        status.add(statusLabel, BorderLayout.CENTER);
        status.add(countsLabel, BorderLayout.EAST);

        add(tabs, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        setSize(1180, 640);
        setLocationRelativeTo(null);
        refreshAll();
    }

    // ---------- Меню ----------

    private JMenuBar createMenu() {
        int ctrl = InputEvent.CTRL_DOWN_MASK;
        JMenu file = new JMenu("Файл");
        file.add(menuItem("Завантажити дані з каталогу...", this::loadData,
                KeyStroke.getKeyStroke(KeyEvent.VK_O, ctrl)));
        file.add(menuItem("Зберегти дані", this::saveData,
                KeyStroke.getKeyStroke(KeyEvent.VK_S, ctrl)));
        file.add(menuItem("Зберегти дані як...", this::saveDataAs,
                KeyStroke.getKeyStroke(KeyEvent.VK_S, ctrl | InputEvent.SHIFT_DOWN_MASK)));
        file.addSeparator();
        file.add(menuItem("Вихід", this::exit, KeyStroke.getKeyStroke(KeyEvent.VK_Q, ctrl)));
        JMenu help = new JMenu("Довідка");
        help.add(menuItem("Гарячі клавіші", this::shortcuts,
                KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0)));
        help.add(menuItem("Про програму", this::about, null));
        JMenuBar bar = new JMenuBar();
        bar.add(file);
        bar.add(help);
        return bar;
    }

    private static JMenuItem menuItem(String text, Runnable action, KeyStroke key) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(e -> action.run());
        item.setAccelerator(key);
        return item;
    }

    // ---------- Оновлення відображення ----------

    private void refreshAll() {
        clientCounts = company.countClientsByAgent();
        agentModel.setItems(company.getAgents());
        clientModel.setItems(company.getClients());
        policyModel.setItems(company.getPolicies());
        queryPanel.refresh();
        countsLabel.setText("Агентів: " + company.getAgentCount()
                + "   Клієнтів: " + company.getClientCount()
                + "   Полісів: " + company.getPolicyCount());
        setTitle("Страхова компанія «" + company.getName() + "»"
                + (modified ? " — є незбережені зміни" : ""));
    }

    private void changed(String message) {
        modified = true;
        refreshAll();
        showStatus(message);
        LOG.info(message);
    }

    public void showStatus(String message) {
        statusLabel.setText(message);
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
        showStatus("Помилка: " + message);
    }

    private boolean confirm(String question) {
        return JOptionPane.showConfirmDialog(this, question, "Підтвердження",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    // ---------- Агенти ----------

    private void addAgent() {
        AgentDialog dialog = new AgentDialog(this, company, null);
        if (dialog.showDialog()) {
            changed("Додано агента " + dialog.getResult().getFullName());
            agentPanel.select(dialog.getResult());
        }
    }

    private void editAgent(Agent agent) {
        if (new AgentDialog(this, company, agent).showDialog()) {
            changed("Змінено дані агента " + agent.getFullName());
            agentPanel.select(agent);
        }
    }

    private void deleteAgent(Agent agent) {
        if (!confirm("Видалити агента " + agent.getFullName() + "?")) {
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
        if (company.getAgentCount() == 0) {
            showError("Немає агентів", "Спершу додайте хоча б одного агента.");
            return;
        }
        ClientDialog dialog = new ClientDialog(this, company, null);
        if (dialog.showDialog()) {
            changed("Додано клієнта " + dialog.getResult().getFullName());
            clientPanel.select(dialog.getResult());
        }
    }

    private void editClient(Client client) {
        if (new ClientDialog(this, company, client).showDialog()) {
            changed("Змінено дані клієнта " + client.getFullName());
            clientPanel.select(client);
        }
    }

    private void deleteClient(Client client) {
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
        if (company.getClientCount() == 0) {
            showError("Немає клієнтів", "Спершу додайте хоча б одного клієнта.");
            return;
        }
        PolicyDialog dialog = new PolicyDialog(this, company, null);
        if (dialog.showDialog()) {
            changed("Оформлено поліс " + dialog.getResult().getPolicyNumber());
            policyPanel.select(dialog.getResult());
        }
    }

    private void editPolicy(Policy policy) {
        if (new PolicyDialog(this, company, policy).showDialog()) {
            changed("Змінено поліс " + policy.getPolicyNumber());
            policyPanel.select(policy);
        }
    }

    private void deletePolicy(Policy policy) {
        if (confirm("Видалити поліс " + policy.getPolicyNumber() + "?")) {
            company.removePolicy(policy.getId());
            changed("Видалено поліс " + policy.getPolicyNumber());
        }
    }

    // ---------- Дані ----------

    private boolean saveData() {
        try {
            store.save(company);
            modified = false;
            refreshAll();
            showStatus("Дані збережено: " + store.location());
            return true;
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Не вдалося зберегти дані", e);
            showError("Помилка збереження",
                    "Не вдалося записати дані: " + e.getMessage());
            return false;
        }
    }

    private void saveDataAs() {
        Path dir = chooseDirectory("Каталог для збереження даних");
        if (dir != null) {
            store = storeFactory.apply(dir);
            saveData();
        }
    }

    private void loadData() {
        if (modified && !confirm("Є незбережені зміни. Завантажити інші дані"
                + " і втратити їх?")) {
            return;
        }
        Path dir = chooseDirectory("Каталог з файлами даних");
        if (dir == null) {
            return;
        }
        DataStore<InsuranceCompany> candidate = storeFactory.apply(dir);
        if (!candidate.exists()) {
            showError("Немає даних", "У каталозі " + candidate.location()
                    + " немає збережених даних програми.");
            return;
        }
        try {
            company = candidate.load();
            store = candidate;
            modified = false;
            refreshAll();
            showStatus("Дані завантажено: " + store.location());
        } catch (IOException | DataFileException e) {
            LOG.log(Level.WARNING, "Не вдалося прочитати дані", e);
            showError("Помилка читання даних", e.getMessage());
        }
    }

    private Path chooseDirectory(String title) {
        JFileChooser chooser = new JFileChooser(
                lastDirectory != null ? lastDirectory.toFile() : null);
        chooser.setDialogTitle(title);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            lastDirectory = chooser.getSelectedFile().toPath();
            return lastDirectory;
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
        LOG.info("Завершення роботи програми");
        dispose();
        System.exit(0);
    }

    // ---------- Довідка ----------

    private void shortcuts() {
        JOptionPane.showMessageDialog(this,
                """
                Insert — додати запис
                Enter або подвійне клацання — редагувати
                Delete — видалити вибраний запис
                Esc у полі пошуку — очистити пошук
                Ctrl+O — завантажити дані
                Ctrl+S — зберегти дані
                Ctrl+Shift+S — зберегти дані як...
                Ctrl+Q — вихід""",
                "Гарячі клавіші", JOptionPane.INFORMATION_MESSAGE);
    }

    private void about() {
        JOptionPane.showMessageDialog(this,
                "Програма «Страхова компанія»\n"
                        + "Курсова робота з дисципліни «ООП на Java», варіант 26\n"
                        + "Виконав: Троцький К. А., гр. АС-253\n\n"
                        + "Каталог даних: " + store.location(),
                "Про програму", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Кольоровий статус поліса: діє — зелений, ще не діє — синій, завершено — сірий. */
    private static final class StatusRenderer extends DefaultTableCellRenderer {
        @Serial
        private static final long serialVersionUID = 1L;

        StatusRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focus, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, focus,
                    row, column);
            if (!selected && value instanceof PolicyStatus status) {
                setForeground(switch (status) {
                    case ACTIVE -> new Color(0, 128, 0);
                    case PENDING -> new Color(0, 90, 180);
                    case EXPIRED -> Color.GRAY;
                });
            }
            setFont(getFont().deriveFont(Font.BOLD));
            return this;
        }
    }
}
