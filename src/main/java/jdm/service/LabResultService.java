package jdm.service;

import jdm.model.*;

import java.time.LocalDateTime;
import java.util.*;

public class LabResultService {
    private final DataStore store = DataStore.getInstance();

    /** Add a new LabResult type (doctor only). */
    public LabResult addLabResult(String groupId, String patientId,
                                  String nameEnglish, String unit) {
        String id = UUID.randomUUID().toString();
        LabResult lr = new LabResult(id, groupId, patientId,
                nameEnglish, nameEnglish, unit);
        store.addLabResult(lr);
        return lr;
    }

    /** Delete a LabResult and all its measurements. */
    public boolean deleteLabResult(String labResultId) {
        return store.removeLabResult(labResultId);
    }

    /** Add a measurement to an existing LabResult. */
    public Measurement addMeasurement(String labResultId, LocalDateTime dt, String value) {
        LabResult lr = store.getLabResult(labResultId);
        if (lr == null) throw new IllegalArgumentException("Lab result not found.");
        Measurement m = new Measurement(null, labResultId, dt, value);
        lr.addMeasurement(m);
        return m;
    }

    /** Edit an existing measurement's value. */
    public boolean editMeasurementValue(String labResultId, String measurementId, String newValue) {
        LabResult lr = store.getLabResult(labResultId);
        if (lr == null) return false;
        return lr.getMeasurements().stream()
                .filter(m -> m.getMeasurementId().equals(measurementId))
                .findFirst()
                .map(m -> { m.setValue(newValue); return true; })
                .orElse(false);
    }

    /** Delete a specific measurement. */
    public boolean deleteMeasurement(String labResultId, String measurementId) {
        LabResult lr = store.getLabResult(labResultId);
        if (lr == null) return false;
        return lr.getMeasurements().removeIf(m -> m.getMeasurementId().equals(measurementId));
    }

    public List<LabResult> getForPatient(String patientId) {
        return store.getLabResultsForPatient(patientId);
    }

    public List<LabResult> getForPatientAndGroup(String patientId, String groupId) {
        return store.getLabResultsForPatientAndGroup(patientId, groupId);
    }

    public LabResult getById(String id) { return store.getLabResult(id); }

    public Collection<LabResultGroup> getAllGroups() { return store.getAllGroups(); }
}
