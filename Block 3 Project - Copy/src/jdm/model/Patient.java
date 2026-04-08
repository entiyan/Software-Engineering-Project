package jdm.model;

import java.util.*;
import java.util.stream.Collectors;

public class Patient {
    private final String patientId;
    private String name;
    private String passwordHash;
    private final List<CmasEntry> cmasEntries = new ArrayList<>();
    private final List<Appointment> appointments = new ArrayList<>();

    public Patient(String patientId, String name) {
        this.patientId = patientId;
        this.name = name;
        this.passwordHash = "patient123"; // default
    }

    public String getPatientId()           { return patientId; }
    public String getName()                { return name; }
    public void   setName(String name)     { this.name = name; }
    public String getPasswordHash()        { return passwordHash; }
    public void   setPasswordHash(String p){ this.passwordHash = p; }

    public List<CmasEntry>    getCmasEntries()  { return Collections.unmodifiableList(cmasEntries); }
    public List<Appointment>  getAppointments() { return Collections.unmodifiableList(appointments); }

    /** Chronological CMAS entries (may include multiple rows per visit from the grid file). */
    public List<CmasEntry> getCmasEntriesSorted() {
        return cmasEntries.stream()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toUnmodifiableList());
    }

    public void addCmasEntry(CmasEntry e)     { cmasEntries.add(e); }
    public void addAppointment(Appointment a) { appointments.add(a); }
    public void removeAppointment(Appointment a){ appointments.remove(a); }

    public String shortId() {
        return patientId.length() > 8 ? patientId.substring(0, 8) + "…" : patientId;
    }

    @Override public String toString() { return name + " [" + shortId() + "]"; }
}
