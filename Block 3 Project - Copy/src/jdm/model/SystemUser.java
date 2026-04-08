package jdm.model;

public interface SystemUser {
    String getUsername();
    String getDisplayName();
    Role getRole();
    boolean canEditLabResults();
    boolean canScheduleAppointments();
    boolean canViewAllPatients();

    enum Role { DOCTOR, PATIENT }
}