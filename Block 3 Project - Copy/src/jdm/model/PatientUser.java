package jdm.model;

/**
 * Authenticated end-user tied to a {@link Patient} record (username = patient UUID).
 */
public class PatientUser implements SystemUser {
    private final String patientId;
    private final String displayName;

    public PatientUser(String patientId, String displayName) {
        this.patientId = patientId;
        this.displayName = displayName;
    }

    /** Same as patient UUID in {@link Patient#getPatientId()}. */
    public String getPatientId() { return patientId; }

    @Override public String getUsername() { return patientId; }
    @Override public String getDisplayName() { return displayName; }
    @Override public Role getRole() { return Role.PATIENT; }
    @Override public boolean canEditLabResults() { return false; }
    @Override public boolean canScheduleAppointments() { return true; }
    @Override public boolean canViewAllPatients() { return false; }
}