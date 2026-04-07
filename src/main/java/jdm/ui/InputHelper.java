package jdm.ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class InputHelper {
    private static final Scanner SC = new Scanner(System.in);
    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static String prompt(String msg) {
        System.out.print(msg);
        return SC.nextLine().trim();
    }

    public static int promptInt(String msg) {
        while (true) {
            System.out.print(msg);
            String s = SC.nextLine().trim();
            try { return Integer.parseInt(s); }
            catch (NumberFormatException e) { System.out.println("  Please enter a number."); }
        }
    }

    public static LocalDateTime promptDateTime(String msg) {
        while (true) {
            System.out.print(msg + " (yyyy-MM-dd HH:mm): ");
            String s = SC.nextLine().trim();
            try { return LocalDateTime.parse(s, DT_FMT); }
            catch (Exception e) { System.out.println("  Invalid format. Try: 2025-06-15 09:30"); }
        }
    }

    public static boolean confirm(String msg) {
        String ans = prompt(msg + " (y/n): ");
        return ans.equalsIgnoreCase("y");
    }

    public static Scanner getScanner() { return SC; }
}
