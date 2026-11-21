package insurance.gui;

import insurance.model.Agent;
import insurance.model.Client;
import insurance.model.FullName;
import insurance.model.InvalidDataException;
import insurance.model.PhoneNumber;
import insurance.service.InsuranceCompany;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;
import java.time.LocalDate;

/** Вікно додавання та редагування клієнта. */
public final class ClientDialog extends EntityDialog {
    private static final long serialVersionUID = 1L;

    private final transient InsuranceCompany company;
    private final transient Client client;          // null — новий клієнт
    private final JTextField nameField = new JTextField(26);
    private final JTextField phoneField = new JTextField(16);
    private final JTextField birthField = new JTextField(10);
    private final JComboBox<Agent> agentBox = new JComboBox<>();
    private transient Client result;

    public ClientDialog(JFrame owner, InsuranceCompany company, Client client) {
        super(owner, client == null ? "Новий клієнт" : "Редагування клієнта");
        this.company = company;
        this.client = client;
        for (Agent a : company.getAgents().getAll()) {
            agentBox.addItem(a);
        }
        addField("ПІБ", nameField);
        addField("Телефон", phoneField);
        addField("Дата народження", birthField);
        addField("Агент", agentBox);
        if (client != null) {
            nameField.setText(client.getFullName().toString());
            phoneField.setText(client.getPhone().getNumber());
            birthField.setText(Formats.date(client.getBirthDate()));
            agentBox.setSelectedItem(client.getAgent());
        } else {
            phoneField.setText("+380");
            birthField.setToolTipText("ДД.ММ.РРРР");
        }
    }

    @Override
    protected void save() {
        FullName name = FullName.parse(nameField.getText());
        PhoneNumber phone = new PhoneNumber(phoneField.getText());
        LocalDate birth =
                Formats.parseDate(birthField.getText(), "Дата народження");
        Agent agent = (Agent) agentBox.getSelectedItem();
        if (agent == null) {
            throw new InvalidDataException("Оберіть агента");
        }
        if (client == null) {
            result = new Client(name, phone, birth, agent);
            company.addClient(result);
        } else {
            result = company.updateClient(client.getId(), name, phone, birth,
                    agent.getId());
        }
    }

    public Client getResult() {
        return result;
    }
}
