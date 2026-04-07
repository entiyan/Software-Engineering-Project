package jdm.ui;

import jdm.model.*;
import jdm.service.*;
import jdm.util.Display;

import java.util.*;

/**
 * Dashboard for a logged-in Doctor. Full access to all patients.
 */
public class DoctorDashboard {
    private final LabResultsScreen   labScreen   = new LabResultsScreen();
    private final CmasScreen         cmasScreen  = new CmasScreen();
    private final AppointmentsScreen apptScreen  = new AppointmentsScreen();
    private final DataStore          store       = DataStore.getInstance();

    public void show(SystemUser user) {
        while (true) {
            System.out.println();
            Display.header("Doctor Portal — " + user.getDisplayName());
            Display.menuItem(1, "Patient List");
            Display.menuItem(2, "View All Upcoming Appointments");
            Display.menuItem(0, "Logout");

            int choice = InputHelper.promptInt("\nSelect: ");
            switch (choice) {
                case 1 -> patientList(user);
                case 2 -> viewAllAppointments();
                case 0 -> { Display.info("Logged out. Goodbye, " + user.getDisplayName() + "!"); return; }
                default -> Display.error("Invalid selection.");
            }
        }
    }

    // ── Patient list ─────────────────────────────────────────────────────────

    private void patientList(SystemUser user) {
        while (true) {
            System.out.println();
            Display.header("Patient List");
            List<Patient> patients = new ArrayList<>(store.getAllPatients());
            if (patients.isEmpty()) { Display.warn("No patients loaded."); return; }

            for (int i = 0; i < patients.size(); i++) {
                Patient p = patients.get(i);
                // Compute latest CMAS if available
                String cmasSummary = p.getCmasEntries().stream()
                        .max(Comparator.naturalOrder())
                        .map(e -> "CMAS " + e.getValue() + " (" + e.getSeverityLabel() + ")")
                        .orElse("No CMAS data");
                System.out.printf("  %s[%d]%s %-30s  %s%n",
                        Display.BOLD, i + 1, Display.RESET, p.getName(), cmasSummary);
            }

            System.out.println();
            Display.menuItem(0, "Back");
            int choice = InputHelper.promptInt("\nSelect patient: ");
            if (choice == 0) return;
            if (choice >= 1 && choice <= patients.size()) {
                patientMenu(user, patients.get(choice - 1));
            } else {
                Display.error("Invalid selection.");
            }
        }
    }

    // ── Per-patient menu ─────────────────────────────────────────────────────

    private void patientMenu(SystemUser user, Patient patient) {
        while (true) {
            System.out.println();
            Display.header("Patient: " + patient.getName());
            System.out.printf("  ID: %s%n%n", patient.getPatientId());
            Display.menuItem(1, "Lab Results");
            Display.menuItem(2, "CMAS Scores");
            Display.menuItem(3, "Appointments");
            Display.menuItem(0, "Back");

            int choice = InputHelper.promptInt("\nSelect: ");
            switch (choice) {
                case 1 -> labScreen.show(user, patient.getPatientId());
                case 2 -> cmasScreen.show(patient.getPatientId());
                case 3 -> apptScreen.show(user, patient.getPatientId());
                case 0 -> { return; }
                default -> Display.error("Invalid selection.");
            }
        }
    }

    // ── All appointments ─────────────────────────────────────────────────────

    private void viewAllAppointments() {
        System.out.println();
        Display.header("All Appointments");
        List<Appointment> all = store.getAllAppointments();
        if (all.isEmpty()) {
            Display.warn("No appointments scheduled.");
        } else {
            for (Appointment a : all) {
                Patient p = store.getPatient(a.getPatientId());
                String pName = p != null ? p.getName() : "Unknown";
                System.out.printf("  %-20s  %s%n", pName, a);
            }
        }
        InputHelper.prompt("\nPress Enter to continue...");
    }
}
