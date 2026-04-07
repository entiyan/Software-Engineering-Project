package jdm.ui;

import jdm.model.*;
import jdm.service.*;
import jdm.util.Display;

import java.time.LocalDateTime;
import java.util.List;

public class AppointmentsScreen {
    private final AppointmentService apptService = new AppointmentService();
    private final DataStore          store        = DataStore.getInstance();

    public void show(SystemUser user, String patientId) {
        while (true) {
            Patient patient = store.getPatient(patientId);
            if (patient == null) { Display.error("Patient not found."); return; }

            System.out.println();
            Display.header("Appointments — " + patient.getName());

            List<Appointment> all = apptService.getForPatient(patientId);
            if (all.isEmpty()) {
                Display.warn("No appointments found.");
            } else {
                Display.section("All Appointments");
                int i = 1;
                for (Appointment a : all) {
                    String color = a.getStatus() == Appointment.Status.CANCELLED
                            ? Display.RESET : (a.getStatus() == Appointment.Status.COMPLETED
                            ? Display.GREEN : Display.CYAN);
                    System.out.printf("  %s[%d]%s %s%s%s%n",
                            Display.BOLD, i++, Display.RESET, color, a, Display.RESET);
                }
            }

            System.out.println();
            Display.menuItem(1, "Schedule new appointment");
            Display.menuItem(2, "Cancel an appointment");
            Display.menuItem(3, "Delete an appointment");
            if (user.getRole() == SystemUser.Role.DOCTOR) {
                Display.menuItem(4, "Mark appointment as completed");
            }
            Display.menuItem(0, "Back");

            int choice = InputHelper.promptInt("\nSelect: ");
            switch (choice) {
                case 1 -> schedule(user, patientId);
                case 2 -> cancelAppt(patientId);
                case 3 -> deleteAppt(patientId);
                case 4 -> { if (user.getRole() == SystemUser.Role.DOCTOR) completeAppt(patientId); }
                case 0 -> { return; }
                default -> Display.error("Invalid selection.");
            }
        }
    }

    // ── Schedule ─────────────────────────────────────────────────────────────

    private void schedule(SystemUser user, String patientId) {
        System.out.println();
        Display.section("Schedule Appointment");
        LocalDateTime dt  = InputHelper.promptDateTime("  Date & time");
        String reason     = InputHelper.prompt("  Reason      : ");
        String doctorName = user.getRole() == SystemUser.Role.DOCTOR
                ? user.getDisplayName()
                : InputHelper.prompt("  Doctor name : ");
        Appointment a = apptService.schedule(patientId, dt, reason, doctorName);
        Display.success("Appointment scheduled: " + a.getDateTime());
    }

    // ── Cancel ───────────────────────────────────────────────────────────────

    private void cancelAppt(String patientId) {
        String apptId = pickAppointmentId(patientId);
        if (apptId == null) return;
        if (apptService.cancel(patientId, apptId)) Display.success("Appointment cancelled.");
        else Display.error("Could not cancel appointment.");
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    private void deleteAppt(String patientId) {
        String apptId = pickAppointmentId(patientId);
        if (apptId == null) return;
        if (InputHelper.confirm("  Permanently delete this appointment?")) {
            if (apptService.delete(patientId, apptId)) Display.success("Deleted.");
            else Display.error("Not found.");
        }
    }

    // ── Complete ─────────────────────────────────────────────────────────────

    private void completeAppt(String patientId) {
        String apptId = pickAppointmentId(patientId);
        if (apptId == null) return;
        List<Appointment> all = apptService.getForPatient(patientId);
        all.stream().filter(a -> a.getAppointmentId().equals(apptId)).findFirst()
                .ifPresentOrElse(a -> {
                    a.setStatus(Appointment.Status.COMPLETED);
                    Display.success("Marked as completed.");
                }, () -> Display.error("Not found."));
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private String pickAppointmentId(String patientId) {
        List<Appointment> all = apptService.getForPatient(patientId);
        if (all.isEmpty()) { Display.warn("No appointments."); return null; }
        for (int i = 0; i < all.size(); i++) {
            System.out.printf("  [%d] %s%n", i + 1, all.get(i));
        }
        int n = InputHelper.promptInt("  Select appointment number: ");
        if (n < 1 || n > all.size()) { Display.error("Invalid."); return null; }
        return all.get(n - 1).getAppointmentId();
    }
}
