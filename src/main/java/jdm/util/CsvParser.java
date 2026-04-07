package jdm.util;

import jdm.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.*;
import java.util.*;

/**
 * Parses all CSV files into domain objects.
 * Handles the mixed date formats present in the JDM dataset.
 */
public class CsvParser {

    // All date/datetime patterns found in the dataset
    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("d-M-yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
    );

    private static final List<DateTimeFormatter> DATETIME_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("d-MM-yyyyHH:mm"),
            DateTimeFormatter.ofPattern("dd-MM-yyyyHH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
    );

    // ── Patients ────────────────────────────────────────────────────────────

    public static Map<String, Patient> parsePatients(String path) throws IOException {
        Map<String, Patient> map = new LinkedHashMap<>();
        try (BufferedReader br = reader(path)) {
            br.readLine(); // header
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = splitCsv(line);
                if (parts.length < 2) continue;
                String id   = parts[0].trim();
                String name = parts[1].trim();
                map.put(id, new Patient(id, name));
            }
        }
        return map;
    }

    // ── Lab Result Groups ───────────────────────────────────────────────────

    public static Map<String, LabResultGroup> parseLabResultGroups(String path) throws IOException {
        Map<String, LabResultGroup> map = new LinkedHashMap<>();
        try (BufferedReader br = reader(path)) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = splitCsv(line);
                if (p.length < 2) continue;
                map.put(p[0].trim(), new LabResultGroup(p[0].trim(), p[1].trim()));
            }
        }
        return map;
    }

    // ── Lab Results (with English names) ────────────────────────────────────

    public static Map<String, LabResult> parseLabResults(String path) throws IOException {
        Map<String, LabResult> map = new LinkedHashMap<>();
        try (BufferedReader br = reader(path)) {
            br.readLine(); // header
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = splitCsv(line);
                if (p.length < 5) continue;
                String id          = p[0].trim();
                String groupId     = p[1].trim();
                String patientId   = p[2].trim();
                String nameOrig    = p[3].trim();
                String unit        = p[4].trim();
                String nameEn      = p.length > 5 ? p[5].trim() : nameOrig;
                map.put(id, new LabResult(id, groupId, patientId, nameOrig, nameEn, unit));
            }
        }
        return map;
    }

    // ── Measurements ────────────────────────────────────────────────────────

    public static void parseMeasurements(String path, Map<String, LabResult> labResults) throws IOException {
        try (BufferedReader br = reader(path)) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = splitCsv(line);
                if (p.length < 4) continue;
                String measurementId = p[0].trim();
                String labResultId   = p[1].trim();
                String rawDateTime   = p[2].trim();
                String value         = p[3].trim();

                LocalDateTime dt = parseDateTime(rawDateTime);
                LabResult lr = labResults.get(labResultId);
                if (lr != null) {
                    lr.addMeasurement(new Measurement(measurementId, labResultId, dt, value));
                }
            }
        }
    }

    // ── CMAS ────────────────────────────────────────────────────────────────

    /**
     * The CMAS file is transposed: columns are dates, rows are score groups.
     * Format: [label], [date1], [date2], ...
     *         [group "> 10"], [val], [val], ...
     *         [group "4-9"],  [val], [val], ...
     */
    public static void parseCmas(String path, Patient patient) throws IOException {
        try (BufferedReader br = reader(path)) {
            // Row 0 = dates header (first cell blank)
            String headerLine = br.readLine();
            if (headerLine == null) return;
            String[] dateTokens = splitCsv(headerLine);

            // Parse dates (skip first cell)
            LocalDate[] dates = new LocalDate[dateTokens.length];
            for (int i = 1; i < dateTokens.length; i++) {
                dates[i] = parseDate(dateTokens[i].trim());
            }

            // Remaining rows = score groups
            String line;
            while ((line = br.readLine()) != null) {
                String[] vals = splitCsv(line);
                if (vals.length < 2) continue;
                String group = vals[0].trim();
                if (group.isBlank()) continue;

                for (int i = 1; i < vals.length && i < dates.length; i++) {
                    String raw = vals[i].trim();
                    if (raw.isBlank()) continue;
                    try {
                        int score = Integer.parseInt(raw);
                        if (dates[i] != null) {
                            patient.addCmasEntry(new CmasEntry(dates[i], group, score));
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        // Sort entries chronologically
        patient.getCmasEntries(); // already unmodifiable; sorting happens via TreeSet at display time
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private static BufferedReader reader(String path) throws IOException {
        return new BufferedReader(
                new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
    }

    /** Simple CSV splitter (handles quoted fields). */
    public static String[] splitCsv(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (c == '"') { inQuotes = !inQuotes; }
            else if (c == ',' && !inQuotes) { result.add(sb.toString()); sb.setLength(0); }
            else { sb.append(c); }
        }
        result.add(sb.toString());
        return result.toArray(new String[0]);
    }

    private static LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        for (DateTimeFormatter fmt : DATE_FORMATTERS) {
            try { return LocalDate.parse(raw, fmt); } catch (Exception ignored) {}
        }
        return null;
    }

    private static LocalDateTime parseDateTime(String raw) {
        if (raw == null || raw.isBlank()) return null;
        for (DateTimeFormatter fmt : DATETIME_FORMATTERS) {
            try { return LocalDateTime.parse(raw, fmt); } catch (Exception ignored) {}
        }
        // fallback: try date-only
        LocalDate d = parseDate(raw);
        return d != null ? d.atStartOfDay() : null;
    }
}
