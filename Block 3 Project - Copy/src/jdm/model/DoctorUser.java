package jdm.model;

public class DoctorUser implements SystemUser {
    private final String username;
    private final String displayName;

    public DoctorUser(String username, String displayName) {
        this.username = username;
        this.displayName = displayName;
    }

    @Override public String getUsername() { return username; }
    @Override public String getDisplayName() { return displayName; }
    @Override public Role getRole() { return Role.DOCTOR; }
    @Override public boolean canEditLabResults() { return true; }
    @Override public boolean canScheduleAppointments() { return true; }
    @Override public boolean canViewAllPatients() { return true; }
}