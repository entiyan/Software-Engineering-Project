package jdm.service;

import jdm.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Central in-memory data repository. Singleton.
 */
public class DataStore {
    private static DataStore instance;

    private final Map<String, Patient>         patients         = new LinkedHashMap<>();
    private final Map<String, LabResultGroup>  labResultGroups  = new LinkedHashMap<>();
    private final Map<String, LabResult>       labResults       = new LinkedHashMap<>();

    // Doctor credentials: username -> hashed password (plain text for demo)
    private final Map<String, String> doctorCredentials = new LinkedHashMap<>();
    private final Map<String, String> doctorNames       = new LinkedHashMap<>();

    private DataStore() {
        // Seed a default doctor account
        doctorCredentials.put("doctor", "doctor123");
        doctorNames.put("doctor", "Dr. Smith");
        doctorCredentials.put("admin",  "admin123");
        doctorNames.put("admin",  "Dr. Admin");
    }

    public static DataStore getInstance() {
        if (instance == null) instance = new DataStore();
        return instance;
    }

    // ── Patients ─────────────────────────────────────────────────────────────

    public void putPatient(Patient p) { patients.put(p.getPatientId(), p); }
    public Patient getPatient(String id) { return patients.get(id); }
    public Collection<Patient> getAllPatients() { return patients.values(); }

    /** Ensures a {@link Patient} row exists for every foreign key seen in lab data. */
    public void ensurePatientForLabData(String patientId) {
        if (patientId == null || patientId.isBlank()) return;
        patients.computeIfAbsent(patientId,
                id -> new Patient(id, "Patient " + id.substring(0, Math.min(8, id.length())) + "…"));
    }

    public Optional<Patient> findPatientByName(String name) {
        return patients.values().stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    // ── Lab Result Groups ─────────────────────────────────────────────────────

    public void putLabResultGroup(LabResultGroup g) { labResultGroups.put(g.getGroupId(), g); }
    public LabResultGroup getLabResultGroup(String id) { return labResultGroups.get(id); }
    public Collection<LabResultGroup> getAllGroups() { return labResultGroups.values(); }

    // ── Lab Results ───────────────────────────────────────────────────────────

    public void putLabResult(LabResult r) { labResults.put(r.getLabResultId(), r); }
    public LabResult getLabResult(String id) { return labResults.get(id); }
    public Collection<LabResult> getAllLabResults() { return labResults.values(); }

    public List<LabResult> getLabResultsForPatient(String patientId) {
        return labResults.values().stream()
                .filter(r -> patientId.equals(r.getPatientId()))
                .sorted(Comparator.comparing(LabResult::getResultNameEnglish))
                .collect(Collectors.toList());
    }

    public List<LabResult> getLabResultsForPatientAndGroup(String patientId, String groupId) {
        return getLabResultsForPatient(patientId).stream()
                .filter(r -> groupId.equals(r.getGroupId()))
                .collect(Collectors.toList());
    }

    public void addLabResult(LabResult r)    { labResults.put(r.getLabResultId(), r); }
    public boolean removeLabResult(String id){ return labResults.remove(id) != null; }

    // ── Auth ─────────────────────────────────────────────────────────────────

    public Optional<SystemUser> authenticateDoctor(String username, String password) {
        String stored = doctorCredentials.get(username);
        if (stored != null && stored.equals(password)) {
            return Optional.of(UserFactory.createDoctor(username, doctorNames.get(username)));
        }
        return Optional.empty();
    }

    public Optional<SystemUser> authenticatePatient(String patientId, String password) {
        Patient p = patients.get(patientId);
        if (p != null && p.getPasswordHash().equals(password)) {
            return Optional.of(UserFactory.createPatient(patientId, p.getName()));
        }
        return Optional.empty();
    }

    // ── Appointments (all) ────────────────────────────────────────────────────

    public List<Appointment> getAllAppointments() {
        return patients.values().stream()
                .flatMap(p -> p.getAppointments().stream())
                .sorted(Comparator.comparing(a -> a.getDateTime() != null
                        ? a.getDateTime() : java.time.LocalDateTime.MIN))
                .collect(Collectors.toList());
    }
}
