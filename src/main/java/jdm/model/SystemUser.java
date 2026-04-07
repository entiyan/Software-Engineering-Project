package jdm.model;

/**
 * Represents an authenticated system user. Implementations define what
 * actions are permitted based on role.
 */
public interface SystemUser {
    String getUsername();
    String getDisplayName();
    Role   getRole();
    boolean canEditLabResults();
    boolean canScheduleAppointments();
    boolean canViewAllPatients();

    enum Role { DOCTOR, PATIENT }
}

// ─────────────────────────────────────────────────────────
// Doctor implementation
// ─────────────────────────────────────────────────────────
class DoctorUser implements SystemUser {
    private final String username;
    private final String displayName;

    public DoctorUser(String username, String displayName) {
        this.username    = username;
        this.displayName = displayName;
    }

    @Override public String  getUsername()              { return username; }
    @Override public String  getDisplayName()           { return displayName; }
    @Override public Role    getRole()                  { return Role.DOCTOR; }
    @Override public boolean canEditLabResults()        { return true; }
    @Override public boolean canScheduleAppointments()  { return true; }
    @Override public boolean canViewAllPatients()       { return true; }
}

// ─────────────────────────────────────────────────────────
// Patient implementation
// ─────────────────────────────────────────────────────────
class PatientUser implements SystemUser {
    private final String username;   // equals patientId
    private final String displayName;

    public PatientUser(String username, String displayName) {
        this.username    = username;
        this.displayName = displayName;
    }

    @Override public String  getUsername()              { return username; }
    @Override public String  getDisplayName()           { return displayName; }
    @Override public Role    getRole()                  { return Role.PATIENT; }
    @Override public boolean canEditLabResults()        { return false; }
    @Override public boolean canScheduleAppointments()  { return true; }
    @Override public boolean canViewAllPatients()       { return false; }
}
