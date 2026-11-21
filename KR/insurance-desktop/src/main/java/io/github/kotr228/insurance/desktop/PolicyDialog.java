package io.github.kotr228.insurance.desktop;

import io.github.kotr228.insurance.model.Client;
import io.github.kotr228.insurance.model.InsurancePeriod;
import io.github.kotr228.insurance.model.InsuranceType;
import io.github.kotr228.insurance.model.Policy;
import io.github.kotr228.insurance.service.InsuranceCompany;
import io.github.kotr228.platform.core.model.InvalidDataException;
import io.github.kotr228.platform.swing.EntityDialog;
import io.github.kotr228.platform.swing.Formats;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Window;
import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Вікно оформлення та редагування страхового поліса. */
public final class PolicyDialog extends EntityDialog {
    @Serial
    private static final long serialVersionUID = 1L;

    private final transient InsuranceCompany company;
    private final transient Policy policy;          // null — новий поліс
    private final JComboBox<Client> clientBox = new JComboBox<>();
    private final JComboBox<InsuranceType> typeBox =
            new JComboBox<>(InsuranceType.values());
    private final JTextField startField = new JTextField(10);
    private final JTextField endField = new JTextField(10);
    private final JTextField eventField = new JTextField(28);
    private final JTextField costField = new JTextField(10);
    private transient Policy result;

    @SuppressWarnings("this-escape")      // форма будується в конструкторі
    public PolicyDialog(Window owner, InsuranceCompany company, Policy policy) {
        super(owner, policy == null ? "Оформлення поліса"
                : "Редагування поліса " + policy.getPolicyNumber());
        this.company = company;
        this.policy = policy;
        for (Client c : company.getClientList()) {
            clientBox.addItem(c);
        }
        // у списку показуємо повне ПІБ, а не лише ініціали
        clientBox.setRenderer(new DefaultListCellRenderer() {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> list,
                    Object value, int index, boolean selected, boolean focus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, selected, focus);
                if (value instanceof Client c) {
                    label.setText(c.getFullName().toString());
                }
                return label;
            }
        });
        addField("Клієнт", clientBox);
        addField("Тип страхування", typeBox);
        addField("Дата початку", startField);
        addField("Дата завершення", endField);
        addField("Страховий випадок", eventField);
        addField("Вартість, грн", costField);
        startField.setToolTipText("ДД.ММ.РРРР");
        endField.setToolTipText("ДД.ММ.РРРР");
        costField.setToolTipText("Наприклад: 4600 або 4 600,50");
        if (policy != null) {
            clientBox.setSelectedItem(policy.getClient());
            typeBox.setSelectedItem(policy.getInsuranceType());
            startField.setText(Formats.date(policy.getPeriod().getStartDate()));
            endField.setText(Formats.date(policy.getPeriod().getEndDate()));
            eventField.setText(policy.getInsuredEvent());
            costField.setText(Formats.moneyForInput(policy.getCost()));
        } else {
            LocalDate today = LocalDate.now();
            startField.setText(Formats.date(today));
            endField.setText(Formats.date(today.plusYears(1).minusDays(1)));
        }
    }

    @Override
    protected void save() {
        Client client = (Client) clientBox.getSelectedItem();
        if (client == null) {
            throw new InvalidDataException("Оберіть клієнта");
        }
        InsuranceType type = (InsuranceType) typeBox.getSelectedItem();
        InsurancePeriod period = new InsurancePeriod(
                Formats.parseDate(startField.getText(), "Дата початку"),
                Formats.parseDate(endField.getText(), "Дата завершення"));
        BigDecimal cost = Formats.parseMoney(costField.getText(), "Вартість");
        result = policy == null
                ? company.createPolicy(type, period, eventField.getText(), cost,
                        client.getId())
                : company.updatePolicy(policy.getId(), type, period,
                        eventField.getText(), cost, client.getId());
    }

    public Policy getResult() {
        return result;
    }
}
