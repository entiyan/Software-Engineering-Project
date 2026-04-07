package jdm.util;

public class Display {
    public static final String RESET  = "\u001B[0m";
    public static final String BOLD   = "\u001B[1m";
    public static final String CYAN   = "\u001B[36m";
    public static final String GREEN  = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String RED    = "\u001B[31m";
    public static final String BLUE   = "\u001B[34m";

    public static void header(String title) {
        int w = 70;
        String bar = "═".repeat(w);
        System.out.println(BOLD + CYAN + "╔" + bar + "╗" + RESET);
        System.out.printf(BOLD + CYAN + "║  %-66s  ║%n" + RESET, title);
        System.out.println(BOLD + CYAN + "╚" + bar + "╝" + RESET);
    }

    public static void section(String title) {
        System.out.println(BOLD + YELLOW + "\n── " + title + " " + "─".repeat(Math.max(0, 66 - title.length())) + RESET);
    }

    public static void success(String msg) { System.out.println(GREEN  + "✔ " + msg + RESET); }
    public static void error(String msg)   { System.out.println(RED    + "✖ " + msg + RESET); }
    public static void info(String msg)    { System.out.println(BLUE   + "ℹ " + msg + RESET); }
    public static void warn(String msg)    { System.out.println(YELLOW + "⚠ " + msg + RESET); }

    public static void divider() { System.out.println(CYAN + "─".repeat(72) + RESET); }

    public static void menuItem(int n, String label) {
        System.out.printf("  %s[%d]%s %s%n", BOLD, n, RESET, label);
    }

    public static String severity(String label) {
        return switch (label) {
            case "Remission" -> GREEN  + label + RESET;
            case "Mild"      -> YELLOW + label + RESET;
            case "Moderate"  -> RED    + label + RESET;
            case "Severe"    -> BOLD + RED + label + RESET;
            default          -> label;
        };
    }
}
