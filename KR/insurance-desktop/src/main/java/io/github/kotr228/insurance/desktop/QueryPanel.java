package io.github.kotr228.insurance.desktop;

import io.github.kotr228.insurance.model.Agent;
import io.github.kotr228.insurance.model.Client;
import io.github.kotr228.insurance.model.InsuranceType;
import io.github.kotr228.insurance.model.Policy;
import io.github.kotr228.insurance.service.InsuranceCompany;
import io.github.kotr228.platform.swing.Formats;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.io.Serial;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/** Вкладка «Запити»: список шести запитів варіанта та результат вибраного. */
public final class QueryPanel extends JPanel {
    @Serial
    private static final long serialVersionUID = 1L;

    static final String[] QUERIES = {
            "1. Список клієнтів",
            "2. Кількість полісів",
            "3. Найдорожчий поліс",
            "4. Середня вартість страхування",
            "5. Агент із найбільшою кількістю клієнтів",
            "6. Тип страхування з найбільшим попитом"};

    private final transient Supplier<InsuranceCompany> company;
    private final JList<String> queryList = new JList<>(QUERIES);
    private final JLabel summary = new JLabel(" ");
    private final DefaultTableModel resultModel = new DefaultTableModel() {
        @Serial
    private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable resultTable = new JTable(resultModel);

    @SuppressWarnings("this-escape")      // панель будується в конструкторі
    public QueryPanel(Supplier<InsuranceCompany> company) {
        super(new BorderLayout(8, 8));
        this.company = company;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        queryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        queryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                refresh();
            }
        });
        JScrollPane listScroll = new JScrollPane(queryList);
        listScroll.setBorder(BorderFactory.createTitledBorder("Запит"));
        listScroll.setPreferredSize(new Dimension(380, 0));

        summary.setFont(summary.getFont().deriveFont(Font.BOLD));
        summary.setBorder(BorderFactory.createEmptyBorder(4, 4, 6, 4));
        resultTable.setRowHeight(
                resultTable.getFontMetrics(resultTable.getFont()).getHeight() + 10);
        resultTable.setShowVerticalLines(false);
        resultTable.setFillsViewportHeight(true);
        resultTable.getTableHeader().setFont(
                resultTable.getTableHeader().getFont().deriveFont(Font.BOLD));
        JPanel result = new JPanel(new BorderLayout(4, 4));
        result.setBorder(BorderFactory.createTitledBorder("Результат"));
        result.add(summary, BorderLayout.NORTH);
        result.add(new JScrollPane(resultTable), BorderLayout.CENTER);

        add(listScroll, BorderLayout.WEST);
        add(result, BorderLayout.CENTER);
        queryList.setSelectedIndex(0);
    }

    public void select(int index) {
        queryList.setSelectedIndex(index);
    }

    /** Повторно виконує вибраний запит (викликається після зміни даних). */
    public void refresh() {
        int index = queryList.getSelectedIndex();
        InsuranceCompany c = company.get();
        if (c == null) {
            return;        // дані ще не передано (під час створення вікна)
        }
        resultModel.setRowCount(0);
        switch (index) {
            case 0 -> clientList(c);
            case 1 -> policyCount(c);
            case 2 -> mostExpensive(c);
            case 3 -> averageCost(c);
            case 4 -> topAgent(c);
            case 5 -> popularType(c);
            default -> summary.setText("Оберіть запит у списку ліворуч");
        }
    }

    private void columns(String... names) {
        resultModel.setColumnIdentifiers(names);
    }

    private void clientList(InsuranceCompany c) {
        columns("ПІБ", "Телефон", "Агент");
        for (Client client : c.getClientList()) {
            resultModel.addRow(new Object[]{client.getFullName(),
                    client.getPhone().getFormatted(), client.getAgent()});
        }
        summary.setText("Клієнтів: " + c.getClientCount());
    }

    private void policyCount(InsuranceCompany c) {
        columns("Номер поліса", "Тип страхування", "Клієнт");
        for (Policy p : c.getPolicies()) {
            resultModel.addRow(new Object[]{p.getPolicyNumber(),
                    p.getInsuranceType(), p.getClient()});
        }
        summary.setText("Кількість полісів: " + c.getPolicyCount());
    }

    private void mostExpensive(InsuranceCompany c) {
        columns("Реквізит", "Значення");
        Optional<Policy> found = c.getMostExpensivePolicy();
        if (found.isEmpty()) {
            summary.setText("Результат не визначений: полісів немає");
            return;
        }
        Policy p = found.get();
        resultModel.addRow(new Object[]{"Номер поліса", p.getPolicyNumber()});
        resultModel.addRow(new Object[]{"Тип страхування", p.getInsuranceType()});
        resultModel.addRow(new Object[]{"Клієнт", p.getClient().getFullName()});
        resultModel.addRow(new Object[]{"Агент", p.getClient().getAgent()});
        resultModel.addRow(new Object[]{"Період дії", p.getPeriod()});
        resultModel.addRow(new Object[]{"Страховий випадок", p.getInsuredEvent()});
        resultModel.addRow(new Object[]{"Вартість, грн",
                Formats.money(p.getCost())});
        summary.setText("Найдорожчий поліс: " + p.getPolicyNumber() + " — "
                + Formats.money(p.getCost()) + " грн");
    }

    private void averageCost(InsuranceCompany c) {
        columns("Номер поліса", "Вартість, грн");
        for (Policy p : c.getPolicies()) {
            resultModel.addRow(new Object[]{p.getPolicyNumber(),
                    Formats.money(p.getCost())});
        }
        summary.setText(c.getPolicyCount() == 0
                ? "Результат не визначений: полісів немає"
                : "Середня вартість страхування: "
                        + Formats.money(c.getAverageCost()) + " грн");
    }

    private void topAgent(InsuranceCompany c) {
        columns("Агент", "Кількість клієнтів");
        Map<Agent, Integer> counts = c.countClientsByAgent();
        for (Map.Entry<Agent, Integer> e : counts.entrySet()) {
            resultModel.addRow(
                    new Object[]{e.getKey().getFullName(), e.getValue()});
        }
        Optional<Agent> top = c.getAgentWithMostClients();
        if (top.isEmpty()) {
            summary.setText("Результат не визначений: агентів немає");
            return;
        }
        summary.setText("Агент із найбільшою кількістю клієнтів: "
                + top.get().getFullName() + " (" + counts.get(top.get()) + ")");
        int row = 0;
        for (Agent a : counts.keySet()) {
            if (a == top.get()) {
                resultTable.setRowSelectionInterval(row, row);   // виділення
            }
            row++;
        }
    }

    private void popularType(InsuranceCompany c) {
        columns("Тип страхування", "Кількість полісів");
        Map<InsuranceType, Integer> counts = c.countPoliciesByType();
        for (Map.Entry<InsuranceType, Integer> e : counts.entrySet()) {
            resultModel.addRow(new Object[]{e.getKey(), e.getValue()});
        }
        Optional<InsuranceType> top = c.getMostPopularType();
        if (top.isEmpty()) {
            summary.setText("Результат не визначений: полісів немає");
            return;
        }
        summary.setText("Найбільший попит: " + top.get() + " ("
                + counts.get(top.get()) + " пол.)");
        int row = top.get().ordinal();
        resultTable.setRowSelectionInterval(row, row);
    }
}
