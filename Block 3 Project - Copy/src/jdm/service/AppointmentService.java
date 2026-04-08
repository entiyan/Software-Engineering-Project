package jdm.service;

import jdm.model.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class AppointmentService {
    private final DataStore store = DataStore.getInstance();

    public Appointment schedule(String patientId, LocalDateTime dt,
                                String reason, String doctorName) {
        Patient p = store.getPatient(patientId);
        if (p == null) throw new IllegalArgumentException("Patient not found: " + patientId);
        Appointment a = new Appointment(patientId, dt, reason, doctorName);
        p.addAppointment(a);
        return a;
    }

    public boolean cancel(String patientId, String appointmentId) {
        Patient p = store.getPatient(patientId);
        if (p == null) return false;
        for (Appointment a : p.getAppointments()) {
            if (a.getAppointmentId().equals(appointmentId)) {
                a.setStatus(Appointment.Status.CANCELLED);
                return true;
            }
        }
        return false;
    }
    
    public static List<Appointment> getAllAppointments() {
    // This looks at every patient in the system and gathers their appointments
    return DataStore.getInstance().getAllPatients().stream()
            .flatMap(p -> p.getAppointments().stream())
            .collect(Collectors.toList());
    }

    public boolean delete(String patientId, String appointmentId) {
        Patient p = store.getPatient(patientId);
        if (p == null) return false;
        Optional<Appointment> found = p.getAppointments().stream()
                .filter(a -> a.getAppointmentId().equals(appointmentId))
                .findFirst();
        found.ifPresent(p::removeAppointment);
        return found.isPresent();
    }

    public List<Appointment> getForPatient(String patientId) {
        Patient p = store.getPatient(patientId);
        if (p == null) return List.of();
        return p.getAppointments().stream()
                .sorted(Comparator.comparing(a -> a.getDateTime() != null
                        ? a.getDateTime() : LocalDateTime.MIN))
                .collect(Collectors.toList());
    }

    public List<Appointment> getUpcoming(String patientId) {
        LocalDateTime now = LocalDateTime.now();
        return getForPatient(patientId).stream()
                .filter(a -> a.getStatus() == Appointment.Status.SCHEDULED
                        && a.getDateTime() != null && a.getDateTime().isAfter(now))
                .collect(Collectors.toList());
    }
    
}
