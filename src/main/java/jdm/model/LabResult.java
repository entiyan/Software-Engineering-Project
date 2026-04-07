package jdm.model;

import java.util.*;

public class LabResult {
    private final String labResultId;
    private final String groupId;
    private final String patientId;
    private final String resultNameOriginal;
    private final String resultNameEnglish;
    private final String unit;
    private final List<Measurement> measurements = new ArrayList<>();

    public LabResult(String labResultId, String groupId, String patientId,
                     String resultNameOriginal, String resultNameEnglish, String unit) {
        this.labResultId       = labResultId;
        this.groupId           = groupId;
        this.patientId         = patientId;
        this.resultNameOriginal = resultNameOriginal;
        this.resultNameEnglish  = resultNameEnglish;
        this.unit              = unit;
    }

    public String getLabResultId()        { return labResultId; }
    public String getGroupId()            { return groupId; }
    public String getPatientId()          { return patientId; }
    public String getResultNameOriginal() { return resultNameOriginal; }
    public String getResultNameEnglish()  { return resultNameEnglish; }
    public String getUnit()               { return unit; }

    public List<Measurement> getMeasurements()      { return measurements; }
    public void addMeasurement(Measurement m)        { measurements.add(m); }
    public boolean removeMeasurement(Measurement m)  { return measurements.remove(m); }

    /** Latest measurement value or "--" if none. */
    public String getLatestValueString() {
        return measurements.stream()
                .filter(m -> m.getDateTime() != null)
                .max(Comparator.comparing(Measurement::getDateTime))
                .map(m -> m.getValue() + (unit != null && !unit.isBlank() ? " " + unit : ""))
                .orElse("--");
    }

    @Override public String toString() {
        String name = resultNameEnglish != null && !resultNameEnglish.isBlank()
                ? resultNameEnglish : resultNameOriginal;
        return String.format("%-30s  Unit: %-12s  Latest: %s", name,
                unit == null ? "" : unit, getLatestValueString());
    }
}
