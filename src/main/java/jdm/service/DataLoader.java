package jdm.service;

import jdm.model.*;
import jdm.util.CsvParser;

import java.io.File;
import java.util.Map;

/**
 * Loads all CSV files into the DataStore.
 */
public class DataLoader {

    public static void load(String dataDirectory) throws Exception {
        DataStore store = DataStore.getInstance();
        String dir = dataDirectory.endsWith(File.separator)
                ? dataDirectory : dataDirectory + File.separator;

        // 1. Patients
        Map<String, Patient> patients = CsvParser.parsePatients(dir + "Patient.csv");
        patients.values().forEach(store::putPatient);
        System.out.println("  Loaded " + patients.size() + " patient(s).");

        // 2. Lab Result Groups
        Map<String, LabResultGroup> groups = CsvParser.parseLabResultGroups(dir + "LabResultGroup.csv");
        groups.values().forEach(store::putLabResultGroup);
        System.out.println("  Loaded " + groups.size() + " lab result group(s).");

        // 3. Lab Results (English names preferred)
        Map<String, LabResult> labResults;
        File enFile = new File(dir + "LabResults_EN_.csv");
        if (enFile.exists()) {
            labResults = CsvParser.parseLabResults(enFile.getAbsolutePath());
        } else {
            labResults = CsvParser.parseLabResults(dir + "LabResult.csv");
        }
        labResults.values().forEach(store::putLabResult);
        System.out.println("  Loaded " + labResults.size() + " lab result type(s).");

        // 4. Measurements
        CsvParser.parseMeasurements(dir + "Measurement.csv", labResults);
        long measCount = labResults.values().stream()
                .mapToLong(r -> r.getMeasurements().size()).sum();
        System.out.println("  Loaded " + measCount + " measurement(s).");

        // 5. CMAS – one file per patient (filename contains patient ID prefix)
        //    Also handle the flat file "1775593554312_CMAS.csv" mapped to Patient X
        File cmasFile = new File(dir + "1775593554312_CMAS.csv");
        if (cmasFile.exists()) {
            // Assign to the one patient in the dataset
            store.getAllPatients().stream().findFirst().ifPresent(p -> {
                try {
                    CsvParser.parseCmas(cmasFile.getAbsolutePath(), p);
                    System.out.println("  Loaded CMAS entries for " + p.getName() + ".");
                } catch (Exception e) {
                    System.err.println("  CMAS parse warning: " + e.getMessage());
                }
            });
        }
    }
}
