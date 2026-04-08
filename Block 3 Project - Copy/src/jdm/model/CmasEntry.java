package jdm.model;

import java.time.LocalDate;

public class CmasEntry implements Comparable<CmasEntry> {
    private final LocalDate date;
    private final String scoreGroup; // "> 10" or "4-9"
    private final int value;

    public CmasEntry(LocalDate date, String scoreGroup, int value) {
        this.date = date;
        this.scoreGroup = scoreGroup;
        this.value = value;
    }

    public LocalDate getDate()       { return date; }
    public String    getScoreGroup() { return scoreGroup; }
    public int       getValue()      { return value; }

    /** Severity label based on score and group. */
    public String getSeverityLabel() {
        if (value >= 48)        return "Remission";
        else if (value >= 40)   return "Mild";
        else if (value >= 28)   return "Moderate";
        else                    return "Severe";
    }

    @Override public int compareTo(CmasEntry o) { return this.date.compareTo(o.date); }

    @Override public String toString() {
        return String.format("%-12s | Group: %-6s | Score: %3d | %s",
                date, scoreGroup, value, getSeverityLabel());
    }
}
