package jdm.service;

import jdm.model.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Human-readable clinical dossier and export helpers.
 */
public class ReportingService {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final DataStore store = DataStore.getInstance();
    private final ClinicalAnalyticsService analytics = new ClinicalAnalyticsService();

    public String buildPatientDossier(Patient p) {
        StringBuilder sb = new StringBuilder();
        sb.append("══ JDM electronic dossier ══\n");
        sb.append("Patient: ").append(p.getName()).append('\n');
        sb.append("ID:      ").append(p.getPatientId()).append("\n\n");

        sb.append("── CMAS (primary series) ──\n");
        List<CmasEntry> cmas = analytics.primaryCmasSeries(p);
        if (cmas.isEmpty()) {
            sb.append("  (no CMAS rows loaded for this patient)\n");
        } else {
            for (CmasEntry e : cmas) {
                sb.append(String.format(Locale.US, "  %s | %s | score %d | %s%n",
                        e.getDate(), e.getScoreGroup(), e.getValue(), e.getSeverityLabel()));
            }
        }

        sb.append("\n── Laboratory (summary) ──\n");
        List<LabResult> labs = store.getLabResultsForPatient(p.getPatientId());
        if (labs.isEmpty()) {
            sb.append("  (no lab rows for this patient id)\n");
        } else {
            for (LabResult lr : labs.stream()
                    .sorted(Comparator.comparing(LabResult::getResultNameEnglish, String.CASE_INSENSITIVE_ORDER))
                    .collect(Collectors.toList())) {
                sb.append("  • ").append(lr.getResultNameEnglish())
                        .append(" — latest: ").append(lr.getLatestValueString())
                        .append(" (n=").append(lr.getMeasurements().size()).append(")\n");
            }
        }

        sb.append("\n── Appointments ──\n");
        List<Appointment> ap = p.getAppointments().stream()
                .sorted(Comparator.comparing(a -> a.getDateTime() != null
                        ? a.getDateTime() : java.time.LocalDateTime.MIN))
                .collect(Collectors.toList());
        if (ap.isEmpty()) sb.append("  (none)\n");
        else for (Appointment a : ap) sb.append("  ").append(a.toString()).append('\n');

        return sb.toString();
    }

    public String measurementDetail(LabResult lr) {
        StringBuilder sb = new StringBuilder();
        sb.append(lr.getResultNameEnglish()).append(" [").append(lr.getUnit()).append("]\n");
        lr.getMeasurements().stream()
                .sorted(Comparator.comparing(Measurement::getDateTime,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .forEach(m -> sb.append("  ")
                        .append(m.getDateTime() != null ? DT.format(m.getDateTime()) : "?")
                        .append("  ").append(m.getValue()).append('\n'));
        return sb.toString();
    }

    public void exportText(Path path, String text) throws IOException {
        Files.writeString(path, text, StandardCharsets.UTF_8);
    }
}
