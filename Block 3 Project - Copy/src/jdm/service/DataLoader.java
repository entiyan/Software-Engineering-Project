package jdm.service;

import jdm.model.*;
import jdm.util.CsvParser;

import java.io.File;
import java.util.Map;

/**
 * Loads all CSV files into the {@link DataStore}.
 */
public final class DataLoader {

    private DataLoader() {}

    public static void load(String dataDirectory) throws Exception {
        DataStore store = DataStore.getInstance();
        String dir = dataDirectory.endsWith(File.separator)
                ? dataDirectory : dataDirectory + File.separator;

        Map<String, Patient> patients = CsvParser.parsePatients(dir + "Patient.csv");
        patients.values().forEach(store::putPatient);
        System.out.println("  Loaded " + patients.size() + " patient(s) from Patient.csv.");

        Map<String, LabResultGroup> groups = CsvParser.parseLabResultGroups(dir + "LabResultGroup.csv");
        groups.values().forEach(store::putLabResultGroup);
        System.out.println("  Loaded " + groups.size() + " lab result group(s).");

        Map<String, LabResult> labResults = loadLabResultsMap(dir);
        for (LabResult lr : labResults.values()) {
            store.ensurePatientForLabData(lr.getPatientId());
        }
        labResults.values().forEach(store::putLabResult);
        System.out.println("  Loaded " + labResults.size() + " lab result row(s).");

        CsvParser.parseMeasurements(dir + "Measurement.csv", labResults);
        long measCount = labResults.values().stream()
                .mapToLong(r -> r.getMeasurements().size()).sum();
        System.out.println("  Loaded " + measCount + " measurement(s).");

        loadCmas(dir, store);
    }

    private static Map<String, LabResult> loadLabResultsMap(String dir) throws Exception {
        String[] candidates = {
                dir + "LabResults(EN).csv",
                dir + "LabResults_EN_.csv",
                dir + "LabResult.csv"
        };
        for (String path : candidates) {
            File f = new File(path);
            if (f.isFile()) {
                System.out.println("  Using lab definition file: " + f.getName());
                return CsvParser.parseLabResults(f.getAbsolutePath());
            }
        }
        throw new java.io.FileNotFoundException("No LabResults(EN).csv / LabResult.csv in " + dir);
    }

    private static void loadCmas(String dir, DataStore store) {
        File flat = new File(dir + "CMAS.csv");
        if (flat.isFile()) {
            assignCmasFile(flat, store);
            return;
        }
        File legacy = new File(dir + "1775593554312_CMAS.csv");
        if (legacy.isFile()) {
            assignCmasFile(legacy, store);
        }
    }

    private static void assignCmasFile(File cmasFile, DataStore store) {
        store.findPatientByName("Patient X")
                .or(() -> store.getAllPatients().stream().findFirst())
                .ifPresentOrElse(p -> {
                    try {
                        CsvParser.parseCmas(cmasFile.getAbsolutePath(), p);
                        System.out.println("  Loaded CMAS grid from " + cmasFile.getName() + " for " + p.getName() + ".");
                    } catch (Exception e) {
                        System.err.println("  CMAS parse warning: " + e.getMessage());
                    }
                }, () -> System.err.println("  CMAS file present but no patient row to attach it to."));
    }
}
