package insurance.gui;

import insurance.model.Agent;
import insurance.model.FullName;
import insurance.model.PhoneNumber;
import insurance.service.InsuranceCompany;

import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/** Вікно додавання та редагування агента. */
public final class AgentDialog extends EntityDialog {
    private static final long serialVersionUID = 1L;

    private final transient InsuranceCompany company;
    private final transient Agent agent;            // null — новий агент
    private final JTextField nameField = new JTextField(26);
    private final JTextField phoneField = new JTextField(16);
    private final JSpinner yearsSpinner =
            new JSpinner(new SpinnerNumberModel(0, 0, 60, 1));
    private transient Agent result;

    public AgentDialog(JFrame owner, InsuranceCompany company, Agent agent) {
        super(owner, agent == null ? "Новий агент" : "Редагування агента");
        this.company = company;
        this.agent = agent;
        addField("ПІБ", nameField);
        addField("Телефон", phoneField);
        addField("Стаж, років", yearsSpinner);
        if (agent != null) {
            nameField.setText(agent.getFullName().toString());
            phoneField.setText(agent.getPhone().getNumber());
            yearsSpinner.setValue(agent.getExperienceYears());
        } else {
            phoneField.setText("+380");
        }
    }

    @Override
    protected void save() {
        FullName name = FullName.parse(nameField.getText());
        PhoneNumber phone = new PhoneNumber(phoneField.getText());
        int years = (Integer) yearsSpinner.getValue();
        if (agent == null) {
            result = new Agent(name, phone, years);
            company.addAgent(result);
        } else {
            result = company.updateAgent(agent.getId(), name, phone, years);
        }
    }

    public Agent getResult() {
        return result;
    }
}
