package jdm.service;

import jdm.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Descriptive stats, trends, and exploratory correlation between CMAS and lab biomarkers.
 */
public class ClinicalAnalyticsService {

    /** Biomarkers highlighted in the JDM case description (subset may exist in the extract). */
    public static final List<String> HIGHLIGHT_BIOMARKERS = List.of(
            "CXCL10", "Galectin-9", "IL-18", "TNFR2");

    private final DataStore store = DataStore.getInstance();

    /**
     * Prefer the main CMAS total row from the transposed file (label typically contains {@code "> 10"}).
     */
    public List<CmasEntry> primaryCmasSeries(Patient p) {
        List<CmasEntry> hi = p.getCmasEntriesSorted().stream()
                .filter(e -> e.getScoreGroup() != null
                        && (e.getScoreGroup().contains("> 10") || e.getScoreGroup().toLowerCase().contains("cmas score >")))
                .collect(Collectors.toList());
        if (!hi.isEmpty()) return hi;
        return p.getCmasEntriesSorted();
    }

    public OptionalDouble latestNumericMeasurement(LabResult lr) {
        return lr.getMeasurements().stream()
                .map(Measurement::getValue)
                .map(ClinicalAnalyticsService::parseDoubleLoose)
                .filter(OptionalDouble::isPresent)
                .mapToDouble(OptionalDouble::getAsDouble)
                .max();
    }

    public static OptionalDouble parseDoubleLoose(String raw) {
        if (raw == null) return OptionalDouble.empty();
        String s = raw.trim().replace(',', '.');
        if (s.isEmpty()) return OptionalDouble.empty();
        try {
            return OptionalDouble.of(Double.parseDouble(s));
        } catch (NumberFormatException e) {
            return OptionalDouble.empty();
        }
    }

    public static double pearsonCorrelation(double[] x, double[] y) {
        int n = Math.min(x.length, y.length);
        if (n < 3) return Double.NaN;
        double sx = 0, sy = 0, sxx = 0, syy = 0, sxy = 0;
        for (int i = 0; i < n; i++) {
            sx += x[i];
            sy += y[i];
            sxx += x[i] * x[i];
            syy += y[i] * y[i];
            sxy += x[i] * y[i];
        }
        double num = n * sxy - sx * sy;
        double den = Math.sqrt((n * sxx - sx * sx) * (n * syy - sy * sy));
        if (den == 0) return Double.NaN;
        return num / den;
    }

    /**
     * Pairs same-calendar-day CMAS (primary series) and biomarker values for exploratory correlation.
     */
    public CorrelationResult cmasBiomarkerSameDay(Patient patient, String englishBiomarkerName) {
        List<CmasEntry> cmas = primaryCmasSeries(patient);
        Map<LocalDate, Integer> byDate = cmas.stream()
                .collect(Collectors.toMap(CmasEntry::getDate, CmasEntry::getValue, (a, b) -> a));

        Optional<LabResult> bio = store.getLabResultsForPatient(patient.getPatientId()).stream()
                .filter(lr -> englishBiomarkerName.equalsIgnoreCase(lr.getResultNameEnglish().trim())
                        || englishBiomarkerName.equalsIgnoreCase(lr.getResultNameOriginal().trim()))
                .findFirst();

        if (bio.isEmpty()) {
            return new CorrelationResult(englishBiomarkerName, 0, Double.NaN,
                    "No lab result named like \"" + englishBiomarkerName + "\" for this patient.");
        }

        List<Double> xs = new ArrayList<>();
        List<Double> ys = new ArrayList<>();
        for (Measurement m : bio.get().getMeasurements()) {
            LocalDateTime dt = m.getDateTime();
            if (dt == null) continue;
            LocalDate d = dt.toLocalDate();
            if (!byDate.containsKey(d)) continue;
            OptionalDouble v = parseDoubleLoose(m.getValue());
            if (v.isEmpty()) continue;
            xs.add((double) byDate.get(d));
            ys.add(v.getAsDouble());
        }

        int paired = xs.size();
        if (paired < 3) {
            return new CorrelationResult(englishBiomarkerName, paired, Double.NaN,
                    "Need at least three visits with both CMAS and " + englishBiomarkerName
                            + " on the same calendar day. Paired points: " + paired + ".");
        }

        double[] xa = xs.stream().mapToDouble(Double::doubleValue).toArray();
        double[] ya = ys.stream().mapToDouble(Double::doubleValue).toArray();
        return new CorrelationResult(englishBiomarkerName, paired, pearsonCorrelation(xa, ya), null);
    }

    public String cohortSummary() {
        Collection<Patient> pts = store.getAllPatients();
        long totalMeas = store.getAllLabResults().stream()
                .mapToLong(lr -> lr.getMeasurements().size()).sum();
        long cmasN = pts.stream().mapToLong(p -> p.getCmasEntries().size()).sum();

        StringBuilder sb = new StringBuilder();
        sb.append("Cohort overview\n");
        sb.append("  Patients in repository: ").append(pts.size()).append('\n');
        sb.append("  Total lab measurements: ").append(totalMeas).append('\n');
        sb.append("  Total CMAS grid cells loaded: ").append(cmasN).append('\n');
        sb.append("\nHighlight biomarkers (latest numeric value per analyte, any patient):\n");
        for (String name : HIGHLIGHT_BIOMARKERS) {
            sb.append("  • ").append(name).append(": ")
                    .append(summarizeBiomarkerAcrossCohort(name)).append('\n');
        }
        return sb.toString();
    }

    private String summarizeBiomarkerAcrossCohort(String englishName) {
        List<String> snippets = new ArrayList<>();
        for (LabResult lr : store.getAllLabResults()) {
            String en = lr.getResultNameEnglish();
            if (en == null || !englishName.equalsIgnoreCase(en.trim())) continue;
            latestNumericMeasurement(lr).ifPresent(v ->
                    snippets.add(String.format(Locale.US, "%s → %.4g %s",
                            lr.getPatientId().substring(0, Math.min(8, lr.getPatientId().length())) + "…",
                            v, lr.getUnit() == null ? "" : lr.getUnit())));
        }
        if (snippets.isEmpty()) return "not present in lab definitions.";
        return String.join("; ", snippets);
    }

    public record CorrelationResult(String biomarker, int pairedPoints, double r,
                                    String message) {
        @Override
        public String toString() {
            if (message != null) return message;
            return String.format(Locale.US, "%s vs primary CMAS (same-day): n=%d, Pearson r=%.3f",
                    biomarker, pairedPoints, r);
        }
    }
}
