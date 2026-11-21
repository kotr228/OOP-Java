package insurance.gui;

import insurance.model.Client;
import insurance.model.InsurancePeriod;
import insurance.model.InsuranceType;
import insurance.model.InvalidDataException;
import insurance.model.Policy;
import insurance.service.InsuranceCompany;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import java.awt.Component;
import java.time.LocalDate;

/** Вікно оформлення та редагування страхового поліса. */
public final class PolicyDialog extends EntityDialog {
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

    public PolicyDialog(JFrame owner, InsuranceCompany company, Policy policy) {
        super(owner, policy == null ? "Оформлення поліса"
                : "Редагування поліса " + policy.getPolicyNumber());
        this.company = company;
        this.policy = policy;
        for (Client c : company.getClientList()) {
            clientBox.addItem(c);
        }
        // у списку показуємо повне ПІБ, а не лише ініціали
        clientBox.setRenderer(new DefaultListCellRenderer() {
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
        if (policy != null) {
            clientBox.setSelectedItem(policy.getClient());
            typeBox.setSelectedItem(policy.getInsuranceType());
            startField.setText(Formats.date(policy.getPeriod().getStartDate()));
            endField.setText(Formats.date(policy.getPeriod().getEndDate()));
            eventField.setText(policy.getInsuredEvent());
            costField.setText(String.valueOf(policy.getCost()));
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
        double cost = Formats.parseMoney(costField.getText(), "Вартість");
        if (policy == null) {
            result = new Policy(type, period, eventField.getText(), cost, client);
            company.addPolicy(result);
        } else {
            result = company.updatePolicy(policy.getId(), type, period,
                    eventField.getText(), cost, client.getId());
        }
    }

    public Policy getResult() {
        return result;
    }
}
