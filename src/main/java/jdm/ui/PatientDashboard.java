package jdm.ui;

import jdm.model.*;
import jdm.service.*;
import jdm.util.Display;

import java.util.List;

/**
 * Dashboard for a logged-in Patient. They can only view their own data.
 */
public class PatientDashboard {
    private final LabResultsScreen    labScreen   = new LabResultsScreen();
    private final CmasScreen          cmasScreen  = new CmasScreen();
    private final AppointmentsScreen  apptScreen  = new AppointmentsScreen();
    private final AppointmentService  apptService = new AppointmentService();
    private final DataStore           store       = DataStore.getInstance();

    public void show(SystemUser user) {
        String patientId = user.getUsername(); // patient's username == patientId
        Patient patient  = store.getPatient(patientId);

        while (true) {
            System.out.println();
            Display.header("Patient Portal — " + user.getDisplayName());

            // Show upcoming appointments as a quick summary
            List<Appointment> upcoming = apptService.getUpcoming(patientId);
            if (!upcoming.isEmpty()) {
                Display.section("Upcoming Appointments");
                upcoming.stream().limit(3).forEach(a ->
                        System.out.println("  • " + a.getDateTime().toString().replace("T"," ") +
                                "  |  " + a.getReason() + "  |  " + a.getDoctorName()));
            }

            System.out.println();
            Display.menuItem(1, "View Lab Results");
            Display.menuItem(2, "View CMAS Scores");
            Display.menuItem(3, "My Appointments");
            Display.menuItem(0, "Logout");

            int choice = InputHelper.promptInt("\nSelect: ");
            switch (choice) {
                case 1 -> labScreen.show(user, patientId);
                case 2 -> cmasScreen.show(patientId);
                case 3 -> apptScreen.show(user, patientId);
                case 0 -> { Display.info("Logged out. Goodbye, " + user.getDisplayName() + "!"); return; }
                default -> Display.error("Invalid selection.");
            }
        }
    }
}
