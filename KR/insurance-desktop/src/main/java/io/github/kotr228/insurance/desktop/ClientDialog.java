package io.github.kotr228.insurance.desktop;

import io.github.kotr228.insurance.model.Agent;
import io.github.kotr228.insurance.model.Client;
import io.github.kotr228.insurance.service.InsuranceCompany;
import io.github.kotr228.platform.core.model.FullName;
import io.github.kotr228.platform.core.model.InvalidDataException;
import io.github.kotr228.platform.core.model.PhoneNumber;
import io.github.kotr228.platform.swing.EntityDialog;
import io.github.kotr228.platform.swing.Formats;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import java.awt.Window;
import java.io.Serial;
import java.time.LocalDate;

/** Вікно додавання та редагування клієнта. */
public final class ClientDialog extends EntityDialog {
    @Serial
    private static final long serialVersionUID = 1L;

    private final transient InsuranceCompany company;
    private final transient Client client;          // null — новий клієнт
    private final JTextField nameField = new JTextField(26);
    private final JTextField phoneField = new JTextField(16);
    private final JTextField birthField = new JTextField(10);
    private final JComboBox<Agent> agentBox = new JComboBox<>();
    private transient Client result;

    @SuppressWarnings("this-escape")      // форма будується в конструкторі
    public ClientDialog(Window owner, InsuranceCompany company, Client client) {
        super(owner, client == null ? "Новий клієнт" : "Редагування клієнта");
        this.company = company;
        this.client = client;
        for (Agent a : company.getAgents()) {
            agentBox.addItem(a);
        }
        addField("ПІБ", nameField);
        addField("Телефон", phoneField);
        addField("Дата народження", birthField);
        addField("Агент", agentBox);
        phoneField.setToolTipText("+380XXXXXXXXX; пробіли, дужки й дефіси допускаються");
        birthField.setToolTipText("ДД.ММ.РРРР");
        if (client != null) {
            nameField.setText(client.getFullName().toString());
            phoneField.setText(client.getPhone().getNumber());
            birthField.setText(Formats.date(client.getBirthDate()));
            agentBox.setSelectedItem(client.getAgent());
        } else {
            phoneField.setText("+380");
        }
    }

    @Override
    protected void save() {
        FullName name = FullName.parse(nameField.getText());
        PhoneNumber phone = new PhoneNumber(phoneField.getText());
        LocalDate birth = Formats.parseDate(birthField.getText(), "Дата народження");
        Agent agent = (Agent) agentBox.getSelectedItem();
        if (agent == null) {
            throw new InvalidDataException("Оберіть агента");
        }
        result = client == null
                ? company.createClient(name, phone, birth, agent.getId())
                : company.updateClient(client.getId(), name, phone, birth,
                        agent.getId());
    }

    public Client getResult() {
        return result;
    }
}
