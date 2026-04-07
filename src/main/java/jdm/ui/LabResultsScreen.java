package jdm.ui;

import jdm.model.*;
import jdm.service.*;
import jdm.util.Display;

import java.time.LocalDateTime;
import java.util.*;

public class LabResultsScreen {
    private final LabResultService labService = new LabResultService();
    private final DataStore store             = DataStore.getInstance();

    public void show(SystemUser user, String patientId) {
        while (true) {
            Patient patient = store.getPatient(patientId);
            if (patient == null) { Display.error("Patient not found."); return; }

            System.out.println();
            Display.header("Lab Results — " + patient.getName());

            // Group results by category
            Map<String, List<LabResult>> grouped = new LinkedHashMap<>();
            for (LabResult lr : labService.getForPatient(patientId)) {
                LabResultGroup grp = store.getLabResultGroup(lr.getGroupId());
                String grpName = grp != null ? grp.getGroupName() : "Other";
                grouped.computeIfAbsent(grpName, k -> new ArrayList<>()).add(lr);
            }

            if (grouped.isEmpty()) {
                Display.warn("No lab results found for this patient.");
            } else {
                int idx = 1;
                List<LabResult> indexed = new ArrayList<>();
                for (Map.Entry<String, List<LabResult>> entry : grouped.entrySet()) {
                    Display.section(entry.getKey());
                    for (LabResult lr : entry.getValue()) {
                        String name = lr.getResultNameEnglish() != null && !lr.getResultNameEnglish().isBlank()
                                ? lr.getResultNameEnglish() : lr.getResultNameOriginal();
                        System.out.printf("  %s[%3d]%s %-32s  Latest: %s%n",
                                Display.BOLD, idx, Display.RESET, name, lr.getLatestValueString());
                        indexed.add(lr);
                        idx++;
                    }
                }

                System.out.println();
                Display.menuItem(0, "Back");
                Display.menuItem(-1, "View all measurements for a result");
                if (user.canEditLabResults()) {
                    Display.menuItem(-2, "Add new lab result type");
                    Display.menuItem(-3, "Delete a lab result");
                    Display.menuItem(-4, "Add measurement to a result");
                    Display.menuItem(-5, "Edit/delete a measurement");
                }

                int choice = InputHelper.promptInt("\nSelect result number or option: ");

                if (choice == 0) return;
                else if (choice == -1) pickAndViewMeasurements(indexed);
                else if (choice == -2 && user.canEditLabResults()) addLabResult(patientId);
                else if (choice == -3 && user.canEditLabResults()) deleteLabResult(indexed);
                else if (choice == -4 && user.canEditLabResults()) addMeasurement(indexed);
                else if (choice == -5 && user.canEditLabResults()) editMeasurements(indexed);
                else if (choice >= 1 && choice <= indexed.size()) {
                    viewMeasurements(indexed.get(choice - 1));
                } else {
                    Display.error("Invalid selection.");
                }
            }
            if (grouped.isEmpty()) return;
        }
    }

    // ── View all measurements for a result ───────────────────────────────────

    private void pickAndViewMeasurements(List<LabResult> indexed) {
        int n = InputHelper.promptInt("  Enter result number: ");
        if (n >= 1 && n <= indexed.size()) viewMeasurements(indexed.get(n - 1));
        else Display.error("Invalid number.");
    }

    private void viewMeasurements(LabResult lr) {
        System.out.println();
        String name = lr.getResultNameEnglish() != null && !lr.getResultNameEnglish().isBlank()
                ? lr.getResultNameEnglish() : lr.getResultNameOriginal();
        Display.header("Measurements — " + name + (lr.getUnit() != null ? " (" + lr.getUnit() + ")" : ""));
        List<Measurement> measurements = lr.getMeasurements();
        measurements.sort(Comparator.comparing(m -> m.getDateTime() != null
                ? m.getDateTime() : LocalDateTime.MIN));
        if (measurements.isEmpty()) {
            Display.warn("No measurements recorded.");
        } else {
            int i = 1;
            for (Measurement m : measurements) {
                System.out.printf("  %3d. %s%n", i++, m);
            }
        }
        InputHelper.prompt("\nPress Enter to continue...");
    }

    // ── Add lab result ───────────────────────────────────────────────────────

    private void addLabResult(String patientId) {
        System.out.println();
        Display.section("Add New Lab Result Type");
        Collection<LabResultGroup> groups = labService.getAllGroups();
        List<LabResultGroup> groupList = new ArrayList<>(groups);
        for (int i = 0; i < groupList.size(); i++) {
            System.out.printf("  [%d] %s%n", i + 1, groupList.get(i).getGroupName());
        }
        int gi = InputHelper.promptInt("  Select group: ");
        if (gi < 1 || gi > groupList.size()) { Display.error("Invalid group."); return; }
        String groupId = groupList.get(gi - 1).getGroupId();
        String name    = InputHelper.prompt("  Result name (English): ");
        String unit    = InputHelper.prompt("  Unit (leave blank if none): ");
        LabResult lr   = labService.addLabResult(groupId, patientId, name, unit);
        Display.success("Added lab result: " + lr.getResultNameEnglish() + " [" + lr.getLabResultId().substring(0,8) + "]");
    }

    // ── Delete lab result ────────────────────────────────────────────────────

    private void deleteLabResult(List<LabResult> indexed) {
        int n = InputHelper.promptInt("  Enter result number to delete: ");
        if (n < 1 || n > indexed.size()) { Display.error("Invalid."); return; }
        LabResult lr = indexed.get(n - 1);
        String name = lr.getResultNameEnglish();
        if (InputHelper.confirm("  Delete '" + name + "' and all its measurements?")) {
            labService.deleteLabResult(lr.getLabResultId());
            Display.success("Deleted: " + name);
        }
    }

    // ── Add measurement ──────────────────────────────────────────────────────

    private void addMeasurement(List<LabResult> indexed) {
        int n = InputHelper.promptInt("  Enter result number to add measurement: ");
        if (n < 1 || n > indexed.size()) { Display.error("Invalid."); return; }
        LabResult lr = indexed.get(n - 1);
        LocalDateTime dt = InputHelper.promptDateTime("  Date/time");
        String value = InputHelper.prompt("  Value: ");
        labService.addMeasurement(lr.getLabResultId(), dt, value);
        Display.success("Measurement added to " + lr.getResultNameEnglish());
    }

    // ── Edit/delete measurement ──────────────────────────────────────────────

    private void editMeasurements(List<LabResult> indexed) {
        int n = InputHelper.promptInt("  Enter result number: ");
        if (n < 1 || n > indexed.size()) { Display.error("Invalid."); return; }
        LabResult lr = indexed.get(n - 1);
        List<Measurement> ms = lr.getMeasurements();
        ms.sort(Comparator.comparing(m -> m.getDateTime() != null ? m.getDateTime() : LocalDateTime.MIN));
        if (ms.isEmpty()) { Display.warn("No measurements."); return; }
        for (int i = 0; i < ms.size(); i++) {
            System.out.printf("  [%d] %s%n", i + 1, ms.get(i));
        }
        int mi = InputHelper.promptInt("  Select measurement: ");
        if (mi < 1 || mi > ms.size()) { Display.error("Invalid."); return; }
        Measurement m = ms.get(mi - 1);
        System.out.println("  [1] Edit value   [2] Delete   [0] Cancel");
        int op = InputHelper.promptInt("  Option: ");
        if (op == 1) {
            String newVal = InputHelper.prompt("  New value: ");
            labService.editMeasurementValue(lr.getLabResultId(), m.getMeasurementId(), newVal);
            Display.success("Value updated.");
        } else if (op == 2) {
            if (InputHelper.confirm("  Delete this measurement?")) {
                labService.deleteMeasurement(lr.getLabResultId(), m.getMeasurementId());
                Display.success("Deleted.");
            }
        }
    }
}
