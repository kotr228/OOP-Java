package insurance.service;

import insurance.model.Agent;
import insurance.model.Client;
import insurance.model.InsuranceType;
import insurance.model.InvalidDataException;
import insurance.model.Policy;

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
        agents.add(agent);
    }

    public void addClient(Client client) {
        if (agents.findById(client.getAgent().getId()).isEmpty()) {
            throw new InvalidDataException(
                    "Агента " + client.getAgent() + " немає в компанії");
        }
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
