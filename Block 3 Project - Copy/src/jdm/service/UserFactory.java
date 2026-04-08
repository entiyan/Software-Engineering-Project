package jdm.service;

import jdm.model.DoctorUser;
import jdm.model.PatientUser;
import jdm.model.SystemUser;

import java.util.Optional;

public final class UserFactory {

    private UserFactory() {}

    /**
     * Authenticates a doctor (username/password) or a patient (patient UUID / password).
     */
    public static Optional<SystemUser> login(String username, String password) {
        if (username == null || username.isBlank()) return Optional.empty();
        String pass = password == null ? "" : password;
        DataStore ds = DataStore.getInstance();
        Optional<SystemUser> doc = ds.authenticateDoctor(username.trim(), pass);
        if (doc.isPresent()) return doc;
        return ds.authenticatePatient(username.trim(), pass);
    }

    public static SystemUser createDoctor(String username, String displayName) {
        return new DoctorUser(username, displayName);
    }

    public static SystemUser createPatient(String patientId, String displayName) {
        return new PatientUser(patientId, displayName);
    }
}
