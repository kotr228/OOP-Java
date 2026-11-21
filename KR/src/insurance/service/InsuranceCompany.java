package insurance.service;

import insurance.model.Agent;
import insurance.model.Client;
import insurance.model.FullName;
import insurance.model.Identifiable;
import insurance.model.InsurancePeriod;
import insurance.model.InsuranceType;
import insurance.model.InvalidDataException;
import insurance.model.Person;
import insurance.model.PhoneNumber;
import insurance.model.Policy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Страхова компанія: зберігає агентів, клієнтів і поліси, виконує запити. */
public class InsuranceCompany {
    private final String name;
    private final Registry<Agent> agents = new Registry<>();
    private final Registry<Client> clients = new Registry<>();
    private final Registry<Policy> policies = new Registry<>();

    public InsuranceCompany(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidDataException(
                    "Назва компанії не може бути порожньою");
        }
        this.name = name.trim();
    }

    public String getName() {
        return name;
    }

    // ---------- Додавання та видалення ----------

    public void addAgent(Agent agent) {
        checkPhoneIsFree(agent.getPhone(), null);
        agents.add(agent);
    }

    public void addClient(Client client) {
        if (agents.findById(client.getAgent().getId()).isEmpty()) {
            throw new InvalidDataException(
                    "Агента " + client.getAgent() + " немає в компанії");
        }
        checkPhoneIsFree(client.getPhone(), null);
        clients.add(client);
    }

    public void addPolicy(Policy policy) {
        if (clients.findById(policy.getClient().getId()).isEmpty()) {
            throw new InvalidDataException(
                    "Клієнта " + policy.getClient() + " немає в компанії");
        }
        policies.add(policy);
    }

    /** Агента можна видалити, лише якщо за ним не закріплено клієнтів. */
    public boolean removeAgent(int id) {
        boolean hasClients =
                !clients.findAll(c -> c.getAgent().getId() == id).isEmpty();
        if (hasClients) {
            throw new InvalidDataException(
                    "Спершу передайте клієнтів агента іншому агенту");
        }
        return agents.remove(id);
    }

    /** Разом із клієнтом видаляються і всі його поліси. */
    public boolean removeClient(int id) {
        for (Policy policy : policies.findAll(p -> p.getClient().getId() == id)) {
            policies.remove(policy.getId());
        }
        return clients.remove(id);
    }

    public boolean removePolicy(int id) {
        return policies.remove(id);
    }

    // ---------- Редагування (централізований контроль змін) ----------
    // Нові значення спочатку перевіряються всі разом на тимчасовому об'єкті
    // з тим самим id, і лише потім записуються. Тому об'єкт або змінюється
    // повністю, або залишається без змін — часткового редагування не буває.

    /** Змінює дані агента з id. */
    public Agent updateAgent(int id, FullName fullName, PhoneNumber phone,
                             int experienceYears) {
        Agent agent = require(agents, id, "Агента");
        new Agent(id, fullName, phone, experienceYears);   // перевірка значень
        checkPhoneIsFree(phone, agent);
        agent.setFullName(fullName);
        agent.setPhone(phone);
        agent.setExperienceYears(experienceYears);
        return agent;
    }

    /** Змінює дані клієнта з id; новий агент має бути в компанії. */
    public Client updateClient(int id, FullName fullName, PhoneNumber phone,
                               LocalDate birthDate, int agentId) {
        Client client = require(clients, id, "Клієнта");
        Agent agent = require(agents, agentId, "Агента");
        new Client(id, fullName, phone, birthDate, agent);  // перевірка значень
        checkPhoneIsFree(phone, client);
        client.setFullName(fullName);
        client.setPhone(phone);
        client.setBirthDate(birthDate);
        client.setAgent(agent);
        return client;
    }

    /** Змінює умови поліса з id; номер поліса не змінюється. */
    public Policy updatePolicy(int id, InsuranceType type, InsurancePeriod period,
                               String insuredEvent, double cost, int clientId) {
        Policy policy = require(policies, id, "Поліса");
        Client client = require(clients, clientId, "Клієнта");
        new Policy(id, type, period, insuredEvent, cost, client);  // перевірка
        policy.setInsuranceType(type);
        policy.setPeriod(period);
        policy.setInsuredEvent(insuredEvent);
        policy.setCost(cost);
        policy.setClient(client);
        return policy;
    }

    /** Повертає об'єкт з id або генерує виняток, якщо його немає в компанії. */
    private static <T extends Identifiable> T require(Registry<T> registry, int id,
                                                      String what) {
        return registry.findById(id).orElseThrow(() -> new InvalidDataException(
                what + " з id " + id + " немає в компанії"));
    }

    /** Номер телефону не може належати іншій особі компанії. */
    private void checkPhoneIsFree(PhoneNumber phone, Person owner) {
        List<Person> people = new ArrayList<>(agents.getAll());
        people.addAll(clients.getAll());
        for (Person person : people) {
            if (person != owner
                    && person.getPhone().getNumber().equals(phone.getNumber())) {
                throw new InvalidDataException("Телефон " + phone.getFormatted()
                        + " уже належить: " + person);
            }
        }
    }

    public Registry<Agent> getAgents() {
        return agents;
    }

    public Registry<Client> getClients() {
        return clients;
    }

    public Registry<Policy> getPolicies() {
        return policies;
    }

    public List<Policy> getPoliciesOf(Client client) {
        return policies.findAll(p -> p.getClient() == client);
    }

    // ---------- Запити до системи (варіант 26) ----------

    /** Запит 1. Список клієнтів, упорядкований за ПІБ. */
    public List<Client> getClientList() {
        List<Client> result = new ArrayList<>(clients.getAll());
        result.sort(Comparator.naturalOrder());
        return result;
    }

    /** Запит 2. Кількість полісів. */
    public int getPolicyCount() {
        return policies.size();
    }

    /** Запит 3. Найдорожчий поліс. */
    public Optional<Policy> getMostExpensivePolicy() {
        return policies.getAll().stream()
                .max(Comparator.comparingDouble(Policy::getCost));
    }

    /** Запит 4. Середня вартість страхування. */
    public double getAverageCost() {
        return policies.getAll().stream()
                .mapToDouble(Policy::getCost)
                .average()
                .orElse(0);
    }

    /** Кількість клієнтів кожного агента (агенти без клієнтів мають 0). */
    public Map<Agent, Integer> countClientsByAgent() {
        // LinkedHashMap зберігає порядок агентів, як у списку
        Map<Agent, Integer> counts = new LinkedHashMap<>();
        for (Agent agent : agents.getAll()) {
            counts.put(agent, 0);
        }
        for (Client client : clients.getAll()) {
            counts.merge(client.getAgent(), 1, Integer::sum);
        }
        return counts;
    }

    /** Запит 5. Агент із найбільшою кількістю клієнтів. */
    public Optional<Agent> getAgentWithMostClients() {
        return countClientsByAgent().entrySet().stream()
                .max(Map.Entry.<Agent, Integer>comparingByValue()
                        .thenComparing(e -> e.getKey().getId(),
                                Comparator.reverseOrder()))
                .map(Map.Entry::getKey);
    }

    /** Кількість полісів кожного типу страхування. */
    public Map<InsuranceType, Integer> countPoliciesByType() {
        Map<InsuranceType, Integer> counts = new EnumMap<>(InsuranceType.class);
        for (InsuranceType type : InsuranceType.values()) {
            counts.put(type, 0);
        }
        for (Policy policy : policies.getAll()) {
            counts.merge(policy.getInsuranceType(), 1, Integer::sum);
        }
        return counts;
    }

    /** Запит 6. Тип страхування з найбільшим попитом. */
    public Optional<InsuranceType> getMostPopularType() {
        if (policies.size() == 0) {
            return Optional.empty();
        }
        return countPoliciesByType().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }
}
