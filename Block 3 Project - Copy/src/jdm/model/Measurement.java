package jdm.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Measurement {
    private String measurementId;
    private final String labResultId;
    private LocalDateTime dateTime;
    private String value;

    public Measurement(String measurementId, String labResultId,
                       LocalDateTime dateTime, String value) {
        this.measurementId = measurementId != null ? measurementId : UUID.randomUUID().toString();
        this.labResultId   = labResultId;
        this.dateTime      = dateTime;
        this.value         = value;
    }

    public String        getMeasurementId()            { return measurementId; }
    public String        getLabResultId()              { return labResultId; }
    public LocalDateTime getDateTime()                 { return dateTime; }
    public void          setDateTime(LocalDateTime dt) { this.dateTime = dt; }
    public String        getValue()                    { return value; }
    public void          setValue(String value)        { this.value = value; }

    @Override public String toString() {
        return String.format("%-20s  Value: %s",
                dateTime != null ? dateTime.toString().replace("T", " ") : "Unknown date", value);
    }
}
