package io.github.kotr228.insurance.service;

import io.github.kotr228.insurance.model.Agent;
import io.github.kotr228.insurance.model.Client;
import io.github.kotr228.insurance.model.InsurancePeriod;
import io.github.kotr228.insurance.model.InsuranceType;
import io.github.kotr228.insurance.model.Person;
import io.github.kotr228.insurance.model.Policy;
import io.github.kotr228.platform.core.model.FullName;
import io.github.kotr228.platform.core.model.Identifiable;
import io.github.kotr228.platform.core.model.InvalidDataException;
import io.github.kotr228.platform.core.model.Money;
import io.github.kotr228.platform.core.model.PhoneNumber;
import io.github.kotr228.platform.core.repository.Registry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Страхова компанія: зберігає агентів, клієнтів і поліси, стежить
 * за бізнес-правилами (цілісність зв'язків, унікальність телефону)
 * і виконує запити. Інтерфейс програми змінює дані лише через цей клас.
 */
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

    // ---------- Створення нових об'єктів (id видає компанія) ----------

    public Agent createAgent(FullName fullName, PhoneNumber phone,
                             int experienceYears) {
        Agent agent = new Agent(agents.nextId(), fullName, phone,
                experienceYears);
        addAgent(agent);
        return agent;
    }

    public Client createClient(FullName fullName, PhoneNumber phone,
                               LocalDate birthDate, int agentId) {
        Agent agent = require(agents, agentId, "Агента");
        Client client = new Client(clients.nextId(), fullName, phone,
                birthDate, agent);
        addClient(client);
        return client;
    }

    public Policy createPolicy(InsuranceType type, InsurancePeriod period,
                               String insuredEvent, BigDecimal cost,
                               int clientId) {
        Client client = require(clients, clientId, "Клієнта");
        Policy policy = new Policy(policies.nextId(), type, period,
                insuredEvent, cost, client);
        addPolicy(policy);
        return policy;
    }

    // ---------- Додавання готових об'єктів (наприклад, зі сховища) ----------

    public void addAgent(Agent agent) {
        checkPhoneIsFree(agent.getPhone(), null);
        agents.add(agent);
    }

    public void addClient(Client client) {
        if (agents.findById(client.getAgent().getId())
                .filter(a -> a == client.getAgent()).isEmpty()) {
            throw new InvalidDataException(
                    "Агента " + client.getAgent() + " немає в компанії");
        }
        checkPhoneIsFree(client.getPhone(), null);
        clients.add(client);
    }

    public void addPolicy(Policy policy) {
        if (clients.findById(policy.getClient().getId())
                .filter(c -> c == policy.getClient()).isEmpty()) {
            throw new InvalidDataException(
                    "Клієнта " + policy.getClient() + " немає в компанії");
        }
        policies.add(policy);
    }

    // ---------- Видалення ----------

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
                               String insuredEvent, BigDecimal cost, int clientId) {
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
            if (person != owner && person.getPhone().equals(phone)) {
                throw new InvalidDataException("Телефон " + phone.getFormatted()
                        + " уже належить: " + person);
            }
        }
    }

    // ---------- Доступ до даних (лише читання) ----------

    /** Усі агенти в порядку додавання (список лише для читання). */
    public List<Agent> getAgents() {
        return agents.getAll();
    }

    /** Усі клієнти в порядку додавання (список лише для читання). */
    public List<Client> getClients() {
        return clients.getAll();
    }

    /** Усі поліси в порядку додавання (список лише для читання). */
    public List<Policy> getPolicies() {
        return policies.getAll();
    }

    public Optional<Agent> findAgent(int id) {
        return agents.findById(id);
    }

    public Optional<Client> findClient(int id) {
        return clients.findById(id);
    }

    public Optional<Policy> findPolicy(int id) {
        return policies.findById(id);
    }

    public int getAgentCount() {
        return agents.size();
    }

    public int getClientCount() {
        return clients.size();
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
                .max(Comparator.comparing(Policy::getCost));
    }

    /** Загальна вартість усіх полісів. */
    public BigDecimal getTotalCost() {
        return policies.getAll().stream()
                .map(Policy::getCost)
                .reduce(Money.ZERO, BigDecimal::add);
    }

    /** Запит 4. Середня вартість страхування (0, якщо полісів немає). */
    public BigDecimal getAverageCost() {
        return Money.average(getTotalCost(), policies.size());
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

    /** Запит 5. Агент із найбільшою кількістю клієнтів (за рівності — менший id). */
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
        if (policies.isEmpty()) {
            return Optional.empty();
        }
        return countPoliciesByType().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }
}
