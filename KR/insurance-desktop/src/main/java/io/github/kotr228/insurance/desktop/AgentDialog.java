package io.github.kotr228.insurance.desktop;

import io.github.kotr228.insurance.model.Agent;
import io.github.kotr228.insurance.service.InsuranceCompany;
import io.github.kotr228.platform.core.model.FullName;
import io.github.kotr228.platform.core.model.PhoneNumber;
import io.github.kotr228.platform.swing.EntityDialog;

import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.Window;
import java.io.Serial;

/** Вікно додавання та редагування агента. */
public final class AgentDialog extends EntityDialog {
    @Serial
    private static final long serialVersionUID = 1L;

    private final transient InsuranceCompany company;
    private final transient Agent agent;            // null — новий агент
    private final JTextField nameField = new JTextField(26);
    private final JTextField phoneField = new JTextField(16);
    private final JSpinner yearsSpinner =
            new JSpinner(new SpinnerNumberModel(0, 0, 60, 1));
    private transient Agent result;

    @SuppressWarnings("this-escape")      // форма будується в конструкторі
    public AgentDialog(Window owner, InsuranceCompany company, Agent agent) {
        super(owner, agent == null ? "Новий агент" : "Редагування агента");
        this.company = company;
        this.agent = agent;
        addField("ПІБ", nameField);
        addField("Телефон", phoneField);
        addField("Стаж, років", yearsSpinner);
        phoneField.setToolTipText("+380XXXXXXXXX; пробіли, дужки й дефіси допускаються");
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
        result = agent == null
                ? company.createAgent(name, phone, years)
                : company.updateAgent(agent.getId(), name, phone, years);
    }

    public Agent getResult() {
        return result;
    }
}
