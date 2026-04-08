package jdm.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Appointment {
    public enum Status { SCHEDULED, COMPLETED, CANCELLED }

    private final String appointmentId;
    private final String patientId;
    private LocalDateTime dateTime;
    private String reason;
    private String doctorName;
    private Status status;

    public Appointment(String patientId, LocalDateTime dateTime, String reason, String doctorName) {
        this.appointmentId = UUID.randomUUID().toString();
        this.patientId     = patientId;
        this.dateTime      = dateTime;
        this.reason        = reason;
        this.doctorName    = doctorName;
        this.status        = Status.SCHEDULED;
    }

    public String        getAppointmentId()             { return appointmentId; }
    public String        getPatientId()                 { return patientId; }
    public LocalDateTime getDateTime()                  { return dateTime; }
    public void          setDateTime(LocalDateTime dt)  { this.dateTime = dt; }
    public String        getReason()                    { return reason; }
    public void          setReason(String reason)       { this.reason = reason; }
    public String        getDoctorName()                { return doctorName; }
    public void          setDoctorName(String d)        { this.doctorName = d; }
    public Status        getStatus()                    { return status; }
    public void          setStatus(Status s)            { this.status = s; }

    @Override public String toString() {
        return String.format("[%s] %s | Reason: %-25s | Doctor: %-15s | Status: %s",
                appointmentId.substring(0, 8),
                dateTime != null ? dateTime.toString().replace("T", " ") : "TBD",
                reason, doctorName, status);
    }
}
