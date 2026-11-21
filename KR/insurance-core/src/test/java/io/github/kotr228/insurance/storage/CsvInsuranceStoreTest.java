package io.github.kotr228.insurance.storage;

import io.github.kotr228.insurance.demo.DemoData;
import io.github.kotr228.insurance.model.InsuranceType;
import io.github.kotr228.insurance.model.Policy;
import io.github.kotr228.insurance.service.InsuranceCompany;
import io.github.kotr228.platform.core.model.Money;
import io.github.kotr228.platform.core.storage.DataFileException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvInsuranceStoreTest {

    @TempDir
    Path dir;

    private CsvInsuranceStore store() {
        return new CsvInsuranceStore(dir, "Копія");
    }

    @Test
    void roundTripKeepsAllData() throws Exception {
        InsuranceCompany original = DemoData.create();
        assertFalse(store().exists());
        store().save(original);
        assertTrue(store().exists());

        InsuranceCompany loaded = store().load();
        assertEquals(original.getAgentCount(), loaded.getAgentCount());
        assertEquals(original.getClientCount(), loaded.getClientCount());
        assertEquals(original.getPolicyCount(), loaded.getPolicyCount());
        for (Policy p : original.getPolicies()) {
            Policy q = loaded.findPolicy(p.getId()).orElseThrow();
            assertEquals(p.getPolicyNumber(), q.getPolicyNumber());
            assertEquals(p.getCost(), q.getCost());
            assertEquals(p.getPeriod(), q.getPeriod());
            assertEquals(p.getInsuredEvent(), q.getInsuredEvent());
            assertEquals(p.getClient().getId(), q.getClient().getId());
        }
        assertEquals(original.getAverageCost(), loaded.getAverageCost());
        assertEquals("Коваленко", loaded.getAgentWithMostClients().orElseThrow()
                .getFullName().getLastName());
        assertFalse(Files.exists(dir.resolve(CsvInsuranceStore.AGENTS_FILE + ".tmp")),
                "тимчасові файли не залишаються");
    }

    @Test
    void newIdsContinueAfterLoadedOnes() throws Exception {
        store().save(DemoData.create());
        InsuranceCompany loaded = store().load();
        Policy created = loaded.createPolicy(InsuranceType.TRAVEL,
                loaded.getPolicies().get(0).getPeriod(), "Втрата багажу",
                Money.of(500), 1);
        assertEquals(7, created.getId());
    }

    @Test
    void separatorAndQuotesInTextSurviveSaving() throws Exception {
        InsuranceCompany company = DemoData.create();
        Policy p = company.getPolicies().get(0);
        String event = "ДТП; викрадення; \"зелена карта\"";
        company.updatePolicy(p.getId(), p.getInsuranceType(), p.getPeriod(), event,
                p.getCost(), p.getClient().getId());
        store().save(company);
        assertEquals(event, store().load().findPolicy(p.getId()).orElseThrow()
                .getInsuredEvent());
    }

    @Test
    void damagedDateReportsFileAndLine() throws Exception {
        store().save(DemoData.create());
        DataFileException e = damage(CsvInsuranceStore.CLIENTS_FILE,
                line -> line.replace("1985-11-02", "02.11.1985"));
        assertEquals(CsvInsuranceStore.CLIENTS_FILE, e.getFileName());
        assertEquals(3, e.getLineNumber());
        assertTrue(e.getMessage().contains("дата"));
    }

    @Test
    void unknownTypeIsReported() throws Exception {
        store().save(DemoData.create());
        DataFileException e = damage(CsvInsuranceStore.POLICIES_FILE,
                line -> line.replace("HEALTH", "PET"));
        assertTrue(e.getMessage().contains("PET"));
    }

    @Test
    void missingReferenceIsReported() throws Exception {
        store().save(DemoData.create());
        DataFileException e = damage(CsvInsuranceStore.POLICIES_FILE,
                line -> line.substring(0, line.lastIndexOf(';')) + ";99");
        assertTrue(e.getMessage().contains("99"));
    }

    @Test
    void wrongFieldCountIsReported() throws Exception {
        store().save(DemoData.create());
        DataFileException e = damage(CsvInsuranceStore.AGENTS_FILE,
                line -> line + ";зайве");
        assertTrue(e.getMessage().contains("полів"));
    }

    /** Псує другий рядок даних файлу (третій рядок) і повертає помилку читання. */
    private DataFileException damage(String fileName, UnaryOperator<String> change)
            throws Exception {
        Path file = dir.resolve(fileName);
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        lines.set(2, change.apply(lines.get(2)));
        Files.write(file, lines, StandardCharsets.UTF_8);
        return assertThrows(DataFileException.class, () -> store().load());
    }
}
