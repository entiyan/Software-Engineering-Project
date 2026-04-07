package jdm.model;

public class UserFactory {
    public static SystemUser createDoctor(String username, String displayName) {
        return new DoctorUser(username, displayName);
    }
    public static SystemUser createPatient(String patientId, String displayName) {
        return new PatientUser(patientId, displayName);
    }
}
