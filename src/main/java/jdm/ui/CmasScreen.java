package jdm.ui;

import jdm.model.*;
import jdm.service.DataStore;
import jdm.util.Display;

import java.util.*;

public class CmasScreen {
    private final DataStore store = DataStore.getInstance();

    public void show(String patientId) {
        Patient patient = store.getPatient(patientId);
        if (patient == null) { Display.error("Patient not found."); return; }

        while (true) {
            System.out.println();
            Display.header("CMAS Scores — " + patient.getName());
            Display.info("CMAS = Childhood Myositis Assessment Scale (max 52 points)");
            System.out.println();
            Display.menuItem(1, "View all CMAS entries (chronological)");
            Display.menuItem(2, "View latest score summary");
            Display.menuItem(3, "View score trend (mini chart)");
            Display.menuItem(0, "Back");

            int choice = InputHelper.promptInt("\nSelect: ");
            switch (choice) {
                case 1 -> viewAll(patient);
                case 2 -> viewLatest(patient);
                case 3 -> viewTrend(patient);
                case 0 -> { return; }
                default -> Display.error("Invalid selection.");
            }
        }
    }

    // ── All entries ──────────────────────────────────────────────────────────

    private void viewAll(Patient patient) {
        List<CmasEntry> entries = new ArrayList<>(patient.getCmasEntries());
        entries.sort(Comparator.naturalOrder());
        System.out.println();
        Display.section("All CMAS Entries (" + entries.size() + " total)");
        if (entries.isEmpty()) { Display.warn("No entries found."); }
        else {
            System.out.printf("  %-14s %-10s %6s   %s%n", "Date", "Group", "Score", "Severity");
            Display.divider();
            for (CmasEntry e : entries) {
                System.out.printf("  %-14s %-10s %6d   %s%n",
                        e.getDate(), e.getScoreGroup(), e.getValue(),
                        Display.severity(e.getSeverityLabel()));
            }
        }
        InputHelper.prompt("\nPress Enter to continue...");
    }

    // ── Latest summary ───────────────────────────────────────────────────────

    private void viewLatest(Patient patient) {
        List<CmasEntry> entries = new ArrayList<>(patient.getCmasEntries());
        if (entries.isEmpty()) { Display.warn("No CMAS data."); return; }
        entries.sort(Comparator.naturalOrder());

        CmasEntry latest = entries.get(entries.size() - 1);
        OptionalDouble avg = entries.stream().mapToInt(CmasEntry::getValue).average();
        int max = entries.stream().mapToInt(CmasEntry::getValue).max().orElse(0);
        int min = entries.stream().mapToInt(CmasEntry::getValue).min().orElse(0);

        System.out.println();
        Display.section("CMAS Summary");
        System.out.printf("  Latest score  : %s%d%s  (%s)%n",
                Display.BOLD, latest.getValue(), Display.RESET,
                Display.severity(latest.getSeverityLabel()));
        System.out.printf("  Date          : %s%n", latest.getDate());
        System.out.printf("  Average score : %.1f%n", avg.orElse(0));
        System.out.printf("  Max / Min     : %d / %d%n", max, min);
        System.out.printf("  Total entries : %d%n", entries.size());
        System.out.println();

        // Severity scale legend
        Display.section("Severity Scale");
        System.out.println("  " + Display.severity("Remission") + "  ≥ 48 pts");
        System.out.println("  " + Display.severity("Mild")      + "       ≥ 40 pts");
        System.out.println("  " + Display.severity("Moderate")  + "   ≥ 28 pts");
        System.out.println("  " + Display.severity("Severe")    + "     < 28 pts");

        InputHelper.prompt("\nPress Enter to continue...");
    }

    // ── Mini ASCII trend chart ───────────────────────────────────────────────

    private void viewTrend(Patient patient) {
        List<CmasEntry> entries = new ArrayList<>(patient.getCmasEntries());
        entries.sort(Comparator.naturalOrder());
        if (entries.size() < 2) { Display.warn("Not enough data for trend."); return; }

        // Use last 20 entries for readability
        if (entries.size() > 20) entries = entries.subList(entries.size() - 20, entries.size());

        int chartHeight = 10;
        int chartWidth  = entries.size();
        int maxScore    = 52;

        System.out.println();
        Display.section("CMAS Score Trend (last " + entries.size() + " entries)");

        // Build chart rows top-to-bottom
        for (int row = chartHeight; row >= 0; row--) {
            int threshold = (int) Math.round((double) row / chartHeight * maxScore);
            System.out.printf("  %3d |", threshold);
            for (CmasEntry e : entries) {
                int barHeight = (int) Math.round((double) e.getValue() / maxScore * chartHeight);
                System.out.print(barHeight >= row ? "█" : " ");
            }
            System.out.println();
        }
        System.out.print("      +");
        System.out.println("─".repeat(chartWidth));

        System.out.printf("  %s  →  %s%n",
                entries.get(0).getDate(), entries.get(entries.size()-1).getDate());
        System.out.printf("  Score range in view: %d – %d%n",
                entries.stream().mapToInt(CmasEntry::getValue).min().orElse(0),
                entries.stream().mapToInt(CmasEntry::getValue).max().orElse(0));

        InputHelper.prompt("\nPress Enter to continue...");
    }
}
